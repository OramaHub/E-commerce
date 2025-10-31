package com.orama.e_commerce.mapper;

import com.orama.e_commerce.dtos.address.AddressRequestDto;
import com.orama.e_commerce.dtos.address.AddressResponseDto;
import com.orama.e_commerce.dtos.address.AddressUpdateRequestDto;
import com.orama.e_commerce.models.Address;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface AddressMapper {

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "client", ignore = true)
  @Mapping(target = "city", ignore = true)
  @Mapping(target = "orders", ignore = true)
  Address toEntity(AddressRequestDto requestDto);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "client", ignore = true)
  @Mapping(target = "city", ignore = true)
  @Mapping(target = "orders", ignore = true)
  void updateEntity(AddressUpdateRequestDto requestDto, @MappingTarget Address address);

  @Mapping(target = "cityId", source = "city.id")
  @Mapping(target = "cityName", source = "city.name")
  @Mapping(target = "clientId", source = "client.id")
  AddressResponseDto toResponseDto(Address address);
}
