package com.orama.e_commerce.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.orama.e_commerce.dtos.product.*;
import com.orama.e_commerce.exceptions.product.ProductAlreadyActiveException;
import com.orama.e_commerce.exceptions.product.ProductAlreadyInactiveException;
import com.orama.e_commerce.exceptions.product.ProductNotFoundException;
import com.orama.e_commerce.exceptions.product.StockNegativeException;
import com.orama.e_commerce.mapper.ProductMapper;
import com.orama.e_commerce.models.Product;
import com.orama.e_commerce.repository.ProductRepository;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

  @Mock private ProductRepository productRepository;
  @Mock private ProductMapper productMapper;

  @InjectMocks private ProductService productService;

  private Product product;
  private ProductResponseDto productResponseDto;
  private Pageable pageable;

  @BeforeEach
  void setUp() {
    product = new Product();
    product.setId(1L);
    product.setName("Produto Teste");
    product.setDescription("Descrição do produto");
    product.setPrice(new BigDecimal("99.90"));
    product.setStock(100);
    product.setActive(true);
    product.setImages(new ArrayList<>());

    productResponseDto =
        new ProductResponseDto(
            1L,
            "Produto Teste",
            "Descrição do produto",
            new BigDecimal("99.90"),
            100,
            Collections.emptyList());

    pageable = PageRequest.of(0, 10);
  }

  @Test
  void shouldReturnProductById() {
    when(productRepository.findById(1L)).thenReturn(Optional.of(product));
    when(productMapper.toResponseDto(product)).thenReturn(productResponseDto);

    ProductResponseDto result = productService.getById(1L);

    assertNotNull(result);
    assertEquals(1L, result.id());
    assertEquals("Produto Teste", result.name());
    verify(productRepository).findById(1L);
  }

  @Test
  void shouldThrowProductNotFoundExceptionWhenProductInactive() {
    product.setActive(false);
    when(productRepository.findById(1L)).thenReturn(Optional.of(product));

    assertThrows(ProductNotFoundException.class, () -> productService.getById(1L));
  }

  @Test
  void shouldThrowProductNotFoundExceptionWhenProductNotFound() {
    when(productRepository.findById(99L)).thenReturn(Optional.empty());

    assertThrows(ProductNotFoundException.class, () -> productService.getById(99L));
  }

  @Test
  void shouldReturnPageOfActiveProducts() {
    Page<Product> productPage = new PageImpl<>(List.of(product));
    when(productRepository.findByActiveTrue(pageable)).thenReturn(productPage);
    when(productMapper.toResponseDto(product)).thenReturn(productResponseDto);

    Page<ProductResponseDto> result = productService.getAllActiveProducts(pageable);

    assertNotNull(result);
    assertEquals(1, result.getTotalElements());
    verify(productRepository).findByActiveTrue(pageable);
  }

  @Test
  void shouldReturnAllProducts() {
    Page<Product> productPage = new PageImpl<>(List.of(product));
    when(productRepository.findAll(pageable)).thenReturn(productPage);
    when(productMapper.toResponseDto(product)).thenReturn(productResponseDto);

    Page<ProductResponseDto> result = productService.getAll(pageable);

    assertNotNull(result);
    assertEquals(1, result.getTotalElements());
    verify(productRepository).findAll(pageable);
  }

  @Test
  void shouldReturnProductsByName() {
    Page<Product> productPage = new PageImpl<>(List.of(product));
    when(productRepository.findByNameContainingIgnoreCaseAndActiveTrue("Produto", pageable))
        .thenReturn(productPage);
    when(productMapper.toResponseDto(product)).thenReturn(productResponseDto);

    Page<ProductResponseDto> result = productService.getAllByName("Produto", pageable);

    assertNotNull(result);
    assertEquals(1, result.getTotalElements());
    verify(productRepository).findByNameContainingIgnoreCaseAndActiveTrue("Produto", pageable);
  }

  @Test
  void shouldCreateProduct() {
    ProductRequestDto requestDto =
        new ProductRequestDto("Produto Teste", "Descrição", new BigDecimal("99.90"), 100);

    when(productMapper.toEntity(requestDto)).thenReturn(product);
    when(productRepository.save(any(Product.class))).thenReturn(product);
    when(productMapper.toResponseDto(product)).thenReturn(productResponseDto);

    ProductResponseDto result = productService.createProduct(requestDto);

    assertNotNull(result);
    assertEquals("Produto Teste", result.name());
    assertTrue(product.getActive());
    verify(productRepository).save(any(Product.class));
  }

  @Test
  void shouldCreateProductWithNullImages() {
    product.setImages(null);
    ProductRequestDto requestDto =
        new ProductRequestDto("Produto Teste", "Descrição", new BigDecimal("99.90"), 100);

    when(productMapper.toEntity(requestDto)).thenReturn(product);
    when(productRepository.save(any(Product.class))).thenReturn(product);
    when(productMapper.toResponseDto(product)).thenReturn(productResponseDto);

    ProductResponseDto result = productService.createProduct(requestDto);

    assertNotNull(result);
    assertNotNull(product.getImages());
    verify(productRepository).save(any(Product.class));
  }

  @Test
  void shouldUpdateProduct() {
    ProductUpdateRequestDto updateRequestDto =
        new ProductUpdateRequestDto(
            "Produto Atualizado", "Nova descrição", new BigDecimal("149.90"));

    when(productRepository.findById(1L)).thenReturn(Optional.of(product));
    when(productRepository.save(any(Product.class))).thenReturn(product);
    when(productMapper.toResponseDto(product)).thenReturn(productResponseDto);

    ProductResponseDto result = productService.updateProduct(1L, updateRequestDto);

    assertNotNull(result);
    verify(productMapper).updateDto(updateRequestDto, product);
    verify(productRepository).save(product);
  }

  @Test
  void shouldAdjustStockPositive() {
    ProductStockAdjustmentDto adjustmentDto = new ProductStockAdjustmentDto(10);

    when(productRepository.findById(1L)).thenReturn(Optional.of(product));
    when(productRepository.save(any(Product.class))).thenReturn(product);
    when(productMapper.toResponseDto(product)).thenReturn(productResponseDto);

    ProductResponseDto result = productService.stockAdjustment(1L, adjustmentDto);

    assertNotNull(result);
    assertEquals(110, product.getStock());
    verify(productRepository).save(product);
  }

  @Test
  void shouldAdjustStockNegative() {
    ProductStockAdjustmentDto adjustmentDto = new ProductStockAdjustmentDto(-50);

    when(productRepository.findById(1L)).thenReturn(Optional.of(product));
    when(productRepository.save(any(Product.class))).thenReturn(product);
    when(productMapper.toResponseDto(product)).thenReturn(productResponseDto);

    ProductResponseDto result = productService.stockAdjustment(1L, adjustmentDto);

    assertNotNull(result);
    assertEquals(50, product.getStock());
    verify(productRepository).save(product);
  }

  @Test
  void shouldThrowStockNegativeExceptionWhenAdjustmentResultsInNegativeStock() {
    ProductStockAdjustmentDto adjustmentDto = new ProductStockAdjustmentDto(-150);

    when(productRepository.findById(1L)).thenReturn(Optional.of(product));

    assertThrows(
        StockNegativeException.class, () -> productService.stockAdjustment(1L, adjustmentDto));
  }

  @Test
  void shouldSetStock() {
    ProductStockSetDto stockSetDto = new ProductStockSetDto(200);

    when(productRepository.findById(1L)).thenReturn(Optional.of(product));
    when(productRepository.save(any(Product.class))).thenReturn(product);
    when(productMapper.toResponseDto(product)).thenReturn(productResponseDto);

    ProductResponseDto result = productService.stockSet(1L, stockSetDto);

    assertNotNull(result);
    assertEquals(200, product.getStock());
    verify(productRepository).save(product);
  }

  @Test
  void shouldDeactivateProduct() {
    when(productRepository.findById(1L)).thenReturn(Optional.of(product));

    productService.deactivateProduct(1L);

    assertFalse(product.getActive());
    verify(productRepository).save(product);
  }

  @Test
  void shouldThrowProductAlreadyInactiveExceptionWhenDeactivatingInactiveProduct() {
    product.setActive(false);
    when(productRepository.findById(1L)).thenReturn(Optional.of(product));

    assertThrows(ProductAlreadyInactiveException.class, () -> productService.deactivateProduct(1L));
  }

  @Test
  void shouldActivateProduct() {
    product.setActive(false);
    when(productRepository.findById(1L)).thenReturn(Optional.of(product));

    productService.activateProduct(1L);

    assertTrue(product.getActive());
    verify(productRepository).save(product);
  }

  @Test
  void shouldThrowProductAlreadyActiveExceptionWhenActivatingActiveProduct() {
    when(productRepository.findById(1L)).thenReturn(Optional.of(product));

    assertThrows(ProductAlreadyActiveException.class, () -> productService.activateProduct(1L));
  }
}
