package com.orama.e_commerce.dtos.product;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record ProductStockSetDto(@NotNull @Min(0) Integer newStockValue) {}
