package com.orama.e_commerce.dtos.client;

import jakarta.validation.constraints.NotBlank;

public record AdminPasswordResetDto(@NotBlank String newPassword) {}
