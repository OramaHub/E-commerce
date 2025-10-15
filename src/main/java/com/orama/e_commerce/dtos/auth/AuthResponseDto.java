package com.orama.e_commerce.dtos.auth;

public record AuthResponseDto(String token, String type, Long expiresIn) {
  public AuthResponseDto(String token, Long expiresIn) {
    this(token, "Bearer", expiresIn);
  }
}
