package com.wikify.security;

import com.wikify.dto.LoginResponseDTO;
import com.wikify.dto.RegisterDTO;
import com.wikify.dto.DepartmentAccessDTO;
import com.wikify.entity.User;
import com.wikify.entity.enums.SystemRole;
import com.wikify.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;
    private final AuthenticationManager authenticationManager;

    public AuthResult login(String login, String password) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(login, password));

        return resultFor((User) authentication.getPrincipal());
    }

    public AuthResult refresh(String refreshToken) {
        String login = refreshToken == null ? null : tokenService.validateRefreshToken(refreshToken);
        if (login == null) {
            throw new BadCredentialsException("Refresh token inválido ou expirado");
        }

        User user = userRepository.findByLogin(login)
                .orElseThrow(() -> new BadCredentialsException("Refresh token inválido ou expirado"));

        return resultFor(user);
    }

    public void registerUser(RegisterDTO data) {

        if (userRepository.findByLogin(data.login()).isPresent()
                || userRepository.findByEmail(data.email()).isPresent()) {
            throw new IllegalArgumentException("Usuário ou email já cadastrado.");
        }

        String encodedPassword = passwordEncoder.encode(data.password());
        User newUser = new User(
                data.login(),
                encodedPassword,
                data.email(),
                SystemRole.USER,
                data.name()
        );

        userRepository.save(newUser);
    }

    private AuthResult resultFor(User user) {
        List<DepartmentAccessDTO> departments = user.getMemberships().stream()
                .map(DepartmentAccessDTO::from)
                .sorted(Comparator.comparing(DepartmentAccessDTO::name))
                .toList();

        LoginResponseDTO response = new LoginResponseDTO(
                tokenService.generateAccessToken(user),
                user.getSystemRole(),
                departments);

        return new AuthResult(
                tokenService.generateRefreshToken(user.getLogin()),
                tokenService.generateMediaToken(user.getLogin()),
                response);
    }
}
