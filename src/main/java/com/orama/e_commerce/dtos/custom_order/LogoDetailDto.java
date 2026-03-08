package com.orama.e_commerce.dtos.custom_order;

import com.orama.e_commerce.enums.LogoPosition;
import com.orama.e_commerce.enums.LogoTechnique;
import jakarta.validation.constraints.NotNull;

public record LogoDetailDto(
    @NotNull(message = "Posição da logo é obrigatória") LogoPosition position,
    @NotNull(message = "Técnica da logo é obrigatória") LogoTechnique technique) {}
