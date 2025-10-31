package com.orama.e_commerce.controller;

import com.orama.e_commerce.dtos.product_image.ProductImageRequestDto;
import com.orama.e_commerce.dtos.product_image.ProductImageResponseDto;
import com.orama.e_commerce.service.ProductImageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Imagens de Produto", description = "Gestão de imagens associadas aos produtos")
@SecurityRequirement(name = "bearerAuth")
public class ProductImageController {

  private final ProductImageService productImageService;

  public ProductImageController(ProductImageService productImageService) {
    this.productImageService = productImageService;
  }

  @PreAuthorize("hasRole('ADMIN')")
  @GetMapping
  @Operation(summary = "Listar imagens de um produto")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Imagens retornadas"),
        @ApiResponse(responseCode = "401", description = "Não autenticado"),
        @ApiResponse(responseCode = "403", description = "Sem permissão"),
        @ApiResponse(responseCode = "404", description = "Produto não encontrado")
      })
  public ResponseEntity<Page<ProductImageResponseDto>> getImagesByProduct(
      @PathVariable Long productId, Pageable pageable) {
    Page<ProductImageResponseDto> images =
        productImageService.getImagesByProductId(productId, pageable);
    return ResponseEntity.ok(images);
  }

  @PreAuthorize("hasRole('ADMIN')")
  @PostMapping("/single")
  @Operation(summary = "Adicionar imagem única ao produto")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "201", description = "Imagem criada"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos"),
        @ApiResponse(responseCode = "401", description = "Não autenticado"),
        @ApiResponse(responseCode = "403", description = "Sem permissão"),
        @ApiResponse(responseCode = "404", description = "Produto não encontrado")
      })
  public ResponseEntity<ProductImageResponseDto> addImageToProduct(
      @PathVariable Long productId,
      @RequestBody @Valid ProductImageRequestDto productImageRequestDto) {
    ProductImageResponseDto response =
        productImageService.addImageToProduct(productId, productImageRequestDto);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @PreAuthorize("hasRole('ADMIN')")
  @PostMapping("/batch")
  @Operation(summary = "Adicionar múltiplas imagens ao produto")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "201", description = "Imagens criadas"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos"),
        @ApiResponse(responseCode = "401", description = "Não autenticado"),
        @ApiResponse(responseCode = "403", description = "Sem permissão"),
        @ApiResponse(responseCode = "404", description = "Produto não encontrado")
      })
  public ResponseEntity<List<ProductImageResponseDto>> addImagesToProduct(
      @PathVariable Long productId,
      @RequestBody @Valid List<ProductImageRequestDto> productImagesRequestDto) {
    List<ProductImageResponseDto> response =
        productImageService.addImagesToProduct(productId, productImagesRequestDto);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @PreAuthorize("hasRole('ADMIN')")
  @DeleteMapping("/{imageId}")
  @Operation(summary = "Remover imagem do produto")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "204", description = "Imagem removida"),
        @ApiResponse(responseCode = "401", description = "Não autenticado"),
        @ApiResponse(responseCode = "403", description = "Sem permissão"),
        @ApiResponse(responseCode = "404", description = "Produto ou imagem não encontrado")
      })
  public ResponseEntity<Void> deleteProductImage(
      @PathVariable Long productId, @PathVariable Long imageId) {
    productImageService.deleteProductImage(productId, imageId);
    return ResponseEntity.noContent().build();
  }
}
