package com.orama.e_commerce.dtos.refresh_token;

public record AuthResponseDto(
    String accessToken, String refreshToken, String type, Long expiresIn) {}
