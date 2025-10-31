package com.orama.e_commerce.controller;

import com.orama.e_commerce.dtos.order.CreateOrderRequestDto;
import com.orama.e_commerce.dtos.order.OrderResponseDto;
import com.orama.e_commerce.enums.OrderStatus;
import com.orama.e_commerce.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
@Tag(name = "Pedidos", description = "Criação e gerenciamento de pedidos")
public class OrderController {

  private final OrderService orderService;

  public OrderController(OrderService orderService) {
    this.orderService = orderService;
  }

  @PostMapping
  @Operation(summary = "Criar pedido", description = "Gera um pedido a partir do carrinho/itens")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "201", description = "Pedido criado"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos")
      })
  public ResponseEntity<OrderResponseDto> createOrder(
      @Valid @RequestBody CreateOrderRequestDto requestDto) {
    OrderResponseDto dto = orderService.createOrder(requestDto);
    return ResponseEntity.status(HttpStatus.CREATED).body(dto);
  }

  @GetMapping("/{id}")
  @Operation(summary = "Buscar pedido por ID")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Pedido encontrado"),
        @ApiResponse(responseCode = "404", description = "Pedido não encontrado")
      })
  public ResponseEntity<OrderResponseDto> getOrderById(@PathVariable Long id) {
    OrderResponseDto dto = orderService.getOrderById(id);
    return ResponseEntity.ok(dto);
  }

  @GetMapping
  @Operation(summary = "Listar pedidos")
  @ApiResponses(@ApiResponse(responseCode = "200", description = "Lista de pedidos retornada"))
  public ResponseEntity<List<OrderResponseDto>> getAllOrders() {
    List<OrderResponseDto> orders = orderService.getAllOrders();
    return ResponseEntity.ok(orders);
  }

  @GetMapping("/client/{clientId}")
  @Operation(summary = "Listar pedidos por cliente")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Lista de pedidos retornada"),
        @ApiResponse(responseCode = "404", description = "Cliente não encontrado")
      })
  public ResponseEntity<List<OrderResponseDto>> getOrdersByClient(@PathVariable Long clientId) {
    List<OrderResponseDto> orders = orderService.getOrdersByClient(clientId);
    return ResponseEntity.ok(orders);
  }

  @GetMapping("/number/{orderNumber}")
  @Operation(summary = "Buscar pedido por número")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Pedido encontrado"),
        @ApiResponse(responseCode = "404", description = "Pedido não encontrado")
      })
  public ResponseEntity<OrderResponseDto> getOrderByOrderNumber(@PathVariable String orderNumber) {
    OrderResponseDto dto = orderService.getOrderByOrderNumber(orderNumber);
    return ResponseEntity.ok(dto);
  }

  @PatchMapping("/{id}/status")
  @Operation(summary = "Atualizar status do pedido")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Status atualizado"),
        @ApiResponse(responseCode = "400", description = "Status inválido"),
        @ApiResponse(responseCode = "404", description = "Pedido não encontrado")
      })
  public ResponseEntity<OrderResponseDto> updateOrderStatus(
      @PathVariable Long id, @RequestParam OrderStatus status) {
    OrderResponseDto dto = orderService.updateOrderStatus(id, status);
    return ResponseEntity.ok(dto);
  }

  @PatchMapping("/{id}/cancel")
  @Operation(summary = "Cancelar pedido")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Pedido cancelado"),
        @ApiResponse(responseCode = "404", description = "Pedido não encontrado")
      })
  public ResponseEntity<OrderResponseDto> cancelOrder(@PathVariable Long id) {
    OrderResponseDto dto = orderService.cancelOrder(id);
    return ResponseEntity.ok(dto);
  }
}
