package com.orama.e_commerce.exceptions.payment;

public class PaymentAlreadyInProgressException extends RuntimeException {
  public PaymentAlreadyInProgressException(String message) {
    super(message);
  }
}
