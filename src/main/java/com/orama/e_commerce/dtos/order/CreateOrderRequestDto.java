package com.orama.e_commerce.dtos.order;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;

public record CreateOrderRequestDto(
    @NotNull(message = "ID do carrinho é obrigatório")
        @Positive(message = "ID do carrinho deve ser positivo")
        Long cartId,
    @PositiveOrZero(message = "Desconto deve ser zero ou positivo") BigDecimal discount,
    @NotBlank(message = "CEP é obrigatório") String zipCode,
    @NotNull(message = "Endereço de entrega é obrigatório")
        @Positive(message = "ID do endereço deve ser positivo")
        Long deliveryAddressId) {}
