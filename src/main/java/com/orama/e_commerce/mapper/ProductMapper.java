package com.orama.e_commerce.mapper;

import com.orama.e_commerce.dtos.product.ProductRequestDto;
import com.orama.e_commerce.dtos.product.ProductResponseDto;
import com.orama.e_commerce.dtos.product.ProductUpdateRequestDto;
import com.orama.e_commerce.models.Product;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(
    componentModel = "spring",
    uses = {ProductImageMapper.class})
public interface ProductMapper {

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "name", source = "name")
  @Mapping(target = "description", source = "description")
  @Mapping(target = "price", source = "price")
  @Mapping(target = "stock", source = "stock")
  @Mapping(target = "active", ignore = true)
  @Mapping(target = "images", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  Product toEntity(ProductRequestDto requestDto);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "stock", ignore = true)
  @Mapping(target = "active", ignore = true)
  @Mapping(target = "images", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  void updateDto(ProductUpdateRequestDto updateDto, @MappingTarget Product product);

  ProductResponseDto toResponseDto(Product product);
}
