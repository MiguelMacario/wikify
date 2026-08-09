package com.wikify.security;

import com.wikify.dto.LoginResponseDTO;

public record AuthResult(String refreshToken, LoginResponseDTO response) {
}
