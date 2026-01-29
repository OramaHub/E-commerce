package com.orama.e_commerce.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.orama.e_commerce.dtos.product_image.ProductImageRequestDto;
import com.orama.e_commerce.dtos.product_image.ProductImageResponseDto;
import com.orama.e_commerce.exceptions.BadRequestException;
import com.orama.e_commerce.exceptions.product_image.ProductImageNotFoundException;
import com.orama.e_commerce.mapper.ProductImageMapper;
import com.orama.e_commerce.models.Product;
import com.orama.e_commerce.models.ProductImage;
import com.orama.e_commerce.repository.ProductImageRepository;
import java.math.BigDecimal;
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
class ProductImageServiceTest {

  @Mock private ProductImageRepository productImageRepository;
  @Mock private ProductService productService;
  @Mock private ProductImageMapper productImageMapper;

  @InjectMocks private ProductImageService productImageService;

  private Product product;
  private ProductImage productImage;
  private ProductImageResponseDto productImageResponseDto;
  private Pageable pageable;

  @BeforeEach
  void setUp() {
    product = new Product();
    product.setId(1L);
    product.setName("Produto Teste");
    product.setPrice(new BigDecimal("99.90"));
    product.setActive(true);

    productImage = new ProductImage();
    productImage.setId(1L);
    productImage.setUrl("https://example.com/image1.jpg");
    productImage.setProduct(product);

    productImageResponseDto = new ProductImageResponseDto(1L, "https://example.com/image1.jpg");

    pageable = PageRequest.of(0, 10);
  }

  @Test
  void shouldGetImagesByProductId() {
    Page<ProductImage> imagePage = new PageImpl<>(List.of(productImage));
    when(productService.findById(1L)).thenReturn(product);
    when(productImageRepository.findAllByProductId(1L, pageable)).thenReturn(imagePage);
    when(productImageMapper.toResponseDto(productImage)).thenReturn(productImageResponseDto);

    Page<ProductImageResponseDto> result = productImageService.getImagesByProductId(1L, pageable);

    assertNotNull(result);
    assertEquals(1, result.getTotalElements());
    verify(productService).findById(1L);
    verify(productImageRepository).findAllByProductId(1L, pageable);
  }

  @Test
  void shouldAddImageToProduct() {
    ProductImageRequestDto requestDto =
        new ProductImageRequestDto("https://example.com/image2.jpg");

    when(productService.findById(1L)).thenReturn(product);
    when(productImageMapper.toEntity(requestDto)).thenReturn(productImage);
    when(productImageRepository.save(any(ProductImage.class))).thenReturn(productImage);
    when(productImageMapper.toResponseDto(productImage)).thenReturn(productImageResponseDto);

    ProductImageResponseDto result = productImageService.addImageToProduct(1L, requestDto);

    assertNotNull(result);
    assertEquals(1L, result.id());
    verify(productService).findById(1L);
    verify(productImageRepository).save(any(ProductImage.class));
  }

  @Test
  void shouldAddMultipleImagesToProduct() {
    ProductImageRequestDto requestDto1 =
        new ProductImageRequestDto("https://example.com/image1.jpg");
    ProductImageRequestDto requestDto2 =
        new ProductImageRequestDto("https://example.com/image2.jpg");

    ProductImage image2 = new ProductImage();
    image2.setId(2L);
    image2.setUrl("https://example.com/image2.jpg");
    image2.setProduct(product);

    ProductImageResponseDto responseDto2 =
        new ProductImageResponseDto(2L, "https://example.com/image2.jpg");

    when(productService.findById(1L)).thenReturn(product);
    when(productImageMapper.toEntity(requestDto1)).thenReturn(productImage);
    when(productImageMapper.toEntity(requestDto2)).thenReturn(image2);
    when(productImageRepository.save(productImage)).thenReturn(productImage);
    when(productImageRepository.save(image2)).thenReturn(image2);
    when(productImageMapper.toResponseDto(productImage)).thenReturn(productImageResponseDto);
    when(productImageMapper.toResponseDto(image2)).thenReturn(responseDto2);

    List<ProductImageResponseDto> result =
        productImageService.addImagesToProduct(1L, List.of(requestDto1, requestDto2));

    assertNotNull(result);
    assertEquals(2, result.size());
    verify(productService).findById(1L);
    verify(productImageRepository, times(2)).save(any(ProductImage.class));
  }

  @Test
  void shouldDeleteProductImage() {
    when(productImageRepository.findById(1L)).thenReturn(Optional.of(productImage));

    productImageService.deleteProductImage(1L, 1L);

    verify(productImageRepository).delete(productImage);
  }

  @Test
  void shouldThrowBadRequestExceptionWhenImageDoesNotBelongToProduct() {
    Product anotherProduct = new Product();
    anotherProduct.setId(2L);

    ProductImage imageFromAnotherProduct = new ProductImage();
    imageFromAnotherProduct.setId(1L);
    imageFromAnotherProduct.setProduct(anotherProduct);

    when(productImageRepository.findById(1L)).thenReturn(Optional.of(imageFromAnotherProduct));

    assertThrows(BadRequestException.class, () -> productImageService.deleteProductImage(1L, 1L));
  }

  @Test
  void shouldThrowProductImageNotFoundExceptionWhenImageNotFound() {
    when(productImageRepository.findById(99L)).thenReturn(Optional.empty());

    assertThrows(
        ProductImageNotFoundException.class, () -> productImageService.deleteProductImage(1L, 99L));
  }
}
