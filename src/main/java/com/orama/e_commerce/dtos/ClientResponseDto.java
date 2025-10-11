package com.orama.e_commerce.dtos;

import com.orama.e_commerce.enums.UserRole;

public record ClientResponseDto(Long id, String name, String email, UserRole role) {}
