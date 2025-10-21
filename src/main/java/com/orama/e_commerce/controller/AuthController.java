package com.orama.e_commerce.controller;

import com.orama.e_commerce.dtos.auth.AuthResponseDto;
import com.orama.e_commerce.dtos.auth.LoginRequestDto;
import com.orama.e_commerce.dtos.client.ClientRequestDto;
import com.orama.e_commerce.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

  private final AuthService authService;

  public AuthController(AuthService authService) {
    this.authService = authService;
  }

  @PostMapping("/login")
  public ResponseEntity<AuthResponseDto> login(@Valid @RequestBody LoginRequestDto loginRequest) {
    AuthResponseDto response = authService.login(loginRequest);
    return ResponseEntity.ok(response);
  }

  @PostMapping("/register")
  public ResponseEntity<AuthResponseDto> register(
      @RequestBody @Valid ClientRequestDto registerDto) {
    AuthResponseDto tokenResponse = authService.register(registerDto);
    return ResponseEntity.status(HttpStatus.CREATED).body(tokenResponse);
  }
}
