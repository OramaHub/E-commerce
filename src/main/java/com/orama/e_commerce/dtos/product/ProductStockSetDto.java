package com.orama.e_commerce.dtos.product;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

// B. DTO para Atualização Direta do Estoque (PUT): muda o valor atual do estoque
public record ProductStockSetDto(@NotNull @Min(0) Integer newStockValue) {}
