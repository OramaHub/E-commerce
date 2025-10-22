package com.orama.e_commerce.mapper;

import com.orama.e_commerce.dtos.cart.CartItemDto;
import com.orama.e_commerce.dtos.cart.CartResponseDto;
import com.orama.e_commerce.models.Cart;
import com.orama.e_commerce.models.CartItem;
import java.math.BigDecimal;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CartMapper {

  @Mapping(target = "clientId", source = "client.id")
  @Mapping(target = "clientName", source = "client.name")
  @Mapping(
      target = "total",
      expression =
          "java(cart.getItems() != null ? calculateTotal(cart.getItems()) : java.math.BigDecimal.ZERO)")
  CartResponseDto toResponseDto(Cart cart);

  @Mapping(target = "productId", source = "product.id")
  @Mapping(target = "productName", source = "product.name")
  @Mapping(
      target = "subtotal",
      expression =
          "java(cartItem.getUnitPrice().multiply(new java.math.BigDecimal(cartItem.getQuantity())))")
  CartItemDto toItemDto(CartItem cartItem);

  default BigDecimal calculateTotal(java.util.List<CartItem> items) {
    return items.stream()
        .map(item -> item.getUnitPrice().multiply(new BigDecimal(item.getQuantity())))
        .reduce(BigDecimal.ZERO, BigDecimal::add);
  }
}
