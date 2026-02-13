package com.orama.e_commerce.dtos.auth;

import jakarta.validation.constraints.NotBlank;

public record ResetPasswordRequestDto(
    @NotBlank(message = "Token is required") String token,
    @NotBlank(message = "New password is required") String newPassword) {}
