package com.orama.e_commerce.dtos.shipping;

import jakarta.validation.constraints.NotBlank;

public record ShippingCalculateRequestDto(
    @NotBlank(message = "CEP é obrigatório") String zipCode) {}
