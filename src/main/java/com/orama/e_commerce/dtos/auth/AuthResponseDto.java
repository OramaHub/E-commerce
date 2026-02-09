package com.orama.e_commerce.dtos.auth;

public record AuthResponseDto(
    String accessToken, String refreshToken, String type, Long expiresIn) {
  public AuthResponseDto(String accessToken, String refreshToken, Long expiresIn) {
    this(accessToken, refreshToken, "Bearer", expiresIn);
  }
}
