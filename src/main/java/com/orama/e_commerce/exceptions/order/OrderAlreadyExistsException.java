package com.orama.e_commerce.exceptions.order;

public class OrderAlreadyExistsException extends RuntimeException {
  public OrderAlreadyExistsException(String message) {
    super(message);
  }
}
