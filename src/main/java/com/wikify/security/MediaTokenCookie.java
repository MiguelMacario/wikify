package com.wikify.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * Cookie que autentica APENAS a leitura de mídia.
 *
 * Por que ele existe: o access token vive em memória e viaja no header
 * Authorization, mas &lt;img src="/media/..."&gt; é requisição iniciada pelo
 * navegador — ela não carrega header nenhum, só cookie. Sem isto, toda imagem
 * e todo vídeo do documento voltam 401.
 *
 * Por que não é perigoso como guardar o access token em cookie:
 * <ul>
 *   <li>é HttpOnly, então um XSS não o lê;</li>
 *   <li>tem `path=/media`, então o navegador não o manda para mais nada;</li>
 *   <li>carrega claim `type=media`, então mesmo capturado e reenviado no header
 *       ele não passa no validateAccessToken — não abre nenhum outro endpoint.</li>
 * </ul>
 *
 * Mesmas configurações de `secure` e `sameSite` do refresh token, pelo mesmo
 * motivo: se a API for para outro domínio registrável, os dois precisam mudar
 * juntos.
 */
@Component
public class MediaTokenCookie {

    public static final String NAME = "mediaToken";

    private static final String PATH = "/media";

    @Value("${api.security.cookie.secure}")
    private boolean secure;

    @Value("${api.security.cookie.same-site}")
    private String sameSite;

    public ResponseCookie create(String mediaToken) {
        return base(mediaToken)
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
