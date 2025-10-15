package com.orama.e_commerce.mapper;

import com.orama.e_commerce.dtos.client.ClientRequestDto;
import com.orama.e_commerce.dtos.client.ClientResponseDto;
import com.orama.e_commerce.dtos.client.ClientUpdateRequestDto;
import com.orama.e_commerce.models.Client;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface ClientMapper {

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "name", source = "name")
  @Mapping(target = "email", source = "email")
  @Mapping(target = "cpf", source = "cpf")
  @Mapping(target = "phone", source = "phone")
  @Mapping(target = "passwordHash", source = "password")
  @Mapping(target = "active", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "role", ignore = true)
  @Mapping(target = "addresses", ignore = true)
  @Mapping(target = "carts", ignore = true)
  Client toEntity(ClientRequestDto requestDto);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "cpf", ignore = true)
  @Mapping(target = "passwordHash", ignore = true)
  @Mapping(target = "active", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "role", ignore = true)
  @Mapping(target = "addresses", ignore = true)
  @Mapping(target = "carts", ignore = true)
  void updateDto(ClientUpdateRequestDto requestDto, @MappingTarget Client client);

  ClientResponseDto toResponseDto(Client client);
}
