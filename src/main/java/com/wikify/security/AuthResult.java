package com.wikify.security;

import com.wikify.dto.LoginResponseDTO;

/**
 * O que uma autenticação bem-sucedida produz.
 *
 * São três credenciais com alcances diferentes de propósito: o access token vai
 * no corpo (memória do front), o refresh e o de mídia viram cookies HttpOnly
 * com paths distintos. Cada um só serve para o que o nome diz.
 */
public record AuthResult(String refreshToken, String mediaToken, LoginResponseDTO response) {
}
