package com.orama.e_commerce.dtos.client;

import com.orama.e_commerce.validation.CPF;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ClientRequestDto(
    @NotBlank(message = "Name cannot be empty")
        @Size(min = 3, max = 150, message = "Name must be between 3 and 150 characters long")
        String name,
    @NotBlank(message = "Email cannot be empty")
        @Email(
            message = "Email format is invalid",
            regexp = "^[a-z0-9.+-_]+@[a-z0-9.-]+\\.[a-z]{2,}$")
        @Size(max = 100, message = "Email must be at most 100 characters long")
        String email,
    @NotBlank(message = "Password cannot be empty")
        @Size(min = 6, message = "Password must be at least 6 characters long")
        String password,
    @NotBlank(message = "CPF cannot be empty")
        @Size(min = 11, max = 11, message = "CPF must be 11 digits")
        @CPF(message = "Invalid CPF")
        String cpf,
    @NotBlank(message = "Phone cannot be empty")
        @Pattern(regexp = "^\\d{10,11}$", message = "Phone must be 10 or 11 digits")
        String phone) {}
