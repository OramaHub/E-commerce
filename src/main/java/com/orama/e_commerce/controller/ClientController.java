package com.orama.e_commerce.controller;

import com.orama.e_commerce.dtos.client.ChangePasswordRequestDto;
import com.orama.e_commerce.dtos.client.ClientResponseDto;
import com.orama.e_commerce.dtos.client.ClientUpdateRequestDto;
import com.orama.e_commerce.service.ClientService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/clients")
@Tag(name = "Clientes")
public class ClientController {

  private final ClientService clientService;

  public ClientController(ClientService clientService) {
    this.clientService = clientService;
  }

  @PreAuthorize("#id == authentication.details['id'] or hasRole('ADMIN')")
  @GetMapping("/{id}")
  @Operation(summary = "Busca cliente pelo id")
  public ResponseEntity<ClientResponseDto> findById(@PathVariable Long id) {
    ClientResponseDto clientResponseDto = clientService.getById(id);
    return new ResponseEntity<>(clientResponseDto, HttpStatus.OK);
  }

  @PreAuthorize("hasRole('ADMIN')")
  @GetMapping
  @Operation(summary = "Lista clientes ativos paginados")
  public ResponseEntity<Page<ClientResponseDto>> findAllActiveClients(Pageable pageable) {
    Page<ClientResponseDto> page = clientService.getAllActiveClients(pageable);
    return new ResponseEntity<>(page, HttpStatus.OK);
  }

  @PreAuthorize("hasRole('ADMIN')")
  @GetMapping("/email")
  @Operation(summary = "Busca cliente pelo e-mail")
  public ResponseEntity<ClientResponseDto> findByEmail(@RequestParam String email) {
    ClientResponseDto dto = clientService.getByEmail(email);
    return new ResponseEntity<>(dto, HttpStatus.OK);
  }

  @PreAuthorize("hasRole('ADMIN')")
  @GetMapping("/cpf")
  @Operation(summary = "Busca cliente pelo CPF")
  public ResponseEntity<ClientResponseDto> findByCpf(@RequestParam String cpf) {
    ClientResponseDto dto = clientService.getByCpf(cpf);
    return new ResponseEntity<>(dto, HttpStatus.OK);
  }

  @PreAuthorize("hasRole('ADMIN')")
  @GetMapping("/role")
  @Operation(summary = "Lista clientes por papel (role)")
  public ResponseEntity<Page<ClientResponseDto>> findAllByRole(
      @RequestParam String role, Pageable pageable) {
    Page<ClientResponseDto> page = clientService.getAllByRole(role, pageable);
    return new ResponseEntity<>(page, HttpStatus.OK);
  }

  @PreAuthorize("#id == authentication.details['id'] or hasRole('ADMIN')")
  @PutMapping("/{id}")
  @Operation(summary = "Atualiza dados do cliente")
  public ResponseEntity<ClientResponseDto> updateClient(
      @PathVariable Long id, @Valid @RequestBody ClientUpdateRequestDto requestDto) {
    ClientResponseDto dto = clientService.updateClient(id, requestDto);
    return ResponseEntity.status(HttpStatus.OK).body(dto);
  }

  @PreAuthorize("#id == authentication.details['id'] or hasRole('ADMIN')")
  @PatchMapping("/{id}/password")
  @Operation(summary = "Altera senha do cliente")
  public ResponseEntity<Void> updatePassword(
      @PathVariable Long id, @Valid @RequestBody ChangePasswordRequestDto dto) {
    clientService.updatePassword(id, dto);
    return ResponseEntity.noContent().build();
  }

  @PreAuthorize("#id == authentication.details['id'] or hasRole('ADMIN')")
  @PatchMapping("/{id}/deactivate")
  @Operation(summary = "Desativa um cliente")
  public ResponseEntity<Void> deactivateClient(@PathVariable Long id) {
    clientService.deactivateClient(id);
    return ResponseEntity.noContent().build();
  }

  @PreAuthorize("#id == authentication.details['id'] or hasRole('ADMIN')")
  @PatchMapping("/{id}/activate")
  @Operation(summary = "Reativa um cliente")
  public ResponseEntity<Void> activateClient(@PathVariable Long id) {
    clientService.activateClient(id);
    return ResponseEntity.noContent().build();
  }
}
