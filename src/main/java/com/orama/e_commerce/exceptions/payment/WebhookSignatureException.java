package com.orama.e_commerce.exceptions.payment;

public class WebhookSignatureException extends RuntimeException {
  public WebhookSignatureException(String message) {
    super(message);
  }
}
