package com.orama.e_commerce.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.orama.e_commerce.dtos.address.AddressRequestDto;
import com.orama.e_commerce.dtos.address.AddressResponseDto;
import com.orama.e_commerce.dtos.address.AddressUpdateRequestDto;
import com.orama.e_commerce.mapper.AddressMapper;
import com.orama.e_commerce.models.Address;
import com.orama.e_commerce.models.City;
import com.orama.e_commerce.models.Client;
import com.orama.e_commerce.repository.AddressRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AddressServiceTest {

  @Mock private AddressRepository addressRepository;
  @Mock private AddressMapper addressMapper;

  @InjectMocks private AddressService addressService;

  private Address address;
  private Client client;
  private City city;
  private AddressResponseDto addressResponseDto;

  @BeforeEach
  void setUp() {
    client = new Client();
    client.setId(1L);
    client.setName("João Silva");

    city = new City();
    city.setId(1L);
    city.setName("São Paulo");

    address = new Address();
    address.setId(1L);
    address.setStreet("Rua das Flores");
    address.setNumber("123");
    address.setComplement("Apt 45");
    address.setDistrict("Centro");
    address.setZipCode("01234-567");
    address.setClient(client);
    address.setCity(city);
    address.setDefaultAddress(false);

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
            "São Paulo",
            1L,
            Instant.now(),
            Instant.now());
  }

  @Test
  void shouldCreateAddress() {
    AddressRequestDto requestDto =
        new AddressRequestDto("Rua das Flores", "123", "Apt 45", "Centro", "01234-567", 1L, false);

    when(addressMapper.toEntity(requestDto)).thenReturn(address);
    when(addressRepository.save(any(Address.class))).thenReturn(address);
    when(addressMapper.toResponseDto(address)).thenReturn(addressResponseDto);

    AddressResponseDto result = addressService.createAddress(requestDto, 1L);

    assertNotNull(result);
    assertEquals("Rua das Flores", result.street());
    verify(addressRepository).save(any(Address.class));
  }

  @Test
  void shouldCreateAddressAsDefault() {
    AddressRequestDto requestDto =
        new AddressRequestDto("Rua das Flores", "123", "Apt 45", "Centro", "01234-567", 1L, true);

    Address existingDefault = new Address();
    existingDefault.setId(2L);
    existingDefault.setDefaultAddress(true);
    existingDefault.setClient(client);

    when(addressMapper.toEntity(requestDto)).thenReturn(address);
    when(addressRepository.findByClientIdAndDefaultAddressTrue(1L))
        .thenReturn(Optional.of(existingDefault));
    when(addressRepository.save(any(Address.class))).thenReturn(address);
    when(addressMapper.toResponseDto(address)).thenReturn(addressResponseDto);

    AddressResponseDto result = addressService.createAddress(requestDto, 1L);

    assertNotNull(result);
    verify(addressRepository, times(2)).save(any(Address.class));
  }

  @Test
  void shouldCreateAddressWithDefaultFalseWhenNull() {
    AddressRequestDto requestDto =
        new AddressRequestDto("Rua das Flores", "123", "Apt 45", "Centro", "01234-567", 1L, null);

    when(addressMapper.toEntity(requestDto)).thenReturn(address);
    when(addressRepository.save(any(Address.class))).thenReturn(address);
    when(addressMapper.toResponseDto(address)).thenReturn(addressResponseDto);

    AddressResponseDto result = addressService.createAddress(requestDto, 1L);

    assertNotNull(result);
    assertFalse(address.getDefaultAddress());
    verify(addressRepository).save(any(Address.class));
  }

  @Test
  void shouldUpdateAddress() {
    AddressUpdateRequestDto updateRequestDto =
        new AddressUpdateRequestDto(
            "Rua Nova", "456", "Casa", "Bairro Novo", "98765-432", 2L, false);

    when(addressRepository.findById(1L)).thenReturn(Optional.of(address));
    when(addressRepository.save(any(Address.class))).thenReturn(address);
    when(addressMapper.toResponseDto(address)).thenReturn(addressResponseDto);

    AddressResponseDto result = addressService.updateAddress(1L, updateRequestDto, 1L);

    assertNotNull(result);
    verify(addressMapper).updateEntity(updateRequestDto, address);
    verify(addressRepository).save(address);
  }

  @Test
  void shouldUpdateAddressAsDefault() {
    AddressUpdateRequestDto updateRequestDto =
        new AddressUpdateRequestDto(
            "Rua Nova", "456", "Casa", "Bairro Novo", "98765-432", null, true);

    Address existingDefault = new Address();
    existingDefault.setId(2L);
    existingDefault.setDefaultAddress(true);
    existingDefault.setClient(client);

    when(addressRepository.findById(1L)).thenReturn(Optional.of(address));
    when(addressRepository.findByClientIdAndDefaultAddressTrue(1L))
        .thenReturn(Optional.of(existingDefault));
    when(addressRepository.save(any(Address.class))).thenReturn(address);
    when(addressMapper.toResponseDto(address)).thenReturn(addressResponseDto);

    AddressResponseDto result = addressService.updateAddress(1L, updateRequestDto, 1L);

    assertNotNull(result);
    verify(addressRepository, times(2)).save(any(Address.class));
  }

  @Test
  void shouldThrowExceptionWhenUpdatingAddressOfAnotherClient() {
    AddressUpdateRequestDto updateRequestDto =
        new AddressUpdateRequestDto(
            "Rua Nova", "456", "Casa", "Bairro Novo", "98765-432", null, false);

    when(addressRepository.findById(1L)).thenReturn(Optional.of(address));

    assertThrows(
        RuntimeException.class, () -> addressService.updateAddress(1L, updateRequestDto, 2L));
  }

  @Test
  void shouldThrowExceptionWhenUpdatingNonExistentAddress() {
    AddressUpdateRequestDto updateRequestDto =
        new AddressUpdateRequestDto(
            "Rua Nova", "456", "Casa", "Bairro Novo", "98765-432", null, false);

    when(addressRepository.findById(99L)).thenReturn(Optional.empty());

    assertThrows(
        RuntimeException.class, () -> addressService.updateAddress(99L, updateRequestDto, 1L));
  }

  @Test
  void shouldGetAddressById() {
    when(addressRepository.findById(1L)).thenReturn(Optional.of(address));
    when(addressMapper.toResponseDto(address)).thenReturn(addressResponseDto);

    AddressResponseDto result = addressService.getAddressById(1L, 1L);

    assertNotNull(result);
    assertEquals(1L, result.id());
    verify(addressRepository).findById(1L);
  }

  @Test
  void shouldThrowExceptionWhenGettingAddressOfAnotherClient() {
    when(addressRepository.findById(1L)).thenReturn(Optional.of(address));

    assertThrows(RuntimeException.class, () -> addressService.getAddressById(1L, 2L));
  }

  @Test
  void shouldThrowExceptionWhenGettingNonExistentAddress() {
    when(addressRepository.findById(99L)).thenReturn(Optional.empty());

    assertThrows(RuntimeException.class, () -> addressService.getAddressById(99L, 1L));
  }

  @Test
  void shouldGetAddressesByClient() {
    when(addressRepository.findByClientId(1L)).thenReturn(List.of(address));
    when(addressMapper.toResponseDto(address)).thenReturn(addressResponseDto);

    List<AddressResponseDto> result = addressService.getAddressesByClient(1L);

    assertNotNull(result);
    assertEquals(1, result.size());
    verify(addressRepository).findByClientId(1L);
  }

  @Test
  void shouldGetDefaultAddress() {
    address.setDefaultAddress(true);
    when(addressRepository.findByClientIdAndDefaultAddressTrue(1L))
        .thenReturn(Optional.of(address));
    when(addressMapper.toResponseDto(address)).thenReturn(addressResponseDto);

    AddressResponseDto result = addressService.getDefaultAddress(1L);

    assertNotNull(result);
    verify(addressRepository).findByClientIdAndDefaultAddressTrue(1L);
  }

  @Test
  void shouldThrowExceptionWhenNoDefaultAddressFound() {
    when(addressRepository.findByClientIdAndDefaultAddressTrue(1L)).thenReturn(Optional.empty());

    assertThrows(RuntimeException.class, () -> addressService.getDefaultAddress(1L));
  }

  @Test
  void shouldDeleteAddress() {
    when(addressRepository.findById(1L)).thenReturn(Optional.of(address));

    addressService.deleteAddress(1L, 1L);

    verify(addressRepository).delete(address);
  }

  @Test
  void shouldThrowExceptionWhenDeletingAddressOfAnotherClient() {
    when(addressRepository.findById(1L)).thenReturn(Optional.of(address));

    assertThrows(RuntimeException.class, () -> addressService.deleteAddress(1L, 2L));
  }

  @Test
  void shouldThrowExceptionWhenDeletingNonExistentAddress() {
    when(addressRepository.findById(99L)).thenReturn(Optional.empty());

    assertThrows(RuntimeException.class, () -> addressService.deleteAddress(99L, 1L));
  }

  @Test
  void shouldSetDefaultAddress() {
    Address existingDefault = new Address();
    existingDefault.setId(2L);
    existingDefault.setDefaultAddress(true);
    existingDefault.setClient(client);

    when(addressRepository.findById(1L)).thenReturn(Optional.of(address));
    when(addressRepository.findByClientIdAndDefaultAddressTrue(1L))
        .thenReturn(Optional.of(existingDefault));

    addressService.setDefaultAddress(1L, 1L);

    assertTrue(address.getDefaultAddress());
    assertFalse(existingDefault.getDefaultAddress());
    verify(addressRepository, times(2)).save(any(Address.class));
  }

  @Test
  void shouldThrowExceptionWhenSettingDefaultAddressOfAnotherClient() {
    when(addressRepository.findById(1L)).thenReturn(Optional.of(address));

    assertThrows(RuntimeException.class, () -> addressService.setDefaultAddress(1L, 2L));
  }

  @Test
  void shouldThrowExceptionWhenSettingDefaultForNonExistentAddress() {
    when(addressRepository.findById(99L)).thenReturn(Optional.empty());

    assertThrows(RuntimeException.class, () -> addressService.setDefaultAddress(99L, 1L));
  }
}
