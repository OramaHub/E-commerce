package com.orama.e_commerce.exceptions.client;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class ClientAlreadyExistsException extends RuntimeException {
  public ClientAlreadyExistsException(String message) {
    super(message);
  }
}
