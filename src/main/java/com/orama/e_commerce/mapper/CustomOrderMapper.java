package com.orama.e_commerce.mapper;

import com.orama.e_commerce.dtos.custom_order.CreateCustomOrderRequestDto;
import com.orama.e_commerce.dtos.custom_order.CustomOrderResponseDto;
import com.orama.e_commerce.dtos.custom_order.LogoDetailDto;
import com.orama.e_commerce.models.CustomOrder;
import com.orama.e_commerce.models.CustomOrderLogoDetail;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CustomOrderMapper {

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "orderNumber", ignore = true)
  @Mapping(target = "client", ignore = true)
  @Mapping(target = "logoUrl", ignore = true)
  @Mapping(target = "previewImageUrl", ignore = true)
  @Mapping(target = "layoutImageUrl", ignore = true)
  @Mapping(target = "status", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "logoDetails", ignore = true)
  CustomOrder toEntity(CreateCustomOrderRequestDto dto);

  @Mapping(target = "clientId", source = "client.id")
  @Mapping(target = "clientName", source = "client.name")
  CustomOrderResponseDto toResponseDto(CustomOrder customOrder);

  LogoDetailDto toLogoDetailDto(CustomOrderLogoDetail detail);
}
