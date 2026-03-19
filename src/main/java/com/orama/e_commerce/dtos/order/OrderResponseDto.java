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
    BigDecimal shippingCost,
    BigDecimal total,
    String zipCode,
    Long clientId,
    String clientName,
    String paymentId,
    String paymentMethod,
    List<OrderItemDto> items) {}
