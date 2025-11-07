package com.orama.e_commerce.dtos.location;

public record StateResponseDto(
    Long id, String name, String abbreviation, CountryResponseDto country) {}
