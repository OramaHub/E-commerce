package com.orama.e_commerce.exceptions.product_image;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class ProductImageNotFoundException extends RuntimeException {
  public ProductImageNotFoundException(String message) {
    super(message);
  }
}
