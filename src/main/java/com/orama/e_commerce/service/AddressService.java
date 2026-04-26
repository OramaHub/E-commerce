package com.orama.e_commerce.service;

import com.orama.e_commerce.dtos.address.AddressRequestDto;
import com.orama.e_commerce.dtos.address.AddressResponseDto;
import com.orama.e_commerce.dtos.address.AddressUpdateRequestDto;
import com.orama.e_commerce.dtos.location.CepLookupResponseDto;
import com.orama.e_commerce.exceptions.BadRequestException;
import com.orama.e_commerce.mapper.AddressMapper;
import com.orama.e_commerce.models.Address;
import com.orama.e_commerce.models.City;
import com.orama.e_commerce.models.Client;
import com.orama.e_commerce.repository.AddressRepository;
import com.orama.e_commerce.repository.CityRepository;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Locale;
import org.springframework.stereotype.Service;

@Service
public class AddressService {

  private final AddressRepository addressRepository;
  private final CityRepository cityRepository;
  private final AddressMapper addressMapper;
  private final LocationService locationService;

  public AddressService(
      AddressRepository addressRepository,
      CityRepository cityRepository,
      AddressMapper addressMapper,
      LocationService locationService) {
    this.addressRepository = addressRepository;
    this.cityRepository = cityRepository;
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
        requestDto.cityId(),
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
        requestDto.cityId() != null
            ? requestDto.cityId()
            : address.getCity() != null ? address.getCity().getId() : null,
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

  private City referenceCity(Long cityId) {
    if (cityId == null || cityId <= 0) {
      return null;
    }
    City city = new City();
    city.setId(cityId);
    return city;
  }

  private void applyLocation(
      Address address,
      Long cityId,
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
            cityId != null && cityId > 0 ? cityId : null,
            trimToNull(cityName),
            normalizeStateUf(stateUf),
            trimToNull(countryCode),
            trimToNull(ibgeCode));

    if (location.cityId() != null) {
      location = enrichFromCity(location);
    }

    if (!hasText(location.cityName()) || !hasText(location.stateUf())) {
      location = enrichFromCep(location, zipCode);
    } else if (location.cityId() == null) {
      location = tryEnrichFromCep(location, zipCode);
    }

    if (!hasText(location.cityName()) || !hasText(location.stateUf())) {
      throw new BadRequestException("Cidade e UF sao obrigatorias para salvar o endereco.");
    }

    address.setCity(referenceCity(location.cityId()));
    address.setCityName(location.cityName());
    address.setStateUf(location.stateUf());
    address.setCountryCode(hasText(location.countryCode()) ? location.countryCode() : "BR");
    address.setIbgeCode(location.ibgeCode());
  }

  private LocationData enrichFromCity(LocationData location) {
    return cityRepository
        .findByIdWithStateAndCountry(location.cityId())
        .map(
            city ->
                new LocationData(
                    city.getId(),
                    firstText(location.cityName(), city.getName()),
                    firstText(
                        location.stateUf(),
                        city.getState() != null ? city.getState().getAbbreviation() : null),
                    firstText(
                        location.countryCode(),
                        city.getState() != null && city.getState().getCountry() != null
                            ? city.getState().getCountry().getAbbreviation()
                            : null),
                    firstText(location.ibgeCode(), city.getIbgeCode())))
        .orElse(
            new LocationData(
                null,
                location.cityName(),
                location.stateUf(),
                location.countryCode(),
                location.ibgeCode()));
  }

  private LocationData enrichFromCep(LocationData location, String zipCode) {
    CepLookupResponseDto lookupResponse = locationService.lookupCep(zipCode);
    if (lookupResponse == null) {
      return location;
    }
    return new LocationData(
        firstLong(location.cityId(), lookupResponse.cityId()),
        firstText(location.cityName(), lookupResponse.cityName(), lookupResponse.city()),
        firstText(location.stateUf(), lookupResponse.stateUf(), lookupResponse.state()),
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

  private Long firstLong(Long first, Long second) {
    return first != null && first > 0 ? first : second;
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
      Long cityId, String cityName, String stateUf, String countryCode, String ibgeCode) {}
}
