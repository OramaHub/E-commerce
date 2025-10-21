package com.orama.e_commerce.exceptions.product;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class ProductAlreadyActiveException extends RuntimeException {
  public ProductAlreadyActiveException(String message) {
    super(message);
  }
}
