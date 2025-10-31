package com.orama.e_commerce.dtos.stripe_entities;

import java.math.BigDecimal;

public record ProductRequest(BigDecimal amount, Long quantity, String name, String currency) {}
