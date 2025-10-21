package com.orama.e_commerce.dtos.order;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;

public record CreateOrderRequestDto(
    @NotNull(message = "Cart ID cannot be null") @Positive(message = "Cart ID must be positive")
        Long cartId,
    @PositiveOrZero(message = "Discount must be zero or positive") BigDecimal discount) {}
