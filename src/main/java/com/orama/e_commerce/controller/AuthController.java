package com.orama.e_commerce.controller;

import com.orama.e_commerce.dtos.auth.AuthRegisterResponseDto;
import com.orama.e_commerce.dtos.auth.AuthResponseDto;
import com.orama.e_commerce.dtos.auth.LoginRequestDto;
import com.orama.e_commerce.dtos.client.ClientRequestDto;
import com.orama.e_commerce.dtos.refresh_token.RefreshTokenRequestDto;
import com.orama.e_commerce.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Autenticação")
public class AuthController {

  private final AuthService authService;

  public AuthController(AuthService authService) {
    this.authService = authService;
  }

  @PostMapping("/login")
  @Operation(summary = "Realiza login e retorna token JWT")
  public ResponseEntity<AuthResponseDto> login(@Valid @RequestBody LoginRequestDto loginRequest) {
    AuthResponseDto response = authService.login(loginRequest);
    return ResponseEntity.ok(response);
  }

  @PostMapping("/register")
  @Operation(summary = "Registra um novo cliente")
  public ResponseEntity<AuthRegisterResponseDto> register(
      @RequestBody @Valid ClientRequestDto registerDto) {
    AuthRegisterResponseDto tokenResponse = authService.register(registerDto);
    return ResponseEntity.status(HttpStatus.CREATED).body(tokenResponse);
  }

  @PostMapping("/refresh")
  @Operation(summary = "Renova o access token usando o refresh token")
  public ResponseEntity<AuthResponseDto> refresh(
      @Valid @RequestBody RefreshTokenRequestDto request) {
    AuthResponseDto response = authService.refresh(request);
    return ResponseEntity.ok(response);
  }

  @PostMapping("/logout")
  @PreAuthorize("isAuthenticated()")
  @Operation(summary = "Invalida o access token e o refresh token (logout)")
  public ResponseEntity<Void> logout(
      @RequestHeader("Authorization") String authHeader,
      @Valid @RequestBody RefreshTokenRequestDto request) {
    String accessToken = authHeader.substring(7);
    authService.logout(accessToken, request);
    return ResponseEntity.noContent().build();
  }
}
