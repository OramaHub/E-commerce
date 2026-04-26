package com.orama.e_commerce.mapper;

import com.orama.e_commerce.dtos.order.CreateOrderRequestDto;
import com.orama.e_commerce.dtos.order.OrderItemDto;
import com.orama.e_commerce.dtos.order.OrderResponseDto;
import com.orama.e_commerce.models.Order;
import com.orama.e_commerce.models.OrderItem;
import com.orama.e_commerce.models.OrderShippingAddress;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface OrderMapper {

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "discount", source = "discount")
  @Mapping(target = "cart.id", source = "cartId")
  @Mapping(target = "orderNumber", ignore = true)
  @Mapping(target = "orderDate", ignore = true)
  @Mapping(target = "status", ignore = true)
  @Mapping(target = "subtotal", ignore = true)
  @Mapping(target = "total", ignore = true)
  @Mapping(target = "shippingCost", ignore = true)
  @Mapping(target = "zipCode", ignore = true)
  @Mapping(target = "client", ignore = true)
  @Mapping(target = "cart", ignore = true)
  @Mapping(target = "items", ignore = true)
  @Mapping(target = "shippingAddress", ignore = true)
  @Mapping(target = "paymentId", ignore = true)
  @Mapping(target = "paymentMethod", ignore = true)
  Order toEntity(CreateOrderRequestDto dto);

  default OrderResponseDto toResponseDto(Order order) {
    if (order == null) {
      return null;
    }

    OrderShippingAddress snapshot = order.getShippingAddress();

    return new OrderResponseDto(
        order.getId(),
        order.getOrderNumber(),
        order.getOrderDate(),
        order.getStatus(),
        order.getSubtotal(),
        order.getDiscount(),
        order.getShippingCost(),
        order.getTotal(),
        order.getZipCode(),
        order.getClient() != null ? order.getClient().getId() : null,
        order.getClient() != null ? order.getClient().getName() : null,
        order.getPaymentId(),
        order.getPaymentMethod(),
        order.getItems() != null
            ? order.getItems().stream().map(this::toItemDto).toList()
            : List.of(),
        snapshot != null ? snapshot.getOriginalAddressId() : null,
        snapshot != null ? snapshot.getStreet() : null,
        snapshot != null ? snapshot.getNumber() : null,
        snapshot != null ? snapshot.getDistrict() : null,
        snapshot != null ? snapshot.getCityName() : null,
        snapshot != null ? snapshot.getStateUf() : null);
  }

  @Mapping(target = "productId", source = "product.id")
  @Mapping(target = "productName", source = "product.name")
  @Mapping(
      target = "subtotal",
      expression =
          "java(orderItem.getUnitPrice().multiply(new java.math.BigDecimal(orderItem.getQuantity())))")
  OrderItemDto toItemDto(OrderItem orderItem);
}
