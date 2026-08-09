package com.wikify.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.wikify.entity.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class TokenService {

    private static final String ISSUER = "AuthWikify";
    private static final String TYPE_CLAIM = "type";
    private static final String ACCESS_TYPE = "access";
    private static final String REFRESH_TYPE = "refresh";

    private static final long ACCESS_TOKEN_EXPIRATION_SECONDS = 2 * 60 * 60;
    public static final long REFRESH_TOKEN_EXPIRATION_SECONDS = 7 * 24 * 60 * 60;

    @Value("${api.security.token.secret}")
    String secret;

    public String generateAccessToken(User user) {
        try{
            return JWT.create()
                    .withIssuer(ISSUER)
                    .withSubject(user.getLogin())
                    .withClaim(TYPE_CLAIM, ACCESS_TYPE)
                    .withClaim("roles", user.getAuthorities().stream()
                            .map(GrantedAuthority::getAuthority)
                            .toList())
                    .withExpiresAt(expiresIn(ACCESS_TOKEN_EXPIRATION_SECONDS))
                    .sign(algorithm());
        } catch(JWTCreationException exception){
            throw new RuntimeException("JWT creation exception", exception);
        }
    }

    public String generateRefreshToken(String login) {
        try{
            // Payload mínimo: o refresh token só identifica quem está renovando
            return JWT.create()
                    .withIssuer(ISSUER)
                    .withSubject(login)
                    .withClaim(TYPE_CLAIM, REFRESH_TYPE)
                    .withExpiresAt(expiresIn(REFRESH_TOKEN_EXPIRATION_SECONDS))
                    .sign(algorithm());
        } catch(JWTCreationException exception){
            throw new RuntimeException("JWT refresh token creation exception", exception);
        }
    }

    public String validateAccessToken(String token){
        return subjectOf(token, ACCESS_TYPE);
    }

    public String validateRefreshToken(String token){
        return subjectOf(token, REFRESH_TYPE);
    }

    private String subjectOf(String token, String expectedType){
        try{
            return JWT.require(algorithm())
                    .withIssuer(ISSUER)
                    .withClaim(TYPE_CLAIM, expectedType)
                    .build()
                    .verify(token)
                    .getSubject();
        } catch(JWTVerificationException exception){
            return null;
        }
    }

    private Algorithm algorithm() {
        return Algorithm.HMAC256(secret);
    }

    private Instant expiresIn(long seconds) {
        return Instant.now().plusSeconds(seconds);
    }
}
