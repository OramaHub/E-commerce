package com.orama.e_commerce.service;

import com.orama.e_commerce.dtos.address.AddressRequestDto;
import com.orama.e_commerce.dtos.address.AddressResponseDto;
import com.orama.e_commerce.dtos.address.AddressUpdateRequestDto;
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

  public AddressService(AddressRepository addressRepository, AddressMapper addressMapper) {
    this.addressRepository = addressRepository;
    this.addressMapper = addressMapper;
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

    City city = new City();
    city.setId(requestDto.cityId());
    address.setCity(city);

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
      throw new RuntimeException("You don't have permission to update this address");
    }

    if (Boolean.TRUE.equals(requestDto.defaultAddress())) {
      unsetDefaultAddress(clientId);
    }

    if (requestDto.cityId() != null) {
      City city = new City();
      city.setId(requestDto.cityId());
      address.setCity(city);
    }

    addressMapper.updateEntity(requestDto, address);

    Address updated = addressRepository.save(address);

    return addressMapper.toResponseDto(updated);
  }

  public AddressResponseDto getAddressById(Long id, Long clientId) {
    Address address = findById(id);

    if (!address.getClient().getId().equals(clientId)) {
      throw new RuntimeException("You don't have permission to view this address");
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
            .orElseThrow(() -> new RuntimeException("No default address found for this client"));

    return addressMapper.toResponseDto(address);
  }

  @Transactional
  public void deleteAddress(Long id, Long clientId) {
    Address address = findById(id);

    if (!address.getClient().getId().equals(clientId)) {
      throw new RuntimeException("You don't have permission to delete this address");
    }

    addressRepository.delete(address);
  }

  @Transactional
  public void setDefaultAddress(Long id, Long clientId) {
    Address address = findById(id);

    if (!address.getClient().getId().equals(clientId)) {
      throw new RuntimeException("You don't have permission to modify this address");
    }

    unsetDefaultAddress(clientId);

    address.setDefaultAddress(true);
    addressRepository.save(address);
  }

  private Address findById(Long id) {
    return addressRepository
        .findById(id)
        .orElseThrow(() -> new RuntimeException("Address not found with id " + id));
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
}
