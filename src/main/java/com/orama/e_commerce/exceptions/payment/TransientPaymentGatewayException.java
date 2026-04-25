package com.orama.e_commerce.exceptions.payment;

public class TransientPaymentGatewayException extends PaymentGatewayException {

  public TransientPaymentGatewayException(String message) {
    super(message);
  }

  public TransientPaymentGatewayException(String message, Throwable cause) {
    super(message, cause);
  }
}
