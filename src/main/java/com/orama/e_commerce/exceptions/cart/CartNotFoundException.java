package com.orama.e_commerce.exceptions.cart;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class CartNotFoundException extends RuntimeException {

  public CartNotFoundException(String message) {
    super(message);
  }
}
