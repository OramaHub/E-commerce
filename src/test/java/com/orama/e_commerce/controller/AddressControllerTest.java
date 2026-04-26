package com.orama.e_commerce.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.orama.e_commerce.dtos.address.AddressRequestDto;
import com.orama.e_commerce.dtos.address.AddressResponseDto;
import com.orama.e_commerce.dtos.address.AddressUpdateRequestDto;
import com.orama.e_commerce.service.AddressService;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

@ExtendWith(MockitoExtension.class)
class AddressControllerTest {

  @Mock private AddressService addressService;
  @Mock private Authentication authentication;

  @InjectMocks private AddressController addressController;

  private AddressResponseDto addressResponseDto;

  @BeforeEach
  void setUp() {
    addressResponseDto =
        new AddressResponseDto(
            1L,
            "Rua das Flores",
            "123",
            "Apt 45",
            "Centro",
            "01234-567",
            false,
            1L,
            "Sao Paulo",
            1L,
            Instant.parse("2026-01-01T00:00:00Z"),
            Instant.parse("2026-01-01T00:00:00Z"));
  }

  @Test
  void shouldResolveClientIdFromAuthenticationDetailsMapWhenListingAddresses() {
    when(authentication.getDetails()).thenReturn(Map.of("id", 1L));
    when(addressService.getAddressesByClient(1L)).thenReturn(List.of(addressResponseDto));

    ResponseEntity<List<AddressResponseDto>> response =
        addressController.getMyAddresses(authentication);

    assertEquals(200, response.getStatusCode().value());
    assertNotNull(response.getBody());
    assertEquals(1, response.getBody().size());
    verify(addressService).getAddressesByClient(1L);
  }

  @Test
  void shouldResolveClientIdFromAuthenticationDetailsMapWhenCreatingAddress() {
    when(authentication.getDetails()).thenReturn(Map.of("id", 1L));
    when(addressService.createAddress(any(AddressRequestDto.class), eq(1L)))
        .thenReturn(addressResponseDto);

    AddressRequestDto requestDto =
        new AddressRequestDto("Rua das Flores", "123", "Apt 45", "Centro", "01234-567", 1L, false);

    ResponseEntity<AddressResponseDto> response =
        addressController.createAddress(requestDto, authentication);

    assertEquals(201, response.getStatusCode().value());
    assertNotNull(response.getBody());
    assertEquals(1L, response.getBody().clientId());
    verify(addressService).createAddress(requestDto, 1L);
  }

  @Test
  void shouldResolveClientIdFromAuthenticationDetailsMapWhenUpdatingAddress() {
    when(authentication.getDetails()).thenReturn(Map.of("id", 1L));
    when(addressService.updateAddress(eq(1L), any(AddressUpdateRequestDto.class), eq(1L)))
        .thenReturn(addressResponseDto);

    AddressUpdateRequestDto requestDto =
        new AddressUpdateRequestDto(
            "Rua Nova", "456", "Casa", "Bairro Novo", "98765-432", 2L, false);

    ResponseEntity<AddressResponseDto> response =
        addressController.updateAddress(1L, requestDto, authentication);

    assertEquals(200, response.getStatusCode().value());
    assertNotNull(response.getBody());
    verify(addressService).updateAddress(1L, requestDto, 1L);
  }
}
