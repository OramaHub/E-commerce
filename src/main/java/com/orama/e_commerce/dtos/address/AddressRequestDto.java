package com.orama.e_commerce.dtos.address;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AddressRequestDto(
    @NotBlank(message = "Street cannot be empty")
        @Size(max = 255, message = "Street must be at most 255 characters long")
        String street,
    @Size(max = 20, message = "Number must be at most 20 characters long") String number,
    @Size(max = 100, message = "Complement must be at most 100 characters long") String complement,
    @NotBlank(message = "District cannot be empty")
        @Size(max = 100, message = "District must be at most 100 characters long")
        String district,
    @NotBlank(message = "Zip code cannot be empty")
        @Size(max = 20, message = "Zip code must be at most 20 characters long")
        String zipCode,
    @NotNull(message = "City ID cannot be null") Long cityId,
    Boolean defaultAddress) {}
