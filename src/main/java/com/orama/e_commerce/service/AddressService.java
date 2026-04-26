package com.orama.e_commerce.service;

import com.orama.e_commerce.dtos.address.AddressRequestDto;
import com.orama.e_commerce.dtos.address.AddressResponseDto;
import com.orama.e_commerce.dtos.address.AddressUpdateRequestDto;
import com.orama.e_commerce.dtos.location.CepLookupResponseDto;
import com.orama.e_commerce.exceptions.BadRequestException;
import com.orama.e_commerce.mapper.AddressMapper;
import com.orama.e_commerce.models.Address;
import com.orama.e_commerce.models.Client;
import com.orama.e_commerce.repository.AddressRepository;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Locale;
import org.springframework.stereotype.Service;

@Service
public class AddressService {

  private final AddressRepository addressRepository;
  private final AddressMapper addressMapper;
  private final LocationService locationService;

  public AddressService(
      AddressRepository addressRepository,
      AddressMapper addressMapper,
      LocationService locationService) {
    this.addressRepository = addressRepository;
    this.addressMapper = addressMapper;
    this.locationService = locationService;
  }

  public boolean isOwner(Long addressId, Long clientId) {
    return addressRepository.existsByIdAndClientId(addressId, clientId);
  }

  @Transactional
  public AddressResponseDto createAddress(AddressRequestDto requestDto, Long clientId) {
    Address address = addressMapper.toEntity(requestDto);

    Client client = new Client();
    client.setId(clientId);
    address.setClient(client);

    applyLocation(
        address,
        requestDto.zipCode(),
        requestDto.cityName(),
        requestDto.stateUf(),
        requestDto.countryCode(),
        requestDto.ibgeCode());

    if (Boolean.TRUE.equals(requestDto.defaultAddress())) {
      unsetDefaultAddress(clientId);
    } else if (requestDto.defaultAddress() == null) {
      address.setDefaultAddress(false);
    }

    Address saved = addressRepository.save(address);

    return addressMapper.toResponseDto(saved);
  }

  @Transactional
  public AddressResponseDto updateAddress(
      Long id, AddressUpdateRequestDto requestDto, Long clientId) {

    Address address = findById(id);

    if (!address.getClient().getId().equals(clientId)) {
      throw new RuntimeException("Você não tem permissão para atualizar este endereço");
    }

    if (Boolean.TRUE.equals(requestDto.defaultAddress())) {
      unsetDefaultAddress(clientId);
    }

    addressMapper.updateEntity(requestDto, address);
    applyLocation(
        address,
        address.getZipCode(),
        address.getCityName(),
        address.getStateUf(),
        address.getCountryCode(),
        address.getIbgeCode());

    Address updated = addressRepository.save(address);

    return addressMapper.toResponseDto(updated);
  }

  public AddressResponseDto getAddressById(Long id, Long clientId) {
    Address address = findById(id);

    if (!address.getClient().getId().equals(clientId)) {
      throw new RuntimeException("Você não tem permissão para visualizar este endereço");
    }

    return addressMapper.toResponseDto(address);
  }

  public List<AddressResponseDto> getAddressesByClient(Long clientId) {
    List<Address> addresses = addressRepository.findByClientId(clientId);
    return addresses.stream().map(addressMapper::toResponseDto).toList();
  }

  public AddressResponseDto getDefaultAddress(Long clientId) {
    Address address =
        addressRepository
            .findByClientIdAndDefaultAddressTrue(clientId)
            .orElseThrow(
                () -> new RuntimeException("Nenhum endereço padrão encontrado para este cliente"));

    return addressMapper.toResponseDto(address);
  }

  @Transactional
  public void deleteAddress(Long id, Long clientId) {
    Address address = findById(id);

    if (!address.getClient().getId().equals(clientId)) {
      throw new RuntimeException("Você não tem permissão para excluir este endereço");
    }

    addressRepository.delete(address);
  }

  @Transactional
  public void setDefaultAddress(Long id, Long clientId) {
    Address address = findById(id);

    if (!address.getClient().getId().equals(clientId)) {
      throw new RuntimeException("Você não tem permissão para modificar este endereço");
    }

    unsetDefaultAddress(clientId);

    address.setDefaultAddress(true);
    addressRepository.save(address);
  }

  private Address findById(Long id) {
    return addressRepository
        .findById(id)
        .orElseThrow(() -> new RuntimeException("Endereço não encontrado com id " + id));
  }

  private void unsetDefaultAddress(Long clientId) {
    addressRepository
        .findByClientIdAndDefaultAddressTrue(clientId)
        .ifPresent(
            defaultAddress -> {
              defaultAddress.setDefaultAddress(false);
              addressRepository.save(defaultAddress);
            });
  }

  private void applyLocation(
      Address address,
      String zipCode,
      String cityName,
      String stateUf,
      String countryCode,
      String ibgeCode) {
    if (!hasText(zipCode)) {
      throw new BadRequestException("CEP e obrigatorio para identificar a cidade.");
    }

    LocationData location =
        new LocationData(
            trimToNull(cityName),
            normalizeStateUf(stateUf),
            trimToNull(countryCode),
            trimToNull(ibgeCode));

    if (!hasText(location.cityName()) || !hasText(location.stateUf())) {
      location = enrichFromCep(location, zipCode);
    } else if (!hasText(location.ibgeCode())) {
      location = tryEnrichFromCep(location, zipCode);
    }

    if (!hasText(location.cityName()) || !hasText(location.stateUf())) {
      throw new BadRequestException("Cidade e UF sao obrigatorias para salvar o endereco.");
    }

    address.setCityName(location.cityName());
    address.setStateUf(location.stateUf());
    address.setCountryCode(hasText(location.countryCode()) ? location.countryCode() : "BR");
    address.setIbgeCode(location.ibgeCode());
  }

  private LocationData enrichFromCep(LocationData location, String zipCode) {
    CepLookupResponseDto lookupResponse = locationService.lookupCep(zipCode);
    if (lookupResponse == null) {
      return location;
    }
    return new LocationData(
        firstText(location.cityName(), lookupResponse.cityName()),
        firstText(location.stateUf(), lookupResponse.stateUf()),
        firstText(location.countryCode(), lookupResponse.countryCode(), "BR"),
        firstText(location.ibgeCode(), lookupResponse.ibgeCode()));
  }

  private LocationData tryEnrichFromCep(LocationData location, String zipCode) {
    try {
      return enrichFromCep(location, zipCode);
    } catch (BadRequestException exception) {
      return location;
    }
  }

  private String firstText(String... values) {
    for (String value : values) {
      if (hasText(value)) {
        return value.trim();
      }
    }
    return null;
  }

  private String normalizeStateUf(String stateUf) {
    String value = trimToNull(stateUf);
    return value != null ? value.toUpperCase(Locale.ROOT) : null;
  }

  private String trimToNull(String value) {
    return hasText(value) ? value.trim() : null;
  }

  private boolean hasText(String value) {
    return value != null && !value.isBlank();
  }

  private record LocationData(
      String cityName, String stateUf, String countryCode, String ibgeCode) {}
}
