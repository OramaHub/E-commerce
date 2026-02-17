package com.orama.e_commerce.dtos.client;

import com.orama.e_commerce.validation.CPF;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ClientRequestDto(
    @NotBlank(message = "Nome é obrigatório")
        @Size(min = 3, max = 150, message = "Nome deve ter entre 3 e 150 caracteres")
        String name,
    @NotBlank(message = "Email é obrigatório")
        @Email(
            message = "Formato de email inválido",
            regexp = "^[a-z0-9.+-_]+@[a-z0-9.-]+\\.[a-z]{2,}$")
        @Size(max = 100, message = "Email deve ter no máximo 100 caracteres")
        String email,
    @NotBlank(message = "Senha é obrigatória")
        @Size(min = 6, message = "Senha deve ter no mínimo 6 caracteres")
        String password,
    @NotBlank(message = "CPF é obrigatório")
        @Size(min = 11, max = 11, message = "CPF deve ter 11 dígitos")
        @CPF(message = "CPF inválido")
        String cpf,
    @NotBlank(message = "Telefone é obrigatório")
        @Pattern(regexp = "^\\d{10,11}$", message = "Telefone deve ter 10 ou 11 dígitos")
        String phone) {}
