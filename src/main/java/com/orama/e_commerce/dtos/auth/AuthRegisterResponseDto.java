package com.orama.e_commerce.dtos.auth;

public record AuthRegisterResponseDto(
        String email,
        String role
) {}