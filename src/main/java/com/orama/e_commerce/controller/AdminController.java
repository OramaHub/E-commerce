package com.orama.e_commerce.controller;

import com.orama.e_commerce.dtos.client.AdminPasswordResetDto;
import com.orama.e_commerce.dtos.client.ClientRequestDto;
import com.orama.e_commerce.dtos.client.ClientResponseDto;
import com.orama.e_commerce.service.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admins")
@Tag(name = "Admin", description = "Administração e gerenciamento de clientes")
@SecurityRequirement(name = "bearerAuth")
public class AdminController {

  private final AdminService adminService;

  public AdminController(AdminService adminService) {
    this.adminService = adminService;
  }

  @PreAuthorize("hasRole('ADMIN')")
  @PostMapping
  @Operation(summary = "Criar administrador", description = "Cria um novo usuário com papel ADMIN")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Administrador criado"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos"),
        @ApiResponse(responseCode = "401", description = "Não autenticado"),
        @ApiResponse(responseCode = "403", description = "Sem permissão para criar admin")
      })
  public ResponseEntity<ClientResponseDto> createAdmin(@Valid @RequestBody ClientRequestDto dto) {
    ClientResponseDto response = adminService.createAdmin(dto);
    return ResponseEntity.ok(response);
  }

  @PreAuthorize("hasRole('ADMIN')")
  @PatchMapping("/clients/{id}/reset-password")
  @Operation(summary = "Resetar senha de cliente", description = "Permite ADMIN redefinir a senha")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "204", description = "Senha redefinida"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos"),
        @ApiResponse(responseCode = "401", description = "Não autenticado"),
        @ApiResponse(responseCode = "403", description = "Sem permissão"),
        @ApiResponse(responseCode = "404", description = "Cliente não encontrado")
      })
  public ResponseEntity<Void> resetClientPassword(
      @PathVariable Long id, @Valid @RequestBody AdminPasswordResetDto dto) {
    adminService.resetClientPassword(id, dto);
    return ResponseEntity.noContent().build();
  }
}
