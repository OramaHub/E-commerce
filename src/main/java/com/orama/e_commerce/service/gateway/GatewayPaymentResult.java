package com.orama.e_commerce.service.gateway;

public record GatewayPaymentResult(
    String providerOrderId,
    String providerPaymentId,
    String status,
    String statusDetail,
    String paymentMethodId,
    String qrCode,
    String qrCodeBase64,
    String ticketUrl,
    String digitableLine) {}
