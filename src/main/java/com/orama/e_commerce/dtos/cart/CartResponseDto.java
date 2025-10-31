package com.orama.e_commerce.dtos.cart;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record CartResponseDto(
    Long id,
    String sessionId,
    Long clientId,
    String clientName,
    List<CartItemDto> items,
    BigDecimal total,
    Instant createdAt,
    Instant updatedAt) {}
