package com.orama.e_commerce.service.gateway;

public interface PaymentGateway {

  GatewayPaymentResult createPayment(CreatePaymentCommand command);

  GatewayOrderResult getOrderStatus(String providerOrderId);
}
