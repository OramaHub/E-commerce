package com.orama.e_commerce.controller;

import com.orama.e_commerce.dtos.client.AdminPasswordResetDto;
import com.orama.e_commerce.dtos.client.ClientRequestDto;
import com.orama.e_commerce.dtos.client.ClientResponseDto;
import com.orama.e_commerce.service.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admins")
@Tag(name = "Admin")
public class AdminController {

  private final AdminService adminService;

  public AdminController(AdminService adminService) {
    this.adminService = adminService;
  }

  @PreAuthorize("hasRole('ADMIN')")
  @PostMapping
  @Operation(summary = "Cria um novo administrador")
  public ResponseEntity<ClientResponseDto> createAdmin(@Valid @RequestBody ClientRequestDto dto) {
    ClientResponseDto response = adminService.createAdmin(dto);
    return ResponseEntity.ok(response);
  }

  @PreAuthorize("hasRole('ADMIN')")
  @PatchMapping("/clients/{id}/reset-password")
  @Operation(summary = "Reseta a senha de um cliente")
  public ResponseEntity<Void> resetClientPassword(
      @PathVariable Long id, @Valid @RequestBody AdminPasswordResetDto dto) {
    adminService.resetClientPassword(id, dto);
    return ResponseEntity.noContent().build();
  }
}
