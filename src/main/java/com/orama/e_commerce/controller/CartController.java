package com.orama.e_commerce.controller;

import com.orama.e_commerce.dtos.cart.AddItemToCartRequestDto;
import com.orama.e_commerce.dtos.cart.CartResponseDto;
import com.orama.e_commerce.dtos.cart.UpdateCartItemRequestDto;
import com.orama.e_commerce.service.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/carts")
@Tag(name = "Carrinho", description = "Operações de carrinho de compras")
public class CartController {

  private final CartService cartService;

  public CartController(CartService cartService) {
    this.cartService = cartService;
  }

  @GetMapping("/client/{clientId}/active")
  @Operation(
      summary = "Obter ou criar carrinho ativo",
      description = "Recupera o carrinho aberto do cliente ou cria um novo")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Carrinho retornado"),
        @ApiResponse(responseCode = "404", description = "Cliente não encontrado")
      })
  public ResponseEntity<CartResponseDto> getOrCreateActiveCart(@PathVariable Long clientId) {
    CartResponseDto dto = cartService.getOrCreateActiveCart(clientId);
    return ResponseEntity.ok(dto);
  }

  @GetMapping("/{id}")
  @Operation(summary = "Buscar carrinho por ID")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Carrinho encontrado"),
        @ApiResponse(responseCode = "404", description = "Carrinho não encontrado")
      })
  public ResponseEntity<CartResponseDto> getCartById(@PathVariable Long id) {
    CartResponseDto dto = cartService.getCartById(id);
    return ResponseEntity.ok(dto);
  }

  @PostMapping("/client/{clientId}/items")
  @Operation(summary = "Adicionar item ao carrinho")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "201", description = "Item adicionado"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos"),
        @ApiResponse(responseCode = "404", description = "Cliente ou produto não encontrado")
      })
  public ResponseEntity<CartResponseDto> addItemToCart(
      @PathVariable Long clientId, @Valid @RequestBody AddItemToCartRequestDto requestDto) {
    CartResponseDto dto = cartService.addItemToCart(clientId, requestDto);
    return ResponseEntity.status(HttpStatus.CREATED).body(dto);
  }

  @PutMapping("/client/{clientId}/items/{cartItemId}")
  @Operation(summary = "Atualizar quantidade de item")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Item atualizado"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos"),
        @ApiResponse(responseCode = "404", description = "Item ou carrinho não encontrado")
      })
  public ResponseEntity<CartResponseDto> updateCartItemQuantity(
      @PathVariable Long clientId,
      @PathVariable Long cartItemId,
      @Valid @RequestBody UpdateCartItemRequestDto requestDto) {
    CartResponseDto dto = cartService.updateCartItemQuantity(clientId, cartItemId, requestDto);
    return ResponseEntity.ok(dto);
  }

  @DeleteMapping("/client/{clientId}/items/{cartItemId}")
  @Operation(summary = "Remover item do carrinho")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Item removido"),
        @ApiResponse(responseCode = "404", description = "Item ou carrinho não encontrado")
      })
  public ResponseEntity<CartResponseDto> removeItemFromCart(
      @PathVariable Long clientId, @PathVariable Long cartItemId) {
    CartResponseDto dto = cartService.removeItemFromCart(clientId, cartItemId);
    return ResponseEntity.ok(dto);
  }

  @DeleteMapping("/client/{clientId}/clear")
  @Operation(summary = "Esvaziar carrinho")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "204", description = "Carrinho esvaziado"),
        @ApiResponse(responseCode = "404", description = "Carrinho não encontrado")
      })
  public ResponseEntity<Void> clearCart(@PathVariable Long clientId) {
    cartService.clearCart(clientId);
    return ResponseEntity.noContent().build();
  }
}
