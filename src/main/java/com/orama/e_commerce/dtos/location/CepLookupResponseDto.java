package com.orama.e_commerce.dtos.location;

public record CepLookupResponseDto(
    String zipCode, String street, String district, String city, String state, Long cityId) {}
