package com.orama.e_commerce.exceptions.product;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class ProductAlreadyInactiveException extends RuntimeException {
  public ProductAlreadyInactiveException(String message) {
    super(message);
  }
}
