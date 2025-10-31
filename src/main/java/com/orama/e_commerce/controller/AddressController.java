package com.orama.e_commerce.controller;

import com.orama.e_commerce.dtos.address.AddressRequestDto;
import com.orama.e_commerce.dtos.address.AddressResponseDto;
import com.orama.e_commerce.dtos.address.AddressUpdateRequestDto;
import com.orama.e_commerce.security.JwtService;
import com.orama.e_commerce.service.AddressService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/addresses")
public class AddressController {

  private final AddressService addressService;
  private final JwtService jwtService;

  public AddressController(AddressService addressService, JwtService jwtService) {
    this.addressService = addressService;
    this.jwtService = jwtService;
  }

  private Long extractClientId(String authorizationHeader) {
    if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
      throw new RuntimeException("Invalid authorization header");
    }
    String token = authorizationHeader.substring(7);
    return jwtService.extractUserId(token);
  }

  @PreAuthorize("isAuthenticated()")
  @PostMapping
  public ResponseEntity<AddressResponseDto> createAddress(
      @Valid @RequestBody AddressRequestDto requestDto,
      @RequestHeader("Authorization") String authorizationHeader) {
    Long clientId = extractClientId(authorizationHeader);
    AddressResponseDto dto = addressService.createAddress(requestDto, clientId);
    return ResponseEntity.status(HttpStatus.CREATED).body(dto);
  }

  @PreAuthorize("isAuthenticated()")
  @PutMapping("/{id}")
  public ResponseEntity<AddressResponseDto> updateAddress(
      @PathVariable Long id,
      @Valid @RequestBody AddressUpdateRequestDto requestDto,
      @RequestHeader("Authorization") String authorizationHeader) {
    Long clientId = extractClientId(authorizationHeader);
    AddressResponseDto dto = addressService.updateAddress(id, requestDto, clientId);
    return ResponseEntity.ok(dto);
  }

  @PreAuthorize("isAuthenticated()")
  @GetMapping("/{id}")
  public ResponseEntity<AddressResponseDto> getAddressById(
      @PathVariable Long id, @RequestHeader("Authorization") String authorizationHeader) {
    Long clientId = extractClientId(authorizationHeader);
    AddressResponseDto dto = addressService.getAddressById(id, clientId);
    return ResponseEntity.ok(dto);
  }

  @PreAuthorize("isAuthenticated()")
  @GetMapping
  public ResponseEntity<List<AddressResponseDto>> getMyAddresses(
      @RequestHeader("Authorization") String authorizationHeader) {
    Long clientId = extractClientId(authorizationHeader);
    List<AddressResponseDto> addresses = addressService.getAddressesByClient(clientId);
    return ResponseEntity.ok(addresses);
  }

  @PreAuthorize("isAuthenticated()")
  @GetMapping("/default")
  public ResponseEntity<AddressResponseDto> getDefaultAddress(
      @RequestHeader("Authorization") String authorizationHeader) {
    Long clientId = extractClientId(authorizationHeader);
    AddressResponseDto dto = addressService.getDefaultAddress(clientId);
    return ResponseEntity.ok(dto);
  }

  @PreAuthorize("isAuthenticated()")
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteAddress(
      @PathVariable Long id, @RequestHeader("Authorization") String authorizationHeader) {
    Long clientId = extractClientId(authorizationHeader);
    addressService.deleteAddress(id, clientId);
    return ResponseEntity.noContent().build();
  }

  @PreAuthorize("isAuthenticated()")
  @PatchMapping("/{id}/set-default")
  public ResponseEntity<Void> setDefaultAddress(
      @PathVariable Long id, @RequestHeader("Authorization") String authorizationHeader) {
    Long clientId = extractClientId(authorizationHeader);
    addressService.setDefaultAddress(id, clientId);
    return ResponseEntity.noContent().build();
  }
}
