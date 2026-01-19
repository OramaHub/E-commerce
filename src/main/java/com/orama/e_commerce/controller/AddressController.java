package com.orama.e_commerce.controller;

import com.orama.e_commerce.dtos.address.AddressRequestDto;
import com.orama.e_commerce.dtos.address.AddressResponseDto;
import com.orama.e_commerce.dtos.address.AddressUpdateRequestDto;
import com.orama.e_commerce.service.AddressService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/addresses")
public class AddressController {

  private final AddressService addressService;

  public AddressController(AddressService addressService) {
    this.addressService = addressService;
  }

  @PreAuthorize("authentication.details != null")
  @PostMapping
  public ResponseEntity<AddressResponseDto> createAddress(
          @Valid @RequestBody AddressRequestDto requestDto,
          Authentication authentication) {

    @SuppressWarnings("unchecked")
    Map<String, Object> details = (Map<String, Object>) authentication.getDetails();
    Long clientId = Long.valueOf(details.get("id").toString());

    AddressResponseDto dto = addressService.createAddress(requestDto, clientId);
    return ResponseEntity.status(HttpStatus.CREATED).body(dto);
  }

  @PreAuthorize("@addressService.isOwner(#id, authentication.details['id']) or hasRole('ADMIN')")
  @PutMapping("/{id}")
  public ResponseEntity<AddressResponseDto> updateAddress(
          @PathVariable Long id,
          @Valid @RequestBody AddressUpdateRequestDto requestDto,
          Authentication authentication) {

    Long clientId = (Long) authentication.getDetails();
    AddressResponseDto dto = addressService.updateAddress(id, requestDto, clientId);
    return ResponseEntity.ok(dto);
  }

  @PreAuthorize("@addressService.isOwner(#id, authentication.details['id']) or hasRole('ADMIN')")
  @GetMapping("/{id}")
  public ResponseEntity<AddressResponseDto> getAddressById(
          @PathVariable Long id,
          Authentication authentication) {

    Long clientId = (Long) authentication.getDetails();
    AddressResponseDto dto = addressService.getAddressById(id, clientId);
    return ResponseEntity.ok(dto);
  }

  @PreAuthorize("authentication.details != null")
  @GetMapping
  public ResponseEntity<List<AddressResponseDto>> getMyAddresses(Authentication authentication) {

    Long clientId = (Long) authentication.getDetails();
    List<AddressResponseDto> addresses = addressService.getAddressesByClient(clientId);
    return ResponseEntity.ok(addresses);
  }

  @PreAuthorize("authentication.details != null")
  @GetMapping("/default")
  public ResponseEntity<AddressResponseDto> getDefaultAddress(Authentication authentication) {

    Long clientId = (Long) authentication.getDetails();
    AddressResponseDto dto = addressService.getDefaultAddress(clientId);
    return ResponseEntity.ok(dto);
  }

  @PreAuthorize("@addressService.isOwner(#id, authentication.details['id']) or hasRole('ADMIN')")
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteAddress(
          @PathVariable Long id,
          Authentication authentication) {

    Long clientId = (Long) authentication.getDetails();
    addressService.deleteAddress(id, clientId);
    return ResponseEntity.noContent().build();
  }

  @PreAuthorize("@addressService.isOwner(#id, authentication.details['id']) or hasRole('ADMIN')")
  @PatchMapping("/{id}/set-default")
  public ResponseEntity<Void> setDefaultAddress(
          @PathVariable Long id,
          Authentication authentication) {

    Long clientId = (Long) authentication.getDetails();
    addressService.setDefaultAddress(id, clientId);
    return ResponseEntity.noContent().build();
  }
}
