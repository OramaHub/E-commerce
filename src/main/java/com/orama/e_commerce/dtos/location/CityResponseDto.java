package com.orama.e_commerce.dtos.location;

public record CityResponseDto(Long id, String name, String ibgeCode, StateResponseDto state) {}
