package com.wikify.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class RefreshTokenCookie {

    public static final String NAME = "refreshToken";

    private static final String PATH = "/auth/refresh";

    @Value("${api.security.cookie.secure}")
    private boolean secure;

    @Value("${api.security.cookie.same-site}")
    private String sameSite;

    public ResponseCookie create(String refreshToken) {
        return base(refreshToken)
                .maxAge(Duration.ofSeconds(TokenService.REFRESH_TOKEN_EXPIRATION_SECONDS))
                .build();
    }

    public ResponseCookie expire() {
        return base("").maxAge(Duration.ZERO).build();
    }

    private ResponseCookie.ResponseCookieBuilder base(String value) {
        return ResponseCookie.from(NAME, value)
                .httpOnly(true)
                .secure(secure)
                .sameSite(sameSite)
                .path(PATH);
    }
}
