package com.orama.e_commerce.dtos.product;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

// Adição/Remoção de Estoque (PATCH) com motivo: ex inventario, etc
public record ProductStockAdjustmentDto(@NotNull @Min(1) Integer quantity, String reason) {}
