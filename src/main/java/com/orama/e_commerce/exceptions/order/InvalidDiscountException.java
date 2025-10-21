package com.orama.e_commerce.exceptions.order;

public class InvalidDiscountException extends RuntimeException {
  public InvalidDiscountException(String message) {
    super(message);
  }
}
