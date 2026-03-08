package com.orama.e_commerce.dtos.custom_order;

import com.orama.e_commerce.enums.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.List;

public record CreateCustomOrderRequestDto(
    @NotNull(message = "Linha do boné é obrigatória") CapLine capLine,
    @NotNull(message = "Modelo do boné é obrigatório") CapModel capModel,
    @NotNull(message = "Material é obrigatório") CapMaterial capMaterial,
    Boolean laserCut,
    Boolean fullLaserCut,
    @NotNull(message = "Tipo de ataca é obrigatório") StrapType strapType,
    @NotBlank(message = "Cor da frente é obrigatória")
        @Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "Cor deve estar no formato hex (#RRGGBB)")
        String colorFront,
    @Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "Cor deve estar no formato hex (#RRGGBB)")
        String colorMesh,
    @NotBlank(message = "Cor da aba é obrigatória")
        @Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "Cor deve estar no formato hex (#RRGGBB)")
        String colorBrim,
    @Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "Cor deve estar no formato hex (#RRGGBB)")
        String colorBrimLining,
    @NotNull(message = "Quantidade é obrigatória")
        @Min(value = 1, message = "Quantidade mínima é 1")
        Integer quantity,
    @Size(max = 2000, message = "Observações devem ter no máximo 2000 caracteres")
        String observations,
    @Valid List<LogoDetailDto> logoDetails) {}
