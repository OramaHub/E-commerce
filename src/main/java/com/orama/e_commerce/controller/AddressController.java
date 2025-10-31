package com.orama.e_commerce.controller;

import com.orama.e_commerce.dtos.address.AddressRequestDto;
import com.orama.e_commerce.dtos.address.AddressResponseDto;
import com.orama.e_commerce.dtos.address.AddressUpdateRequestDto;
import com.orama.e_commerce.security.JwtService;
import com.orama.e_commerce.service.AddressService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/addresses")
@Tag(name = "Endereços", description = "Gerenciamento de endereços do cliente autenticado")
@SecurityRequirement(name = "bearerAuth")
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
  @Operation(
      summary = "Criar endereço",
      description = "Cria um endereço para o cliente autenticado")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "201", description = "Endereço criado"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos"),
        @ApiResponse(responseCode = "401", description = "Não autenticado")
      })
  public ResponseEntity<AddressResponseDto> createAddress(
      @Valid @RequestBody AddressRequestDto requestDto,
      @Parameter(hidden = true) @RequestHeader("Authorization") String authorizationHeader) {
    Long clientId = extractClientId(authorizationHeader);
    AddressResponseDto dto = addressService.createAddress(requestDto, clientId);
    return ResponseEntity.status(HttpStatus.CREATED).body(dto);
  }

  @PreAuthorize("isAuthenticated()")
  @PutMapping("/{id}")
  @Operation(summary = "Atualizar endereço")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Endereço atualizado"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos"),
        @ApiResponse(responseCode = "401", description = "Não autenticado"),
        @ApiResponse(responseCode = "403", description = "Sem permissão"),
        @ApiResponse(responseCode = "404", description = "Endereço não encontrado")
      })
  public ResponseEntity<AddressResponseDto> updateAddress(
      @PathVariable Long id,
      @Valid @RequestBody AddressUpdateRequestDto requestDto,
      @Parameter(hidden = true) @RequestHeader("Authorization") String authorizationHeader) {
    Long clientId = extractClientId(authorizationHeader);
    AddressResponseDto dto = addressService.updateAddress(id, requestDto, clientId);
    return ResponseEntity.ok(dto);
  }

  @PreAuthorize("isAuthenticated()")
  @GetMapping("/{id}")
  @Operation(summary = "Buscar endereço por ID")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Endereço retornado"),
        @ApiResponse(responseCode = "401", description = "Não autenticado"),
        @ApiResponse(responseCode = "403", description = "Sem permissão"),
        @ApiResponse(responseCode = "404", description = "Endereço não encontrado")
      })
  public ResponseEntity<AddressResponseDto> getAddressById(
      @PathVariable Long id,
      @Parameter(hidden = true) @RequestHeader("Authorization") String authorizationHeader) {
    Long clientId = extractClientId(authorizationHeader);
    AddressResponseDto dto = addressService.getAddressById(id, clientId);
    return ResponseEntity.ok(dto);
  }

  @PreAuthorize("isAuthenticated()")
  @GetMapping
  @Operation(summary = "Listar endereços do cliente")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Lista de endereços retornada"),
        @ApiResponse(responseCode = "401", description = "Não autenticado")
      })
  public ResponseEntity<List<AddressResponseDto>> getMyAddresses(
      @Parameter(hidden = true) @RequestHeader("Authorization") String authorizationHeader) {
    Long clientId = extractClientId(authorizationHeader);
    List<AddressResponseDto> addresses = addressService.getAddressesByClient(clientId);
    return ResponseEntity.ok(addresses);
  }

  @PreAuthorize("isAuthenticated()")
  @GetMapping("/default")
  @Operation(summary = "Buscar endereço padrão do cliente")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Endereço padrão retornado"),
        @ApiResponse(responseCode = "401", description = "Não autenticado"),
        @ApiResponse(responseCode = "404", description = "Endereço padrão não encontrado")
      })
  public ResponseEntity<AddressResponseDto> getDefaultAddress(
      @Parameter(hidden = true) @RequestHeader("Authorization") String authorizationHeader) {
    Long clientId = extractClientId(authorizationHeader);
    AddressResponseDto dto = addressService.getDefaultAddress(clientId);
    return ResponseEntity.ok(dto);
  }

  @PreAuthorize("isAuthenticated()")
  @DeleteMapping("/{id}")
  @Operation(summary = "Excluir endereço")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "204", description = "Endereço removido"),
        @ApiResponse(responseCode = "401", description = "Não autenticado"),
        @ApiResponse(responseCode = "403", description = "Sem permissão"),
        @ApiResponse(responseCode = "404", description = "Endereço não encontrado")
      })
  public ResponseEntity<Void> deleteAddress(
      @PathVariable Long id,
      @Parameter(hidden = true) @RequestHeader("Authorization") String authorizationHeader) {
    Long clientId = extractClientId(authorizationHeader);
    addressService.deleteAddress(id, clientId);
    return ResponseEntity.noContent().build();
  }

  @PreAuthorize("isAuthenticated()")
  @PatchMapping("/{id}/set-default")
  @Operation(summary = "Definir endereço padrão")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "204", description = "Endereço definido como padrão"),
        @ApiResponse(responseCode = "401", description = "Não autenticado"),
        @ApiResponse(responseCode = "403", description = "Sem permissão"),
        @ApiResponse(responseCode = "404", description = "Endereço não encontrado")
      })
  public ResponseEntity<Void> setDefaultAddress(
      @PathVariable Long id,
      @Parameter(hidden = true) @RequestHeader("Authorization") String authorizationHeader) {
    Long clientId = extractClientId(authorizationHeader);
    addressService.setDefaultAddress(id, clientId);
    return ResponseEntity.noContent().build();
  }
}
