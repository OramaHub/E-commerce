package com.orama.e_commerce.controller;

import com.orama.e_commerce.dtos.order.CreateOrderRequestDto;
import com.orama.e_commerce.dtos.order.OrderResponseDto;
import com.orama.e_commerce.enums.OrderStatus;
import com.orama.e_commerce.service.OrderService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

  private final OrderService orderService;

  public OrderController(OrderService orderService) {
    this.orderService = orderService;
  }

  @PreAuthorize("authentication.details['id'] != null")
  @PostMapping
  public ResponseEntity<OrderResponseDto> createOrder(
      @Valid @RequestBody CreateOrderRequestDto requestDto) {
    OrderResponseDto dto = orderService.createOrder(requestDto);
    return ResponseEntity.status(HttpStatus.CREATED).body(dto);
  }

  @PreAuthorize("hasRole('ADMIN')")
  @GetMapping("/{id}")
  public ResponseEntity<OrderResponseDto> getOrderById(@PathVariable Long id) {
    OrderResponseDto dto = orderService.getOrderById(id);
    return ResponseEntity.ok(dto);
  }

  @PreAuthorize("hasRole('ADMIN')")
  @GetMapping
  public ResponseEntity<List<OrderResponseDto>> getAllOrders() {
    List<OrderResponseDto> orders = orderService.getAllOrders();
    return ResponseEntity.ok(orders);
  }

  @PreAuthorize("#clientId == authentication.details['id'] or hasRole('ADMIN')")
  @GetMapping("/client/{clientId}")
  public ResponseEntity<List<OrderResponseDto>> getOrdersByClient(@PathVariable Long clientId) {
    List<OrderResponseDto> orders = orderService.getOrdersByClient(clientId);
    return ResponseEntity.ok(orders);
  }

  @PreAuthorize("hasRole('ADMIN')")
  @GetMapping("/number/{orderNumber}")
  public ResponseEntity<OrderResponseDto> getOrderByOrderNumber(@PathVariable String orderNumber) {
    OrderResponseDto dto = orderService.getOrderByOrderNumber(orderNumber);
    return ResponseEntity.ok(dto);
  }

  @PreAuthorize("hasRole('ADMIN')")
  @PatchMapping("/{id}/status")
  public ResponseEntity<OrderResponseDto> updateOrderStatus(
      @PathVariable Long id, @RequestParam OrderStatus status) {
    OrderResponseDto dto = orderService.updateOrderStatus(id, status);
    return ResponseEntity.ok(dto);
  }

  @PreAuthorize("hasRole('ADMIN') or authentication.details['id'] != null")
  @PatchMapping("/{id}/cancel")
  public ResponseEntity<OrderResponseDto> cancelOrder(@PathVariable Long id) {
    OrderResponseDto dto = orderService.cancelOrder(id);
    return ResponseEntity.ok(dto);
  }
}
