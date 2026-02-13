package com.orama.e_commerce.exceptions.handler;

import com.orama.e_commerce.exceptions.BadRequestException;
import com.orama.e_commerce.exceptions.auth.InvalidPasswordResetTokenException;
import com.orama.e_commerce.exceptions.auth.InvalidRefreshTokenException;
import com.orama.e_commerce.exceptions.client.*;
import com.orama.e_commerce.exceptions.product.ProductAlreadyActiveException;
import com.orama.e_commerce.exceptions.product.ProductAlreadyInactiveException;
import com.orama.e_commerce.exceptions.product.ProductNotFoundException;
import com.orama.e_commerce.exceptions.product.StockNegativeException;
import com.orama.e_commerce.exceptions.product_image.ProductImageNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

@RestControllerAdvice
public class GlobalExceptionHandler {

  private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorMessage> handleGenericException(
      Exception ex, HttpServletRequest request) {

    logger.error("Unexpected error", ex);
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .contentType(MediaType.APPLICATION_JSON)
        .body(
            new ErrorMessage(
                request,
                HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred. Please try again later."));
  }

  @ExceptionHandler(InvalidPasswordResetTokenException.class)
  public ResponseEntity<ErrorMessage> handleInvalidPasswordResetToken(
      InvalidPasswordResetTokenException ex, HttpServletRequest request) {
    logger.warn("Invalid password reset token: {}", ex.getMessage());

    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorMessage(request, HttpStatus.BAD_REQUEST, ex.getMessage()));
  }

  @ExceptionHandler(InvalidRefreshTokenException.class)
  public ResponseEntity<ErrorMessage> handleInvalidRefreshToken(
      InvalidRefreshTokenException ex, HttpServletRequest request) {
    logger.warn("Invalid refresh token: {}", ex.getMessage());

    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorMessage(request, HttpStatus.UNAUTHORIZED, ex.getMessage()));
  }

