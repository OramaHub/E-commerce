package com.orama.e_commerce.service.gateway;

public record GatewayOrderResult(
    String providerOrderId, String status, String statusDetail, String paymentMethodId) {}
