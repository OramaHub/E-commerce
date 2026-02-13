package com.orama.e_commerce.controller;

import com.orama.e_commerce.dtos.auth.AuthRegisterResponseDto;
import com.orama.e_commerce.dtos.auth.AuthResponseDto;
import com.orama.e_commerce.dtos.auth.ForgotPasswordRequestDto;
import com.orama.e_commerce.dtos.auth.LoginRequestDto;
import com.orama.e_commerce.dtos.auth.ResetPasswordRequestDto;
import com.orama.e_commerce.dtos.client.ClientRequestDto;
import com.orama.e_commerce.dtos.refresh_token.RefreshTokenRequestDto;
import com.orama.e_commerce.service.AuthService;
import com.orama.e_commerce.service.PasswordResetService;
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
  private final PasswordResetService passwordResetService;

  public AuthController(AuthService authService, PasswordResetService passwordResetService) {
    this.authService = authService;
    this.passwordResetService = passwordResetService;
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

  @PostMapping("/forgot-password")
  @Operation(summary = "Solicita redefinição de senha via email")
  public ResponseEntity<Void> forgotPassword(@Valid @RequestBody ForgotPasswordRequestDto request) {
    passwordResetService.requestPasswordReset(request.email());
    return ResponseEntity.ok().build();
  }

  @PostMapping("/reset-password")
  @Operation(summary = "Redefine a senha usando o token enviado por email")
  public ResponseEntity<Void> resetPassword(@Valid @RequestBody ResetPasswordRequestDto request) {
    passwordResetService.resetPassword(request.token(), request.newPassword());
    return ResponseEntity.ok().build();
  }
}
