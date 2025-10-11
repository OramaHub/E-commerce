package com.orama.e_commerce.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ClientRequestDto(
        @NotBlank(message = "Name cannot be empty")
        @Size(max = 150, message = "Name must be at most 150 characters long")
        String name,
        @NotBlank(message = "Email cannot be empty")
        @Email(
                message = "Email format is invalid",
                regexp = "^[a-z0-9.+-_]+@[a-z0-9.-]+\\.[a-z]{2,}$")
        @Size(max = 100, message = "Email must be at most 100 characters long")
        String email,
        @NotBlank(message = "Password cannot be empty") String password,
        @NotBlank String cpf,
        @NotBlank String phone) {}
