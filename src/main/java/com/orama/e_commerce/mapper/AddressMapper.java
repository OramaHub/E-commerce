package com.orama.e_commerce.mapper;

import com.orama.e_commerce.dtos.address.AddressRequestDto;
import com.orama.e_commerce.dtos.address.AddressResponseDto;
import com.orama.e_commerce.dtos.address.AddressUpdateRequestDto;
import com.orama.e_commerce.models.Address;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface AddressMapper {

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "client", ignore = true)
  @Mapping(target = "city", ignore = true)
  @Mapping(target = "orders", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  Address toEntity(AddressRequestDto requestDto);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "client", ignore = true)
  @Mapping(target = "city", ignore = true)
  @Mapping(target = "orders", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
  void updateEntity(AddressUpdateRequestDto requestDto, @MappingTarget Address address);

  default AddressResponseDto toResponseDto(Address address) {
    if (address == null) {
      return null;
    }
    return new AddressResponseDto(
        address.getId(),
        address.getStreet(),
        address.getNumber(),
        address.getComplement(),
        address.getDistrict(),
        address.getZipCode(),
        address.getDefaultAddress(),
        address.getCity() != null ? address.getCity().getId() : null,
        resolveCityName(address),
        resolveStateUf(address),
        hasText(address.getCountryCode()) ? address.getCountryCode() : "BR",
        address.getIbgeCode(),
        address.getClient() != null ? address.getClient().getId() : null,
        address.getCreatedAt(),
        address.getUpdatedAt());
  }

  private String resolveCityName(Address address) {
    if (hasText(address.getCityName())) {
      return address.getCityName();
    }
    return address.getCity() != null ? address.getCity().getName() : null;
  }

  private String resolveStateUf(Address address) {
    if (hasText(address.getStateUf())) {
      return address.getStateUf();
    }
    return address.getCity() != null && address.getCity().getState() != null
        ? address.getCity().getState().getAbbreviation()
        : null;
  }

  private boolean hasText(String value) {
    return value != null && !value.isBlank();
  }
}
