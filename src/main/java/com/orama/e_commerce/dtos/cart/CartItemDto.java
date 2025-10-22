package com.orama.e_commerce.dtos.cart;

import java.math.BigDecimal;

public record CartItemDto(
    Long id,
    Long productId,
    String productName,
    Integer quantity,
    BigDecimal unitPrice,
    BigDecimal subtotal) {}
