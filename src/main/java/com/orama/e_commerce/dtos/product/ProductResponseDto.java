package com.orama.e_commerce.dtos.product;

import com.orama.e_commerce.dtos.product_image.ProductImageResponseDto;
import java.math.BigDecimal;
import java.util.List;

public record ProductResponseDto(
    Long id,
    String name,
    String description,
    BigDecimal price,
    Integer stock,
    List<ProductImageResponseDto> images) {}
