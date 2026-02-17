package com.orama.e_commerce.dtos.address;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AddressRequestDto(
    @NotBlank(message = "Rua é obrigatória")
        @Size(max = 255, message = "Rua deve ter no máximo 255 caracteres")
        String street,
    @Size(max = 20, message = "Número deve ter no máximo 20 caracteres") String number,
    @Size(max = 100, message = "Complemento deve ter no máximo 100 caracteres") String complement,
    @NotBlank(message = "Bairro é obrigatório")
        @Size(max = 100, message = "Bairro deve ter no máximo 100 caracteres")
        String district,
    @NotBlank(message = "CEP é obrigatório")
        @Size(max = 20, message = "CEP deve ter no máximo 20 caracteres")
        String zipCode,
    @NotNull(message = "ID da cidade é obrigatório") Long cityId,
    Boolean defaultAddress) {}