  @ExceptionHandler(BadCredentialsException.class)
  public ResponseEntity<ErrorMessage> handleBadCredentials(
      BadCredentialsException ex, HttpServletRequest request) {
    logger.warn("Authentication failed: Bad Credentials for user: {}", ex.getMessage());

    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorMessage(request, HttpStatus.UNAUTHORIZED, "Invalid email or password."));
  }

  @ExceptionHandler(UsernameNotFoundException.class)
  public ResponseEntity<ErrorMessage> handleUsernameNotFound(
      UsernameNotFoundException ex, HttpServletRequest request) {
    logger.warn("Authentication failed: User not found: {}", ex.getMessage());

    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorMessage(request, HttpStatus.UNAUTHORIZED, "Invalid email or password."));
  }

  @ExceptionHandler(InvalidPasswordException.class)
  public ResponseEntity<ErrorMessage> handleInvalidPassword(
      InvalidPasswordException ex, HttpServletRequest request) {
    logger.error("********** API ERROR **********", ex);

    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorMessage(request, HttpStatus.UNAUTHORIZED, ex.getMessage()));
  }

  @ExceptionHandler(EmailAlreadyExistsException.class)
  public ResponseEntity<ErrorMessage> handleEmailAlreadyExists(
      EmailAlreadyExistsException ex, HttpServletRequest request) {
    logger.error("********** API ERROR **********", ex);

    return ResponseEntity.status(HttpStatus.CONFLICT)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorMessage(request, HttpStatus.CONFLICT, ex.getMessage()));
  }

  @ExceptionHandler(ClientNotFoundException.class)
  public ResponseEntity<ErrorMessage> handleClientNotFoundException(
      ClientNotFoundException ex, HttpServletRequest request) {
    logger.error("********** API ERROR **********", ex);

    return ResponseEntity.status(HttpStatus.NOT_FOUND)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorMessage(request, HttpStatus.NOT_FOUND, ex.getMessage()));
  }

  @ExceptionHandler(ClientAlreadyActiveException.class)
  public ResponseEntity<ErrorMessage> handleClientAlreadyActiveException(
      ClientAlreadyActiveException ex, HttpServletRequest request) {
    logger.error("********** API ERROR **********", ex);

    return ResponseEntity.status(HttpStatus.CONFLICT)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorMessage(request, HttpStatus.CONFLICT, ex.getMessage()));
  }

  @ExceptionHandler(ClientAlreadyInactiveException.class)
  public ResponseEntity<ErrorMessage> handleClientAlreadyInactiveException(
      ClientAlreadyInactiveException ex, HttpServletRequest request) {
    logger.error("********** API ERROR **********", ex);

    return ResponseEntity.status(HttpStatus.CONFLICT)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorMessage(request, HttpStatus.CONFLICT, ex.getMessage()));
  }

  @ExceptionHandler(ClientAlreadyExistsException.class)
  public ResponseEntity<ErrorMessage> handleClientAlreadyExistsException(
      ClientAlreadyExistsException ex, HttpServletRequest request) {
    logger.error("********** API ERROR **********", ex);

    return ResponseEntity.status(HttpStatus.CONFLICT)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorMessage(request, HttpStatus.CONFLICT, ex.getMessage()));
  }

  @ExceptionHandler(ProductNotFoundException.class)
  public ResponseEntity<ErrorMessage> handleProductNotFoundException(
      ProductNotFoundException ex, HttpServletRequest request) {
    logger.error("********** API ERROR **********", ex);

    return ResponseEntity.status(HttpStatus.NOT_FOUND)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorMessage(request, HttpStatus.NOT_FOUND, ex.getMessage()));
  }

  @ExceptionHandler(ProductAlreadyActiveException.class)
  public ResponseEntity<ErrorMessage> handleProductAlreadyActiveException(
      ProductAlreadyActiveException ex, HttpServletRequest request) {
    logger.error("********** API ERROR **********", ex);

    return ResponseEntity.status(HttpStatus.CONFLICT)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorMessage(request, HttpStatus.CONFLICT, ex.getMessage()));
  }

  @ExceptionHandler(ProductAlreadyInactiveException.class)
  public ResponseEntity<ErrorMessage> handleProductAlreadyInactiveException(
      ProductAlreadyInactiveException ex, HttpServletRequest request) {
    logger.error("********** API ERROR **********", ex);

    return ResponseEntity.status(HttpStatus.CONFLICT)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorMessage(request, HttpStatus.CONFLICT, ex.getMessage()));
  }

  @ExceptionHandler(StockNegativeException.class)
  public ResponseEntity<ErrorMessage> handleStockNegativeException(
      StockNegativeException ex, HttpServletRequest request) {
    logger.error("********** API ERROR **********", ex);

    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorMessage(request, HttpStatus.BAD_REQUEST, ex.getMessage()));
  }

  @ExceptionHandler(ProductImageNotFoundException.class)
  public ResponseEntity<ErrorMessage> handleProductImageNotFoundException(
      ProductImageNotFoundException ex, HttpServletRequest request) {
    logger.error("********** API ERROR **********", ex);

    return ResponseEntity.status(HttpStatus.NOT_FOUND)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorMessage(request, HttpStatus.NOT_FOUND, ex.getMessage()));
  }

  @ExceptionHandler(BadRequestException.class)
  public ResponseEntity<ErrorMessage> handleBadRequestException(
      BadRequestException ex, HttpServletRequest request) {
    logger.error("********** API ERROR **********", ex);
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .contentType(MediaType.APPLICATION_JSON)
        .body(new ErrorMessage(request, HttpStatus.BAD_REQUEST, ex.getMessage()));
  }

  @ExceptionHandler(MissingPathVariableException.class)
  public ResponseEntity<ErrorMessage> handleMissingPathVariable(
      MissingPathVariableException ex, HttpServletRequest request) {
    logger.error("Missing path variable", ex);
    String message = String.format("Missing path variable: %s", ex.getVariableName());
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(new ErrorMessage(request, HttpStatus.BAD_REQUEST, message));
  }

  @ExceptionHandler(MissingServletRequestParameterException.class)
  public ResponseEntity<ErrorMessage> handleMissingRequestParam(
      MissingServletRequestParameterException ex, HttpServletRequest request) {
    logger.error("Missing request parameter", ex);
    String message = String.format("Missing request parameter: %s", ex.getParameterName());
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(new ErrorMessage(request, HttpStatus.BAD_REQUEST, message));
  }

  @ExceptionHandler(NoHandlerFoundException.class)
  public ResponseEntity<ErrorMessage> handleNoHandlerFound(
      NoHandlerFoundException ex, HttpServletRequest request) {
    logger.error("No handler found for request", ex);
    String message =
        String.format("No handler found for %s %s", ex.getHttpMethod(), ex.getRequestURL());
    return ResponseEntity.status(HttpStatus.NOT_FOUND)
        .body(new ErrorMessage(request, HttpStatus.NOT_FOUND, message));
  }

  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<ErrorMessage> handleIllegalArgumentException(
      IllegalArgumentException ex, HttpServletRequest request) {
    logger.error("********** API ERROR **********", ex);
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .contentType(MediaType.APPLICATION_JSON)
        .body(
            new ErrorMessage(
                request, HttpStatus.BAD_REQUEST, "Invalid argument: " + ex.getMessage()));
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorMessage> handleMethodArgumentNotValidException(
      MethodArgumentNotValidException ex, HttpServletRequest request) {

    logger.error("Validation error", ex);
    return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
        .contentType(MediaType.APPLICATION_JSON)
        .body(
            new ErrorMessage(
                request,
                HttpStatus.UNPROCESSABLE_ENTITY,
                "Invalid field(s)",
                ex.getBindingResult()));
  }

  @ExceptionHandler(AccessDeniedException.class)
  public ResponseEntity<ErrorMessage> handleAccessDeniedException(
      AccessDeniedException ex, HttpServletRequest request) {
    logger.error("********** API ERROR **********", ex);
    return ResponseEntity.status(HttpStatus.FORBIDDEN)
        .contentType(MediaType.APPLICATION_JSON)
        .body(
            new ErrorMessage(
                request,
                HttpStatus.FORBIDDEN,
                "Access denied: " + "You do not have permission to access this resource"));
  }
}
