package com.orama.e_commerce.dtos.location;

public record CepLookupResponseDto(
    String zipCode,
    String street,
    String district,
    String cityName,
    String stateUf,
    String countryCode,
    String ibgeCode) {}
