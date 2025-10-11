package com.orama.e_commerce.mapper;

import com.orama.e_commerce.dtos.ClientRequestDto;
import com.orama.e_commerce.dtos.ClientResponseDto;
import com.orama.e_commerce.models.Client;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "name", source = "name")
  @Mapping(target = "email", source = "email")
  @Mapping(target = "passwordHash", source = "password")
  @Mapping(target = "role", ignore = true)
  Client toEntity(ClientRequestDto requestDto);

  ClientResponseDto toResponseDto(Client client);
}
