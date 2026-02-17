package com.orama.e_commerce.dtos.client;

import jakarta.validation.constraints.NotBlank;

public record ChangePasswordRequestDto(
    @NotBlank(message = "Senha atual é obrigatória") String currentPassword,
    @NotBlank(message = "Nova senha é obrigatória") String newPassword) {}
