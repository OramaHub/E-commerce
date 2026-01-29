package com.orama.e_commerce.controller;

import com.orama.e_commerce.dtos.cart.AddItemToCartRequestDto;
import com.orama.e_commerce.dtos.cart.CartResponseDto;
import com.orama.e_commerce.dtos.cart.UpdateCartItemRequestDto;
import com.orama.e_commerce.service.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/carts")
@Tag(name = "Carrinhos")
public class CartController {

  private final CartService cartService;

  public CartController(CartService cartService) {
    this.cartService = cartService;
  }

  @PreAuthorize("#clientId == authentication.details['id'] or hasRole('ADMIN')")
  @GetMapping("/client/{clientId}/active")
  @Operation(summary = "Busca ou cria o carrinho ativo do cliente")
  public ResponseEntity<CartResponseDto> getOrCreateActiveCart(@PathVariable Long clientId) {
    CartResponseDto dto = cartService.getOrCreateActiveCart(clientId);
    return ResponseEntity.ok(dto);
  }

  @PreAuthorize("hasRole('ADMIN')")
  @GetMapping("/{id}")
  @Operation(summary = "Busca carrinho pelo id")
  public ResponseEntity<CartResponseDto> getCartById(@PathVariable Long id) {
    CartResponseDto dto = cartService.getCartById(id);
    return ResponseEntity.ok(dto);
  }

  @PreAuthorize("#clientId == authentication.details['id'] or hasRole('ADMIN')")
  @PostMapping("/client/{clientId}/items")
  @Operation(summary = "Adiciona item ao carrinho")
  public ResponseEntity<CartResponseDto> addItemToCart(
      @PathVariable Long clientId, @Valid @RequestBody AddItemToCartRequestDto requestDto) {
    CartResponseDto dto = cartService.addItemToCart(clientId, requestDto);
    return ResponseEntity.status(HttpStatus.CREATED).body(dto);
  }

  @PreAuthorize("#clientId == authentication.details['id'] or hasRole('ADMIN')")
  @PutMapping("/client/{clientId}/items/{cartItemId}")
  @Operation(summary = "Atualiza quantidade de um item do carrinho")
  public ResponseEntity<CartResponseDto> updateCartItemQuantity(
      @PathVariable Long clientId,
      @PathVariable Long cartItemId,
      @Valid @RequestBody UpdateCartItemRequestDto requestDto) {
    CartResponseDto dto = cartService.updateCartItemQuantity(clientId, cartItemId, requestDto);
    return ResponseEntity.ok(dto);
  }

  @PreAuthorize("#clientId == authentication.details['id'] or hasRole('ADMIN')")
  @DeleteMapping("/client/{clientId}/items/{cartItemId}")
  @Operation(summary = "Remove item do carrinho")
  public ResponseEntity<CartResponseDto> removeItemFromCart(
      @PathVariable Long clientId, @PathVariable Long cartItemId) {
    CartResponseDto dto = cartService.removeItemFromCart(clientId, cartItemId);
    return ResponseEntity.ok(dto);
  }

  @PreAuthorize("#clientId == authentication.details['id'] or hasRole('ADMIN')")
  @DeleteMapping("/client/{clientId}/clear")
  @Operation(summary = "Esvazia o carrinho do cliente")
  public ResponseEntity<Void> clearCart(@PathVariable Long clientId) {
    cartService.clearCart(clientId);
    return ResponseEntity.noContent().build();
  }
}
