package com.orama.e_commerce.dtos.product;

import jakarta.validation.constraints.NotNull;

public record ProductStockAdjustmentDto(@NotNull Integer quantity) {}
