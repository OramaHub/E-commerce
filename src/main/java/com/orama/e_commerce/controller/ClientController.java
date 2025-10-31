package com.orama.e_commerce.controller;

import com.orama.e_commerce.dtos.client.ChangePasswordRequestDto;
import com.orama.e_commerce.dtos.client.ClientResponseDto;
import com.orama.e_commerce.dtos.client.ClientUpdateRequestDto;
import com.orama.e_commerce.service.ClientService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
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
@Tag(name = "Clientes", description = "Gestão de clientes e acesso")
@SecurityRequirement(name = "bearerAuth")
public class ClientController {

  private final ClientService clientService;

  public ClientController(ClientService clientService) {
    this.clientService = clientService;
  }

  @PreAuthorize("#id == authentication.details['id'] or hasRole('ADMIN')")
  @GetMapping("/{id}")
  @Operation(
      summary = "Buscar cliente por ID",
      description = "Acessível para o próprio cliente ou ADMIN")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Cliente encontrado"),
        @ApiResponse(responseCode = "401", description = "Não autenticado"),
        @ApiResponse(responseCode = "403", description = "Sem permissão"),
        @ApiResponse(responseCode = "404", description = "Cliente não encontrado")
      })
  public ResponseEntity<ClientResponseDto> findById(@PathVariable Long id) {
    ClientResponseDto clientResponseDto = clientService.getById(id);
    return new ResponseEntity<>(clientResponseDto, HttpStatus.OK);
  }

  @PreAuthorize("hasRole('ADMIN')")
  @GetMapping
  @Operation(summary = "Listar clientes ativos", description = "Disponível apenas para ADMIN")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Lista retornada"),
        @ApiResponse(responseCode = "401", description = "Não autenticado"),
        @ApiResponse(responseCode = "403", description = "Sem permissão")
      })
  public ResponseEntity<Page<ClientResponseDto>> findAllActiveClients(Pageable pageable) {
    Page<ClientResponseDto> page = clientService.getAllActiveClients(pageable);
    return new ResponseEntity<>(page, HttpStatus.OK);
  }

  @PreAuthorize("hasRole('ADMIN')")
  @GetMapping("/email")
  @Operation(summary = "Buscar cliente por e-mail", description = "Disponível para ADMIN")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Cliente encontrado"),
        @ApiResponse(responseCode = "401", description = "Não autenticado"),
        @ApiResponse(responseCode = "403", description = "Sem permissão"),
        @ApiResponse(responseCode = "404", description = "Cliente não encontrado")
      })
  public ResponseEntity<ClientResponseDto> findByEmail(@RequestParam String email) {
    ClientResponseDto dto = clientService.getByEmail(email);
    return new ResponseEntity<>(dto, HttpStatus.OK);
  }

  @PreAuthorize("hasRole('ADMIN')")
  @GetMapping("/cpf")
  @Operation(summary = "Buscar cliente por CPF", description = "Disponível para ADMIN")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Cliente encontrado"),
        @ApiResponse(responseCode = "401", description = "Não autenticado"),
        @ApiResponse(responseCode = "403", description = "Sem permissão"),
        @ApiResponse(responseCode = "404", description = "Cliente não encontrado")
      })
  public ResponseEntity<ClientResponseDto> findByCpf(@RequestParam String cpf) {
    ClientResponseDto dto = clientService.getByCpf(cpf);
    return new ResponseEntity<>(dto, HttpStatus.OK);
  }

  @PreAuthorize("hasRole('ADMIN')")
  @GetMapping("/role")
  @Operation(summary = "Listar clientes por papel", description = "Filtra clientes por role")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Lista retornada"),
        @ApiResponse(responseCode = "401", description = "Não autenticado"),
        @ApiResponse(responseCode = "403", description = "Sem permissão")
      })
  public ResponseEntity<Page<ClientResponseDto>> findAllByRole(
      @RequestParam String role, Pageable pageable) {
    Page<ClientResponseDto> page = clientService.getAllByRole(role, pageable);
    return new ResponseEntity<>(page, HttpStatus.OK);
  }

  @PreAuthorize("#id == authentication.details['id'] or hasRole('ADMIN')")
  @PutMapping("/{id}")
  @Operation(
      summary = "Atualizar cliente",
      description = "Atualiza dados do próprio cliente ou ADMIN")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Cliente atualizado"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos"),
        @ApiResponse(responseCode = "401", description = "Não autenticado"),
        @ApiResponse(responseCode = "403", description = "Sem permissão"),
        @ApiResponse(responseCode = "404", description = "Cliente não encontrado")
      })
  public ResponseEntity<ClientResponseDto> updateClient(
      @PathVariable Long id, @Valid @RequestBody ClientUpdateRequestDto requestDto) {
    ClientResponseDto dto = clientService.updateClient(id, requestDto);
    return ResponseEntity.status(HttpStatus.OK).body(dto);
  }

  @PreAuthorize("#id == authentication.details['id'] or hasRole('ADMIN')")
  @PatchMapping("/{id}/password")
  @Operation(summary = "Atualizar senha", description = "Permite que o cliente altere sua senha")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "204", description = "Senha atualizada"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos"),
        @ApiResponse(responseCode = "401", description = "Não autenticado"),
        @ApiResponse(responseCode = "403", description = "Sem permissão"),
        @ApiResponse(responseCode = "404", description = "Cliente não encontrado")
      })
  public ResponseEntity<Void> updatePassword(
      @PathVariable Long id, @Valid @RequestBody ChangePasswordRequestDto dto) {
    clientService.updatePassword(id, dto);
    return ResponseEntity.noContent().build();
  }

  @PreAuthorize("#id == authentication.details['id'] or hasRole('ADMIN')")
  @PatchMapping("/{id}/deactivate")
  @Operation(
      summary = "Desativar cliente",
      description = "Desativa conta do cliente autenticado ou ADMIN")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "204", description = "Cliente desativado"),
        @ApiResponse(responseCode = "401", description = "Não autenticado"),
        @ApiResponse(responseCode = "403", description = "Sem permissão"),
        @ApiResponse(responseCode = "404", description = "Cliente não encontrado")
      })
  public ResponseEntity<Void> deactivateClient(@PathVariable Long id) {
    clientService.deactivateClient(id);
    return ResponseEntity.noContent().build();
  }

  @PreAuthorize("#id == authentication.details['id'] or hasRole('ADMIN')")
  @PatchMapping("/{id}/activate")
  @Operation(
      summary = "Reativar cliente",
      description = "Reativa conta do cliente autenticado ou ADMIN")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "204", description = "Cliente reativado"),
        @ApiResponse(responseCode = "401", description = "Não autenticado"),
        @ApiResponse(responseCode = "403", description = "Sem permissão"),
        @ApiResponse(responseCode = "404", description = "Cliente não encontrado")
      })
  public ResponseEntity<Void> activateClient(@PathVariable Long id) {
    clientService.activateClient(id);
    return ResponseEntity.noContent().build();
  }
}
