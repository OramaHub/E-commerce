package com.orama.e_commerce.dtos.refresh_token;

import jakarta.validation.constraints.NotBlank;

public record RefreshTokenRequestDto(@NotBlank String refreshToken) {}
