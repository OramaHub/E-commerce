package com.orama.e_commerce.controller;

import com.orama.e_commerce.dtos.custom_order.CreateCustomOrderRequestDto;
import com.orama.e_commerce.dtos.custom_order.CustomOrderResponseDto;
import com.orama.e_commerce.enums.CustomOrderStatus;
import com.orama.e_commerce.service.CustomOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/custom-orders")
@Tag(name = "Pedidos Personalizados")
public class CustomOrderController {

  private final CustomOrderService customOrderService;

  public CustomOrderController(CustomOrderService customOrderService) {
    this.customOrderService = customOrderService;
  }

  @PreAuthorize("#clientId == authentication.details['id'] or hasRole('ADMIN')")
  @PostMapping("/client/{clientId}")
  @Operation(summary = "Cria um pedido personalizado de boné")
  public ResponseEntity<CustomOrderResponseDto> create(
      @PathVariable Long clientId, @Valid @RequestBody CreateCustomOrderRequestDto dto) {
    CustomOrderResponseDto response = customOrderService.createCustomOrder(dto, clientId);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @PreAuthorize("authentication.details['id'] != null or hasRole('ADMIN')")
  @GetMapping("/{id}")
  @Operation(summary = "Busca pedido personalizado pelo id")
  public ResponseEntity<CustomOrderResponseDto> getById(@PathVariable Long id) {
    CustomOrderResponseDto response = customOrderService.getById(id);
    return ResponseEntity.ok(response);
  }

  @PreAuthorize("#clientId == authentication.details['id'] or hasRole('ADMIN')")
  @GetMapping("/client/{clientId}")
  @Operation(summary = "Lista pedidos personalizados de um cliente")
  public ResponseEntity<List<CustomOrderResponseDto>> getByClient(@PathVariable Long clientId) {
    List<CustomOrderResponseDto> orders = customOrderService.getByClient(clientId);
    return ResponseEntity.ok(orders);
  }

  @PreAuthorize("hasRole('ADMIN')")
  @GetMapping
  @Operation(summary = "Lista todos os pedidos personalizados")
  public ResponseEntity<List<CustomOrderResponseDto>> getAll() {
    List<CustomOrderResponseDto> orders = customOrderService.getAllOrders();
    return ResponseEntity.ok(orders);
  }

  @PreAuthorize("hasRole('ADMIN')")
  @PatchMapping("/{id}/status")
  @Operation(summary = "Atualiza o status do pedido personalizado")
  public ResponseEntity<CustomOrderResponseDto> updateStatus(
      @PathVariable Long id, @RequestParam CustomOrderStatus status) {
    CustomOrderResponseDto response = customOrderService.updateStatus(id, status);
    return ResponseEntity.ok(response);
  }

  @PreAuthorize("authentication.details['id'] != null")
  @PostMapping(value = "/{id}/logo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @Operation(summary = "Faz upload da logo do cliente para o pedido personalizado")
  public ResponseEntity<CustomOrderResponseDto> uploadLogo(
      @PathVariable Long id, @RequestParam("file") MultipartFile file) {
    CustomOrderResponseDto response = customOrderService.uploadLogo(id, file);
    return ResponseEntity.ok(response);
  }

  @PreAuthorize("authentication.details['id'] != null")
  @PostMapping(value = "/{id}/preview", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @Operation(summary = "Faz upload da imagem de preview do boné personalizado")
  public ResponseEntity<CustomOrderResponseDto> uploadPreview(
      @PathVariable Long id, @RequestParam("file") MultipartFile file) {
    CustomOrderResponseDto response = customOrderService.uploadPreview(id, file);
    return ResponseEntity.ok(response);
  }
}
