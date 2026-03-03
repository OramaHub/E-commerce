package com.orama.e_commerce.dtos.shipping;

import java.math.BigDecimal;

public record ShippingCalculateResponseDto(
    String zipCode,
    String city,
    String state,
    String region,
    BigDecimal shippingCost,
    boolean freeShipping) {}
