package com.wikify.controller;

import com.wikify.dto.AuthenticationDTO;
import com.wikify.dto.LoginResponseDTO;
import com.wikify.dto.RegisterDTO;
import com.wikify.security.AuthService;
import com.wikify.security.AuthResult;
import com.wikify.security.RefreshTokenCookie;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("auth")
public class AuthorizationController {

    private final AuthService authService;
    private final RefreshTokenCookie refreshTokenCookie;

    public AuthorizationController(AuthService authService, RefreshTokenCookie refreshTokenCookie) {
        this.authService = authService;
        this.refreshTokenCookie = refreshTokenCookie;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@RequestBody AuthenticationDTO data ){
        try {
            return withRefreshCookie(authService.login(data.login(), data.password()));
        } catch (AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<LoginResponseDTO> refresh(
            @CookieValue(name = RefreshTokenCookie.NAME, required = false) String refreshToken) {
        try {
            return withRefreshCookie(authService.refresh(refreshToken));
        } catch (AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .header(HttpHeaders.SET_COOKIE, refreshTokenCookie.expire().toString())
                    .build();
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, refreshTokenCookie.expire().toString())
                .build();
    }

    @PostMapping("/register")
    public ResponseEntity<Void> register(@RequestBody RegisterDTO data ) {
        try {
            authService.registerUser(data);
            return ResponseEntity.status(HttpStatus.CREATED).build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }

    private ResponseEntity<LoginResponseDTO> withRefreshCookie(AuthResult result) {
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, refreshTokenCookie.create(result.refreshToken()).toString())
                .body(result.response());
    }
}
