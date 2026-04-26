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
import jakarta.transaction.Transactional;
import java.util.List;
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

    address.setCity(referenceCity(resolveCityId(requestDto.cityId(), requestDto.zipCode())));

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

    if (requestDto.cityId() != null || hasText(requestDto.zipCode())) {
      address.setCity(referenceCity(resolveCityId(requestDto.cityId(), requestDto.zipCode())));
    }

    addressMapper.updateEntity(requestDto, address);

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
    City city = new City();
    city.setId(cityId);
    return city;
  }

  private Long resolveCityId(Long cityId, String zipCode) {
    if (cityId != null && cityId > 0) {
      return cityId;
    }

    if (!hasText(zipCode)) {
      throw new BadRequestException("CEP e obrigatorio para identificar a cidade.");
    }

    CepLookupResponseDto lookupResponse = locationService.lookupCep(zipCode);
    if (lookupResponse == null || lookupResponse.cityId() == null) {
      throw new BadRequestException("Nao foi possivel identificar a cidade pelo CEP informado.");
    }

    return lookupResponse.cityId();
  }

  private boolean hasText(String value) {
    return value != null && !value.isBlank();
  }
}
