package com.orama.e_commerce.dtos.custom_order;

import com.orama.e_commerce.enums.*;
import java.time.Instant;
import java.util.List;

public record CustomOrderResponseDto(
    Long id,
    String orderNumber,
    Long clientId,
    String clientName,
    CapLine capLine,
    CapModel capModel,
    CapMaterial capMaterial,
    Boolean laserCut,
    Boolean fullLaserCut,
    StrapType strapType,
    String colorFront,
    String colorMesh,
    String colorBrim,
    String colorBrimLining,
    Integer quantity,
    String logoUrl,
    String previewImageUrl,
    String layoutImageUrl,
    String observations,
    CustomOrderStatus status,
    Instant createdAt,
    List<LogoDetailDto> logoDetails) {}
