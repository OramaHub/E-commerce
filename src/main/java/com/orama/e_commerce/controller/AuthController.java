package com.orama.e_commerce.controller;

import com.orama.e_commerce.dtos.auth.AuthResponseDto;
import com.orama.e_commerce.dtos.auth.LoginRequestDto;
import com.orama.e_commerce.dtos.client.ClientRequestDto;
import com.orama.e_commerce.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Auth", description = "Autenticação e registro de usuários")
public class AuthController {

  private final AuthService authService;

  public AuthController(AuthService authService) {
    this.authService = authService;
  }

  @PostMapping("/login")
  @Operation(
      summary = "Autenticar usuário",
      description = "Retorna um token JWT para acesso às rotas protegidas")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Login realizado com sucesso"),
        @ApiResponse(responseCode = "400", description = "Requisição inválida"),
        @ApiResponse(responseCode = "401", description = "Credenciais inválidas")
      })
  public ResponseEntity<AuthResponseDto> login(@Valid @RequestBody LoginRequestDto loginRequest) {
    AuthResponseDto response = authService.login(loginRequest);
    return ResponseEntity.ok(response);
  }

  @PostMapping("/register")
  @Operation(summary = "Registrar cliente", description = "Cria um cliente e retorna o token JWT")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "201", description = "Cliente registrado com sucesso"),
        @ApiResponse(responseCode = "400", description = "Dados de registro inválidos")
      })
  public ResponseEntity<AuthResponseDto> register(
      @RequestBody @Valid ClientRequestDto registerDto) {
    AuthResponseDto tokenResponse = authService.register(registerDto);
    return ResponseEntity.status(HttpStatus.CREATED).body(tokenResponse);
  }
}
