package com.orama.e_commerce.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.orama.e_commerce.dtos.address.AddressRequestDto;
import com.orama.e_commerce.dtos.address.AddressResponseDto;
import com.orama.e_commerce.dtos.address.AddressUpdateRequestDto;
import com.orama.e_commerce.dtos.location.CepLookupResponseDto;
import com.orama.e_commerce.exceptions.BadRequestException;
import com.orama.e_commerce.mapper.AddressMapper;
import com.orama.e_commerce.models.Address;
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
  @Mock private LocationService locationService;

  @InjectMocks private AddressService addressService;

  private Address address;
  private Client client;
  private AddressResponseDto addressResponseDto;

  @BeforeEach
  void setUp() {
    client = new Client();
    client.setId(1L);
    client.setName("Joao Silva");

    address = new Address();
    address.setId(1L);
    address.setStreet("Rua das Flores");
    address.setNumber("123");
    address.setComplement("Apt 45");
    address.setDistrict("Centro");
    address.setZipCode("01234-567");
    address.setClient(client);
    address.setCityName("Sao Paulo");
    address.setStateUf("SP");
    address.setCountryCode("BR");
    address.setIbgeCode("3550308");
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
            "Sao Paulo",
            "SP",
            "BR",
            "3550308",
            1L,
            Instant.now(),
            Instant.now());
  }

  @Test
  void shouldCreateAddress() {
    AddressRequestDto requestDto = request(false);

    when(addressMapper.toEntity(requestDto)).thenReturn(address);
    when(addressRepository.save(any(Address.class))).thenReturn(address);
    when(addressMapper.toResponseDto(address)).thenReturn(addressResponseDto);

    AddressResponseDto result = addressService.createAddress(requestDto, 1L);

    assertNotNull(result);
    assertEquals("Rua das Flores", result.street());
    verify(addressRepository).save(address);
  }

  @Test
  void shouldCreateAddressEnrichingTextualLocationFromZipCodeWhenMissing() {
    AddressRequestDto requestDto =
        new AddressRequestDto(
            "Rua das Flores",
            "123",
            "Apt 45",
            "Centro",
            "01234-567",
            null,
            null,
            null,
            null,
            false);

    address.setCityName(null);
    address.setStateUf(null);
    address.setCountryCode(null);
    address.setIbgeCode(null);

    when(addressMapper.toEntity(requestDto)).thenReturn(address);
    when(locationService.lookupCep("01234-567")).thenReturn(lookup("Sao Paulo", "SP"));
    when(addressRepository.save(any(Address.class))).thenReturn(address);
    when(addressMapper.toResponseDto(address)).thenReturn(addressResponseDto);

    AddressResponseDto result = addressService.createAddress(requestDto, 1L);

    assertNotNull(result);
    assertEquals("Sao Paulo", address.getCityName());
    assertEquals("SP", address.getStateUf());
    assertEquals("BR", address.getCountryCode());
    assertEquals("3550308", address.getIbgeCode());
    verify(addressRepository).save(address);
  }

  @Test
  void shouldCreateAddressWithTextualCityWhenCepLookupFails() {
    AddressRequestDto requestDto =
        new AddressRequestDto(
            "Rua das Flores",
            "123",
            "Apt 45",
            "Centro",
            "01234-567",
            "Sao Paulo",
            "SP",
            "BR",
            null,
            false);
    address.setIbgeCode(null);

    when(addressMapper.toEntity(requestDto)).thenReturn(address);
    when(locationService.lookupCep("01234-567"))
        .thenThrow(new BadRequestException("Nao foi possivel consultar o CEP."));
    when(addressRepository.save(any(Address.class))).thenReturn(address);
    when(addressMapper.toResponseDto(address)).thenReturn(addressResponseDto);

    AddressResponseDto result = addressService.createAddress(requestDto, 1L);

    assertNotNull(result);
    assertEquals("Sao Paulo", address.getCityName());
    assertEquals("SP", address.getStateUf());
    verify(addressRepository).save(address);
  }

  @Test
  void shouldThrowWhenTextualCityIsMissingAndCepLookupFails() {
    AddressRequestDto requestDto =
        new AddressRequestDto(
            "Rua das Flores",
            "123",
            "Apt 45",
            "Centro",
            "01234-567",
            null,
            null,
            null,
            null,
            false);

    address.setCityName(null);
    address.setStateUf(null);

    when(addressMapper.toEntity(requestDto)).thenReturn(address);
    when(locationService.lookupCep("01234-567"))
        .thenThrow(new BadRequestException("Nao foi possivel consultar o CEP."));

    assertThrows(BadRequestException.class, () -> addressService.createAddress(requestDto, 1L));
    verify(addressRepository, never()).save(any(Address.class));
  }

  @Test
  void shouldCreateAddressAsDefault() {
    AddressRequestDto requestDto = request(true);

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
    AddressRequestDto requestDto = request(null);

    when(addressMapper.toEntity(requestDto)).thenReturn(address);
    when(addressRepository.save(any(Address.class))).thenReturn(address);
    when(addressMapper.toResponseDto(address)).thenReturn(addressResponseDto);

    AddressResponseDto result = addressService.createAddress(requestDto, 1L);

    assertNotNull(result);
    assertFalse(address.getDefaultAddress());
    verify(addressRepository).save(address);
  }

  @Test
  void shouldUpdateAddress() {
    AddressUpdateRequestDto updateRequestDto = update(false);

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
    AddressUpdateRequestDto updateRequestDto = update(true);

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
    AddressUpdateRequestDto updateRequestDto = update(false);

    when(addressRepository.findById(1L)).thenReturn(Optional.of(address));

    assertThrows(
        RuntimeException.class, () -> addressService.updateAddress(1L, updateRequestDto, 2L));
  }

  @Test
  void shouldThrowExceptionWhenUpdatingNonExistentAddress() {
    AddressUpdateRequestDto updateRequestDto = update(false);

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

  private AddressRequestDto request(Boolean defaultAddress) {
    return new AddressRequestDto(
        "Rua das Flores",
        "123",
        "Apt 45",
        "Centro",
        "01234-567",
        "Sao Paulo",
        "SP",
        "BR",
        "3550308",
        defaultAddress);
  }

  private AddressUpdateRequestDto update(Boolean defaultAddress) {
    return new AddressUpdateRequestDto(
        "Rua Nova",
        "456",
        "Casa",
        "Bairro Novo",
        "98765-432",
        "Sao Paulo",
        "SP",
        "BR",
        "3550308",
        defaultAddress);
  }

  private CepLookupResponseDto lookup(String cityName, String stateUf) {
    return new CepLookupResponseDto(
        "01234-567", "Rua das Flores", "Centro", cityName, stateUf, "BR", "3550308");
  }
}
