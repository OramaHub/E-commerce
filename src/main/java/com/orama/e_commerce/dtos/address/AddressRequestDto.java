package com.orama.e_commerce.dtos.address;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AddressRequestDto(
    @NotBlank(message = "Rua e obrigatoria")
        @Size(max = 255, message = "Rua deve ter no maximo 255 caracteres")
        String street,
    @NotBlank(message = "Numero e obrigatorio")
        @Size(max = 20, message = "Numero deve ter no maximo 20 caracteres")
        String number,
    @Size(max = 100, message = "Complemento deve ter no maximo 100 caracteres") String complement,
    @NotBlank(message = "Bairro e obrigatorio")
        @Size(max = 100, message = "Bairro deve ter no maximo 100 caracteres")
        String district,
    @NotBlank(message = "CEP e obrigatorio")
        @Size(max = 20, message = "CEP deve ter no maximo 20 caracteres")
        String zipCode,
    @Size(max = 150, message = "Cidade deve ter no maximo 150 caracteres") String cityName,
    @Size(max = 10, message = "UF deve ter no maximo 10 caracteres") String stateUf,
    @Size(max = 10, message = "Pais deve ter no maximo 10 caracteres") String countryCode,
    @Size(max = 7, message = "Codigo IBGE deve ter no maximo 7 caracteres") String ibgeCode,
    Boolean defaultAddress) {}
