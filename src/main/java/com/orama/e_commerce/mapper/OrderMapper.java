package com.orama.e_commerce.mapper;

import com.orama.e_commerce.dtos.order.CreateOrderRequestDto;
import com.orama.e_commerce.dtos.order.OrderItemDto;
import com.orama.e_commerce.dtos.order.OrderResponseDto;
import com.orama.e_commerce.models.Address;
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
  @Mapping(target = "deliveryAddress", ignore = true)
  @Mapping(target = "shippingAddress", ignore = true)
  @Mapping(target = "paymentId", ignore = true)
  @Mapping(target = "paymentMethod", ignore = true)
  Order toEntity(CreateOrderRequestDto dto);

  default OrderResponseDto toResponseDto(Order order) {
    if (order == null) {
      return null;
    }

    OrderShippingAddress snapshot = order.getShippingAddress();
    Address address = order.getDeliveryAddress();

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
        snapshot != null
            ? snapshot.getOriginalAddressId()
            : address != null ? address.getId() : null,
        snapshot != null ? snapshot.getStreet() : address != null ? address.getStreet() : null,
        snapshot != null ? snapshot.getNumber() : address != null ? address.getNumber() : null,
        snapshot != null ? snapshot.getDistrict() : address != null ? address.getDistrict() : null,
        snapshot != null ? snapshot.getCityName() : resolveCityName(address),
        snapshot != null ? snapshot.getStateUf() : resolveStateUf(address));
  }

  @Mapping(target = "productId", source = "product.id")
  @Mapping(target = "productName", source = "product.name")
  @Mapping(
      target = "subtotal",
      expression =
          "java(orderItem.getUnitPrice().multiply(new java.math.BigDecimal(orderItem.getQuantity())))")
  OrderItemDto toItemDto(OrderItem orderItem);

  private String resolveCityName(Address address) {
    if (address == null) {
      return null;
    }
    if (hasText(address.getCityName())) {
      return address.getCityName();
    }
    return address.getCity() != null ? address.getCity().getName() : null;
  }

  private String resolveStateUf(Address address) {
    if (address == null) {
      return null;
    }
    if (hasText(address.getStateUf())) {
      return address.getStateUf();
    }
    return address.getCity() != null && address.getCity().getState() != null
        ? address.getCity().getState().getAbbreviation()
        : null;
  }

  private boolean hasText(String value) {
    return value != null && !value.isBlank();
  }
}
