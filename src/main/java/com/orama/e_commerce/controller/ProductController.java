package com.orama.e_commerce.controller;

import com.orama.e_commerce.dtos.product.*;
import com.orama.e_commerce.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/products")
public class ProductController {

  private final ProductService productService;

  public ProductController(ProductService productService) {
    this.productService = productService;
  }

  @PreAuthorize("hasRole('ADMIN')")
  @PostMapping
  public ResponseEntity<ProductResponseDto> createProduct(
      @Valid @RequestBody ProductRequestDto productRequestDto) {
    ProductResponseDto productResponseDto = productService.createProduct(productRequestDto);
    return ResponseEntity.status(HttpStatus.CREATED).body(productResponseDto);
  }

  @PreAuthorize("hasRole('ADMIN')")
  @PutMapping("/{id}")
  public ResponseEntity<ProductResponseDto> updateProduct(
      @PathVariable Long id, @Valid @RequestBody ProductUpdateRequestDto productUpdateRequestDto) {
    ProductResponseDto productResponseDto =
        productService.updateProduct(id, productUpdateRequestDto);
    return ResponseEntity.ok(productResponseDto);
  }

  @PreAuthorize("hasRole('ADMIN')")
  @PatchMapping("/{id}/stock/adjust")
  public ResponseEntity<ProductResponseDto> adjustStock(
      @PathVariable Long id, @RequestBody @Valid ProductStockAdjustmentDto stockAdjustmentDto) {
    ProductResponseDto response = productService.stockAdjustment(id, stockAdjustmentDto);
    return ResponseEntity.ok(response);
  }

  @PreAuthorize("hasRole('ADMIN')")
  @PatchMapping("/{id}/stock/set")
  public ResponseEntity<ProductResponseDto> setStock(
      @PathVariable Long id, @RequestBody @Valid ProductStockSetDto stockSetDto) {
    ProductResponseDto response = productService.stockSet(id, stockSetDto);
    return ResponseEntity.ok(response);
  }

  @PreAuthorize("hasRole('ADMIN')")
  @PatchMapping("/{id}/deactivate")
  public ResponseEntity<Void> deactivateProduct(@PathVariable Long id) {
    productService.deactivateProduct(id);
    return ResponseEntity.noContent().build();
  }

  @PreAuthorize("hasRole('ADMIN')")
  @PatchMapping("/{id}/activate")
  public ResponseEntity<Void> activateProduct(@PathVariable Long id) {
    productService.activateProduct(id);
    return ResponseEntity.noContent().build();
  }
}
