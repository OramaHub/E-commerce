package com.orama.e_commerce.controller;

import com.orama.e_commerce.dtos.product_image.ProductImageRequestDto;
import com.orama.e_commerce.dtos.product_image.ProductImageResponseDto;
import com.orama.e_commerce.service.ProductImageService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/products/{productId}/images")
public class ProductImageController {

  private final ProductImageService productImageService;

  public ProductImageController(ProductImageService productImageService) {
    this.productImageService = productImageService;
  }

  @GetMapping
  public ResponseEntity<Page<ProductImageResponseDto>> getImagesByProduct(
      @PathVariable Long productId, Pageable pageable) {
    Page<ProductImageResponseDto> images =
        productImageService.getImagesByProductId(productId, pageable);
    return ResponseEntity.ok(images);
  }

  @PreAuthorize("hasRole('ADMIN')")
  @PostMapping("/single")
  public ResponseEntity<ProductImageResponseDto> addImageToProduct(
      @PathVariable Long productId,
      @RequestBody @Valid ProductImageRequestDto productImageRequestDto) {
    ProductImageResponseDto response =
        productImageService.addImageToProduct(productId, productImageRequestDto);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @PreAuthorize("hasRole('ADMIN')")
  @PostMapping("/batch")
  public ResponseEntity<List<ProductImageResponseDto>> addImagesToProduct(
      @PathVariable Long productId,
      @RequestBody @Valid List<ProductImageRequestDto> productImagesRequestDto) {
    List<ProductImageResponseDto> response =
        productImageService.addImagesToProduct(productId, productImagesRequestDto);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @PreAuthorize("hasRole('ADMIN')")
  @DeleteMapping("/{imageId}")
  public ResponseEntity<Void> deleteProductImage(
      @PathVariable Long productId, @PathVariable Long imageId) {
    productImageService.deleteProductImage(productId, imageId);
    return ResponseEntity.noContent().build();
  }
}
