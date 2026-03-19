package com.orama.e_commerce.dtos.payment;

import jakarta.validation.constraints.NotBlank;

public record InitiatePaymentRequestDto(
    @NotBlank String paymentType, String cardToken, Integer installments, String paymentMethodId) {}
