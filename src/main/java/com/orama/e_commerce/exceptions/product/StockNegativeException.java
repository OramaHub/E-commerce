package com.orama.e_commerce.exceptions.product;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class StockNegativeException extends RuntimeException {
  public StockNegativeException(String message) {
    super(message);
  }
}
