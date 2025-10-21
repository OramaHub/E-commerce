package com.orama.e_commerce.exceptions.handler;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorMessage {

  private final String path;
  private final String method;
  private final int status;
  private final String statusMessage;
  private final String errorMessage;
  private Map<String, String> validationErrors;

  public ErrorMessage(HttpServletRequest request, HttpStatus status, String errorMessage) {
    this.path = request.getRequestURI();
    this.method = request.getMethod();
    this.status = status.value();
    this.statusMessage = status.getReasonPhrase();
    this.errorMessage = errorMessage;
  }

  public ErrorMessage(
      HttpServletRequest request,
      HttpStatus status,
      String errorMessage,
      BindingResult validationResult) {
    this.path = request.getRequestURI();
    this.method = request.getMethod();
    this.status = status.value();
    this.statusMessage = status.getReasonPhrase();
    this.errorMessage = errorMessage;
    addErrors(validationResult);
  }

  private void addErrors(BindingResult validationResult) {
    this.validationErrors = new HashMap<>();

    for (FieldError fieldError : validationResult.getFieldErrors()) {
      this.validationErrors.put(fieldError.getField(), fieldError.getDefaultMessage());
    }
  }

  public String getPath() {
    return path;
  }

  public String getMethod() {
    return method;
  }

  public int getStatus() {
    return status;
  }

  public String getStatusMessage() {
    return statusMessage;
  }

  public String getErrorMessage() {
    return errorMessage;
  }

  public Map<String, String> getValidationErrors() {
    return validationErrors;
  }
}
