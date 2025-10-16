package com.orama.e_commerce.mapper;

import com.orama.e_commerce.dtos.product.ProductRequestDto;
import com.orama.e_commerce.dtos.product.ProductResponseDto;
import com.orama.e_commerce.models.Product;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ProductMapper {

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "name", source = "name")
  @Mapping(target = "description", source = "description")
  @Mapping(target = "price", source = "price")
  @Mapping(target = "stock", source = "stock")
  @Mapping(target = "active", ignore = true)
  @Mapping(target = "images", ignore = true)
  Product toEntity(ProductRequestDto requestDto);

  ProductResponseDto toResponseDto(Product product);
}
