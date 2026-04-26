package com.orama.e_commerce.controller;

import com.orama.e_commerce.dtos.address.AddressRequestDto;
import com.orama.e_commerce.dtos.address.AddressResponseDto;
import com.orama.e_commerce.dtos.address.AddressUpdateRequestDto;
import com.orama.e_commerce.service.AddressService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/addresses")
@Tag(name = "Endereços")
public class AddressController {

  private final AddressService addressService;

  public AddressController(AddressService addressService) {
    this.addressService = addressService;
  }

  @PreAuthorize("authentication.details != null")
  @PostMapping
  @Operation(summary = "Cria um endereço para o cliente autenticado")
  public ResponseEntity<AddressResponseDto> createAddress(
      @Valid @RequestBody AddressRequestDto requestDto, Authentication authentication) {

    Long clientId = getAuthenticatedClientId(authentication);
    AddressResponseDto dto = addressService.createAddress(requestDto, clientId);
    return ResponseEntity.status(HttpStatus.CREATED).body(dto);
  }

  @PreAuthorize("@addressService.isOwner(#id, authentication.details['id']) or hasRole('ADMIN')")
  @PutMapping("/{id}")
  @Operation(summary = "Atualiza um endereço do cliente")
  public ResponseEntity<AddressResponseDto> updateAddress(
      @PathVariable Long id,
      @Valid @RequestBody AddressUpdateRequestDto requestDto,
      Authentication authentication) {

    Long clientId = getAuthenticatedClientId(authentication);
    AddressResponseDto dto = addressService.updateAddress(id, requestDto, clientId);
    return ResponseEntity.ok(dto);
  }

  @PreAuthorize("@addressService.isOwner(#id, authentication.details['id']) or hasRole('ADMIN')")
  @GetMapping("/{id}")
  @Operation(summary = "Busca endereço pelo id")
  public ResponseEntity<AddressResponseDto> getAddressById(
      @PathVariable Long id, Authentication authentication) {

    Long clientId = getAuthenticatedClientId(authentication);
    AddressResponseDto dto = addressService.getAddressById(id, clientId);
    return ResponseEntity.ok(dto);
  }

  @PreAuthorize("authentication.details != null")
  @GetMapping
  @Operation(summary = "Lista endereços do cliente autenticado")
  public ResponseEntity<List<AddressResponseDto>> getMyAddresses(Authentication authentication) {

    Long clientId = getAuthenticatedClientId(authentication);
    List<AddressResponseDto> addresses = addressService.getAddressesByClient(clientId);
    return ResponseEntity.ok(addresses);
  }

  @PreAuthorize("authentication.details != null")
  @GetMapping("/default")
  @Operation(summary = "Busca endereço padrão do cliente autenticado")
  public ResponseEntity<AddressResponseDto> getDefaultAddress(Authentication authentication) {

    Long clientId = getAuthenticatedClientId(authentication);
    AddressResponseDto dto = addressService.getDefaultAddress(clientId);
    return ResponseEntity.ok(dto);
  }

  @PreAuthorize("@addressService.isOwner(#id, authentication.details['id']) or hasRole('ADMIN')")
  @DeleteMapping("/{id}")
  @Operation(summary = "Remove um endereço")
  public ResponseEntity<Void> deleteAddress(@PathVariable Long id, Authentication authentication) {

    Long clientId = getAuthenticatedClientId(authentication);
    addressService.deleteAddress(id, clientId);
    return ResponseEntity.noContent().build();
  }

  @PreAuthorize("@addressService.isOwner(#id, authentication.details['id']) or hasRole('ADMIN')")
  @PatchMapping("/{id}/set-default")
  @Operation(summary = "Define um endereço como padrão")
  public ResponseEntity<Void> setDefaultAddress(
      @PathVariable Long id, Authentication authentication) {

    Long clientId = getAuthenticatedClientId(authentication);
    addressService.setDefaultAddress(id, clientId);
    return ResponseEntity.noContent().build();
  }

  private Long getAuthenticatedClientId(Authentication authentication) {
    Object details = authentication.getDetails();

    if (details instanceof Map<?, ?> detailsMap) {
      Object clientId = detailsMap.get("id");
      if (clientId != null) {
        return Long.valueOf(clientId.toString());
      }
    }

    if (details instanceof Number number) {
      return number.longValue();
    }

    if (details instanceof String value && !value.isBlank()) {
      return Long.valueOf(value);
    }

    throw new IllegalStateException("Não foi possível identificar o cliente autenticado.");
  }
}
