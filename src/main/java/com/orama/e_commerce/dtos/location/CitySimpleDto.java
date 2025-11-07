package com.orama.e_commerce.dtos.location;

public record CitySimpleDto(
    Long id, String name, String ibgeCode, Long stateId, String stateName, String stateCode) {}
