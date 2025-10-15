package com.orama.e_commerce.controller;

import com.orama.e_commerce.dtos.client.ChangePasswordRequestDto;
import com.orama.e_commerce.dtos.client.ClientRequestDto;
import com.orama.e_commerce.dtos.client.ClientResponseDto;
import com.orama.e_commerce.dtos.client.ClientUpdateRequestDto;
import com.orama.e_commerce.service.ClientService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/clients")
public class ClientController {

  private final ClientService clientService;

  public ClientController(ClientService clientService) {
    this.clientService = clientService;
  }

  @PostMapping
  public ResponseEntity<ClientResponseDto> createClient(
      @Valid @RequestBody ClientRequestDto requestDto) {
    ClientResponseDto dto = clientService.createClient(requestDto);
    return ResponseEntity.status(HttpStatus.CREATED).body(dto);
  }

  @PutMapping("/{id}")
  public ResponseEntity<ClientResponseDto> updateClient(
      @PathVariable Long id, @Valid @RequestBody ClientUpdateRequestDto requestDto) {
    ClientResponseDto dto = clientService.updateClient(id, requestDto);
    return ResponseEntity.status(HttpStatus.OK).body(dto);
  }

  @PatchMapping("/{id}/password")
  public ResponseEntity<Void> updatePassword(
      @PathVariable Long id, @Valid @RequestBody ChangePasswordRequestDto dto) {
    clientService.updatePassword(id, dto);
    return ResponseEntity.noContent().build();
  }

  @PatchMapping("/{id}/deactivate")
  public ResponseEntity<Void> deactivateClient(@PathVariable Long id) {
    clientService.deactivateClient(id);
    return ResponseEntity.noContent().build();
  }
}
