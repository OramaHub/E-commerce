package com.orama.e_commerce.dtos.client;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ClientUpdateRequestDto(
    @NotBlank(message = "Nome é obrigatório")
        @Size(max = 150, message = "Nome deve ter no máximo 150 caracteres")
        String name,
    @NotBlank(message = "Email é obrigatório")
        @Email(
            message = "Formato de email inválido",
            regexp = "^[a-z0-9.+-_]+@[a-z0-9.-]+\\.[a-z]{2,}$")
        @Size(max = 100, message = "Email deve ter no máximo 100 caracteres")
        String email,
    @NotBlank(message = "Telefone é obrigatório")
        @Size(max = 30, message = "Telefone deve ter no máximo 30 caracteres")
        String phone) {}
