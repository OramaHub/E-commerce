package com.orama.e_commerce.dtos.order;

import java.math.BigDecimal;

public record OrderItemDto(
    Long productId,
    String productName,
    Integer quantity,
    BigDecimal unitPrice,
    BigDecimal subtotal) {}
