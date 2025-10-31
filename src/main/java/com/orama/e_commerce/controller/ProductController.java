package com.orama.e_commerce.controller;

import com.orama.e_commerce.dtos.product.*;
import com.orama.e_commerce.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/products")
@Tag(name = "Produtos", description = "Catálogo e estoque de produtos")
public class ProductController {

  private final ProductService productService;

  public ProductController(ProductService productService) {
    this.productService = productService;
  }

  @GetMapping("/{id}")
  @Operation(summary = "Buscar produto por ID")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Produto encontrado"),
        @ApiResponse(responseCode = "404", description = "Produto não encontrado")
      })
  public ResponseEntity<ProductResponseDto> findById(@PathVariable Long id) {
    ProductResponseDto productResponseDto = productService.getById(id);
    return new ResponseEntity<>(productResponseDto, HttpStatus.OK);
  }

  @GetMapping
  @Operation(
      summary = "Listar produtos ativos",
      description = "Retorna produtos disponíveis com paginação")
  @ApiResponses(@ApiResponse(responseCode = "200", description = "Lista retornada"))
  public ResponseEntity<Page<ProductResponseDto>> findAllActiveProducts(Pageable pageable) {
    Page<ProductResponseDto> page = productService.getAllActiveProducts(pageable);
    return new ResponseEntity<>(page, HttpStatus.OK);
  }

  @PreAuthorize("hasRole('ADMIN')")
  @GetMapping("/all")
  @Operation(summary = "Listar todos os produtos", description = "Inclui ativos e inativos")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Lista retornada"),
        @ApiResponse(responseCode = "401", description = "Não autenticado"),
        @ApiResponse(responseCode = "403", description = "Sem permissão")
      })
  @SecurityRequirement(name = "bearerAuth")
  public ResponseEntity<Page<ProductResponseDto>> findAll(Pageable pageable) {
    Page<ProductResponseDto> page = productService.getAll(pageable);
    return new ResponseEntity<>(page, HttpStatus.OK);
  }

  @GetMapping("/name")
  @Operation(summary = "Buscar produtos por nome")
  @ApiResponses(@ApiResponse(responseCode = "200", description = "Lista filtrada retornada"))
  public ResponseEntity<Page<ProductResponseDto>> findAllByName(
      @RequestParam String name, Pageable pageable) {
    Page<ProductResponseDto> page = productService.getAllByName(name, pageable);
    return new ResponseEntity<>(page, HttpStatus.OK);
  }

  @PreAuthorize("hasRole('ADMIN')")
  @PostMapping
  @Operation(summary = "Criar produto")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "201", description = "Produto criado"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos"),
        @ApiResponse(responseCode = "401", description = "Não autenticado"),
        @ApiResponse(responseCode = "403", description = "Sem permissão")
      })
  @SecurityRequirement(name = "bearerAuth")
  public ResponseEntity<ProductResponseDto> createProduct(
      @Valid @RequestBody ProductRequestDto productRequestDto) {
    ProductResponseDto productResponseDto = productService.createProduct(productRequestDto);
    return ResponseEntity.status(HttpStatus.CREATED).body(productResponseDto);
  }

  @PreAuthorize("hasRole('ADMIN')")
  @PutMapping("/{id}")
  @Operation(summary = "Atualizar produto")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Produto atualizado"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos"),
        @ApiResponse(responseCode = "401", description = "Não autenticado"),
        @ApiResponse(responseCode = "403", description = "Sem permissão"),
        @ApiResponse(responseCode = "404", description = "Produto não encontrado")
      })
  @SecurityRequirement(name = "bearerAuth")
  public ResponseEntity<ProductResponseDto> updateProduct(
      @PathVariable Long id, @Valid @RequestBody ProductUpdateRequestDto productUpdateRequestDto) {
    ProductResponseDto productResponseDto =
        productService.updateProduct(id, productUpdateRequestDto);
    return ResponseEntity.ok(productResponseDto);
  }

  @PreAuthorize("hasRole('ADMIN')")
  @PatchMapping("/{id}/stock/adjust")
  @Operation(summary = "Ajustar estoque", description = "Incrementa ou decrementa o estoque atual")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Estoque ajustado"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos"),
        @ApiResponse(responseCode = "401", description = "Não autenticado"),
        @ApiResponse(responseCode = "403", description = "Sem permissão"),
        @ApiResponse(responseCode = "404", description = "Produto não encontrado")
      })
  @SecurityRequirement(name = "bearerAuth")
  public ResponseEntity<ProductResponseDto> adjustStock(
      @PathVariable Long id, @RequestBody @Valid ProductStockAdjustmentDto stockAdjustmentDto) {
    ProductResponseDto response = productService.stockAdjustment(id, stockAdjustmentDto);
    return ResponseEntity.ok(response);
  }

  @PreAuthorize("hasRole('ADMIN')")
  @PatchMapping("/{id}/stock/set")
  @Operation(summary = "Definir estoque", description = "Define um novo estoque absoluto")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Estoque atualizado"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos"),
        @ApiResponse(responseCode = "401", description = "Não autenticado"),
        @ApiResponse(responseCode = "403", description = "Sem permissão"),
        @ApiResponse(responseCode = "404", description = "Produto não encontrado")
      })
  @SecurityRequirement(name = "bearerAuth")
  public ResponseEntity<ProductResponseDto> setStock(
      @PathVariable Long id, @RequestBody @Valid ProductStockSetDto stockSetDto) {
    ProductResponseDto response = productService.stockSet(id, stockSetDto);
    return ResponseEntity.ok(response);
  }

  @PreAuthorize("hasRole('ADMIN')")
  @PatchMapping("/{id}/deactivate")
  @Operation(summary = "Desativar produto")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "204", description = "Produto desativado"),
        @ApiResponse(responseCode = "401", description = "Não autenticado"),
        @ApiResponse(responseCode = "403", description = "Sem permissão"),
        @ApiResponse(responseCode = "404", description = "Produto não encontrado")
      })
  @SecurityRequirement(name = "bearerAuth")
  public ResponseEntity<Void> deactivateProduct(@PathVariable Long id) {
    productService.deactivateProduct(id);
    return ResponseEntity.noContent().build();
  }

  @PreAuthorize("hasRole('ADMIN')")
  @PatchMapping("/{id}/activate")
  @Operation(summary = "Reativar produto")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "204", description = "Produto reativado"),
        @ApiResponse(responseCode = "401", description = "Não autenticado"),
        @ApiResponse(responseCode = "403", description = "Sem permissão"),
        @ApiResponse(responseCode = "404", description = "Produto não encontrado")
      })
  @SecurityRequirement(name = "bearerAuth")
  public ResponseEntity<Void> activateProduct(@PathVariable Long id) {
    productService.activateProduct(id);
    return ResponseEntity.noContent().build();
  }
}
