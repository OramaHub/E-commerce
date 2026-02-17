package com.orama.e_commerce.dtos.address;

import jakarta.validation.constraints.Size;

public record AddressUpdateRequestDto(
    @Size(max = 255, message = "Rua deve ter no máximo 255 caracteres") String street,
    @Size(max = 20, message = "Número deve ter no máximo 20 caracteres") String number,
    @Size(max = 100, message = "Complemento deve ter no máximo 100 caracteres") String complement,
    @Size(max = 100, message = "Bairro deve ter no máximo 100 caracteres") String district,
    @Size(max = 20, message = "CEP deve ter no máximo 20 caracteres") String zipCode,
    Long cityId,
    Boolean defaultAddress) {}
