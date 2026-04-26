package com.orama.e_commerce.service.gateway;

public interface PaymentGateway {

  GatewayPaymentResult createPayment(CreatePaymentCommand command);

  GatewayOrderResult getOrderStatus(String providerOrderId);

  GatewayOrderResult cancelOrder(String providerOrderId, String idempotencyKey);

  GatewayOrderResult refundOrder(String providerOrderId, String idempotencyKey);
}
