package com.orama.e_commerce.exceptions.payment;

public class WebhookProcessingException extends RuntimeException {
  public WebhookProcessingException(String message, Throwable cause) {
    super(message, cause);
  }
}
