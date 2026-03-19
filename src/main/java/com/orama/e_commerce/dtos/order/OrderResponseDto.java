package com.orama.e_commerce.dtos.order;

import com.orama.e_commerce.enums.OrderStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record OrderResponseDto(
    Long id,
    String orderNumber,
    Instant orderDate,
    OrderStatus status,
    BigDecimal subtotal,
    BigDecimal discount,
    BigDecimal shippingCost,
    BigDecimal total,
    String zipCode,
    Long clientId,
    String clientName,
    String paymentId,
    String paymentMethod,
    List<OrderItemDto> items,
    Long deliveryAddressId,
    String deliveryStreet,
    String deliveryNumber,
    String deliveryDistrict,
    String deliveryCity,
    String deliveryState) {}
