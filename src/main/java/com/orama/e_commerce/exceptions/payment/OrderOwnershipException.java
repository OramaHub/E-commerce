package com.orama.e_commerce.exceptions.payment;

public class OrderOwnershipException extends RuntimeException {
  public OrderOwnershipException(String message) {
    super(message);
  }
}
