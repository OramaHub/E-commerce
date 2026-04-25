package com.orama.e_commerce.exceptions.payment;

public class PermanentPaymentGatewayException extends PaymentGatewayException {

  public PermanentPaymentGatewayException(String message) {
    super(message);
  }

  public PermanentPaymentGatewayException(String message, Throwable cause) {
    super(message, cause);
  }
}
