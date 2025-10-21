package com.orama.e_commerce.mapper;

import com.orama.e_commerce.dtos.order.CreateOrderRequestDto;
import com.orama.e_commerce.dtos.order.OrderItemDto;
import com.orama.e_commerce.dtos.order.OrderResponseDto;
import com.orama.e_commerce.models.Order;
import com.orama.e_commerce.models.OrderItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface OrderMapper {

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "discount", source = "discount")
  @Mapping(target = "cart.id", source = "cartId")
  @Mapping(target = "orderNumber", ignore = true)
  @Mapping(target = "orderDate", ignore = true)
  @Mapping(target = "subtotal", ignore = true)
  @Mapping(target = "total", ignore = true)
  @Mapping(target = "client", ignore = true)
  @Mapping(target = "cart", ignore = true)
  @Mapping(target = "items", ignore = true)
  Order toEntity(CreateOrderRequestDto dto);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "clientId", source = "client.id")
  @Mapping(target = "clientName", source = "client.name")
  OrderResponseDto toResponseDto(Order order);

  @Mapping(target = "productId", source = "product.id")
  @Mapping(target = "productName", source = "product.name")
  @Mapping(
      target = "subtotal",
      expression =
          "java(orderItem.getUnitPrice().multiply(new java.math.BigDecimal(orderItem.getQuantity())))")
  OrderItemDto toItemDto(OrderItem orderItem);
}
