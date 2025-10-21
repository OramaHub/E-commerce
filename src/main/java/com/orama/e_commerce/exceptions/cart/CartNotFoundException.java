package com.orama.e_commerce.exceptions.cart;

public class CartNotFoundException extends RuntimeException {
  public CartNotFoundException(String message) {
    super(message);
  }
}
