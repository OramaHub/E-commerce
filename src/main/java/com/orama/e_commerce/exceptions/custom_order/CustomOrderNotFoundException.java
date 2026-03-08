package com.orama.e_commerce.exceptions.custom_order;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class CustomOrderNotFoundException extends RuntimeException {

  public CustomOrderNotFoundException(String message) {
    super(message);
  }
}
