package com.orama.e_commerce.dtos.order;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record OrderResponseDto(
    Long id,
    String orderNumber,
    Instant orderDate,
    BigDecimal subtotal,
    BigDecimal discount,
    BigDecimal total,
    Long clientId,
    String clientName,
    List<OrderItemDto> items) {}
