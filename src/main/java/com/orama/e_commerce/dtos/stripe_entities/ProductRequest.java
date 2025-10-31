package com.orama.e_commerce.dtos.stripe_entities;

public record ProductRequest(Long amount, Long quantity, String name, String currency) {}
