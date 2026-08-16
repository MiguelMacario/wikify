package com.wikify.security;

import com.wikify.repositories.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;

@Component
public class SecurityFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";
    private static final String MEDIA_PATH = "/media/";

    private final TokenService tokenService;
    private final UserRepository userRepository;

    public SecurityFilter(TokenService tokenService, UserRepository userRepository) {
        this.tokenService = tokenService;
        this.userRepository = userRepository;
    }


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String login = loginFromHeader(request);

        // Só se o header não autenticou: o token de mídia é a exceção, não a
        // regra, e nunca deve ganhar do caminho normal.
        if (login == null && isMediaRequest(request)) {
            login = loginFromMediaCookie(request);
        }

        if (login != null) {
            userRepository.findByLogin(login).ifPresent(user -> {
                var authentication = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(authentication);
            });
        }

        filterChain.doFilter(request, response);
    }

    private String loginFromHeader(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {return null;}
        return tokenService.validateAccessToken(authHeader.substring(BEARER_PREFIX.length()).trim());
    }

    /**
     * O `path=/media` do cookie faz o NAVEGADOR não mandá-lo para outras rotas,
     * mas isso é conveniência do navegador, não garantia: um cliente qualquer
     * manda o cookie que quiser para onde quiser. Por isso a rota é conferida
     * aqui também — é esta checagem que impede o token de mídia de autenticar
     * uma escrita em /docs.
     */
    private boolean isMediaRequest(HttpServletRequest request) {
        return request.getRequestURI().startsWith(MEDIA_PATH);
    }

    private String loginFromMediaCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) return null;

        Optional<String> token = Arrays.stream(cookies)
                .filter(cookie -> MediaTokenCookie.NAME.equals(cookie.getName()))
                .map(Cookie::getValue)
                .findFirst();

        return token.map(tokenService::validateMediaToken).orElse(null);
    }
}
