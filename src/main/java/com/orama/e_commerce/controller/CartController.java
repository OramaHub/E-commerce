package com.orama.e_commerce.controller;

import com.orama.e_commerce.dtos.cart.AddItemToCartRequestDto;
import com.orama.e_commerce.dtos.cart.CartResponseDto;
import com.orama.e_commerce.dtos.cart.UpdateCartItemRequestDto;
import com.orama.e_commerce.service.CartService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/carts")
public class CartController {

  private final CartService cartService;

  public CartController(CartService cartService) {
    this.cartService = cartService;
  }

  @GetMapping("/client/{clientId}/active")
  public ResponseEntity<CartResponseDto> getOrCreateActiveCart(@PathVariable Long clientId) {
    CartResponseDto dto = cartService.getOrCreateActiveCart(clientId);
    return ResponseEntity.ok(dto);
  }

  @GetMapping("/{id}")
  public ResponseEntity<CartResponseDto> getCartById(@PathVariable Long id) {
    CartResponseDto dto = cartService.getCartById(id);
    return ResponseEntity.ok(dto);
  }

  @PostMapping("/client/{clientId}/items")
  public ResponseEntity<CartResponseDto> addItemToCart(
      @PathVariable Long clientId, @Valid @RequestBody AddItemToCartRequestDto requestDto) {
    CartResponseDto dto = cartService.addItemToCart(clientId, requestDto);
    return ResponseEntity.status(HttpStatus.CREATED).body(dto);
  }

  @PutMapping("/client/{clientId}/items/{cartItemId}")
  public ResponseEntity<CartResponseDto> updateCartItemQuantity(
      @PathVariable Long clientId,
      @PathVariable Long cartItemId,
      @Valid @RequestBody UpdateCartItemRequestDto requestDto) {
    CartResponseDto dto = cartService.updateCartItemQuantity(clientId, cartItemId, requestDto);
    return ResponseEntity.ok(dto);
  }

  @DeleteMapping("/client/{clientId}/items/{cartItemId}")
  public ResponseEntity<CartResponseDto> removeItemFromCart(
      @PathVariable Long clientId, @PathVariable Long cartItemId) {
    CartResponseDto dto = cartService.removeItemFromCart(clientId, cartItemId);
    return ResponseEntity.ok(dto);
  }

  @DeleteMapping("/client/{clientId}/clear")
  public ResponseEntity<Void> clearCart(@PathVariable Long clientId) {
    cartService.clearCart(clientId);
    return ResponseEntity.noContent().build();
  }
}
