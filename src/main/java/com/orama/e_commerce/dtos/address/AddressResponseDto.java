package com.orama.e_commerce.dtos.address;

import java.time.Instant;

public record AddressResponseDto(
    Long id,
    String street,
    String number,
    String complement,
    String district,
    String zipCode,
    Boolean defaultAddress,
    Long cityId,
    String cityName,
    Long clientId,
    Instant createdAt,
    Instant updatedAt) {}
