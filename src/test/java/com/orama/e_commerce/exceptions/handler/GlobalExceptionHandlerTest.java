package com.orama.e_commerce.exceptions.handler;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.servlet.resource.NoResourceFoundException;

class GlobalExceptionHandlerTest {

  private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

  @Test
  void shouldReturnNotFoundForMissingStaticResource() {
    MockHttpServletRequest request = new MockHttpServletRequest("GET", "/swagger-ui.html");
    NoResourceFoundException exception =
        new NoResourceFoundException(HttpMethod.GET, "swagger-ui.html");

    ResponseEntity<ErrorMessage> response = handler.handleNoResourceFound(exception, request);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getStatus()).isEqualTo(404);
    assertThat(response.getBody().getPath()).isEqualTo("/swagger-ui.html");
  }
}
