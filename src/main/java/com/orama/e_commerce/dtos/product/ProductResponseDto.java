package com.orama.e_commerce.dtos.product;

import java.math.BigDecimal;

public record ProductResponseDto(
    Long id, String name, String description, BigDecimal price, Integer stock) {}
