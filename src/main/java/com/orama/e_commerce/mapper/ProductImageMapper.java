package com.orama.e_commerce.mapper;

import com.orama.e_commerce.dtos.product_image.ProductImageRequestDto;
import com.orama.e_commerce.dtos.product_image.ProductImageResponseDto;
import com.orama.e_commerce.models.ProductImage;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ProductImageMapper {

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "product", ignore = true)
  @Mapping(target = "url", source = "url")
  ProductImage toEntity(ProductImageRequestDto productImageRequestDto);

  ProductImageResponseDto toResponseDto(ProductImage image);
}
