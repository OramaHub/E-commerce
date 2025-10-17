package com.orama.e_commerce.service;

import com.orama.e_commerce.dtos.product_image.ProductImageRequestDto;
import com.orama.e_commerce.dtos.product_image.ProductImageResponseDto;
import com.orama.e_commerce.exceptions.BadRequestException;
import com.orama.e_commerce.exceptions.product_image.ProductImageNotFoundException;
import com.orama.e_commerce.mapper.ProductImageMapper;
import com.orama.e_commerce.models.Product;
import com.orama.e_commerce.models.ProductImage;
import com.orama.e_commerce.repository.ProductImageRepository;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class ProductImageService {

  private final ProductImageRepository productImageRepository;
  private final ProductService productService;
  private final ProductImageMapper productImageMapper;

  public ProductImageService(
      ProductImageRepository productImageRepository,
      ProductService productService,
      ProductImageMapper productImageMapper) {
    this.productImageRepository = productImageRepository;
    this.productService = productService;
    this.productImageMapper = productImageMapper;
  }

  public Page<ProductImageResponseDto> getImagesByProductId(Long productId, Pageable pageable) {
    productService.findById(productId);

    return productImageRepository
        .findAllByProductId(productId, pageable)
        .map(productImageMapper::toResponseDto);
  }

  // Adiciona apenas uma imagem por vez
  @Transactional
  public ProductImageResponseDto addImageToProduct(
      Long productId, ProductImageRequestDto productImageRequestDto) {
    Product product = productService.findById(productId);

    ProductImage productImage = productImageMapper.toEntity(productImageRequestDto);
    productImage.setProduct(product);

    productImageRepository.save(productImage);

    return productImageMapper.toResponseDto(productImage);
  }

  // Adiciona várias imagens de vez
  @Transactional
  public List<ProductImageResponseDto> addImagesToProduct(
      Long productId, List<ProductImageRequestDto> imageRequestDtos) {

    Product product = productService.findById(productId);

    return imageRequestDtos.stream()
        .map(
            requestDto -> {
              ProductImage productImage = productImageMapper.toEntity(requestDto);

              productImage.setProduct(product);

              ProductImage savedImage = productImageRepository.save(productImage);

              return productImageMapper.toResponseDto(savedImage);
            })
        .collect(Collectors.toList());
  }

  @Transactional
  public void deleteProductImage(Long productId, Long imageId) {
    ProductImage image = findById(imageId);

    if (!image.getProduct().getId().equals(productId)) {
      throw new BadRequestException("Image ID does not belong to the specified Product ID.");
    }

    // deletar o arquivo fisico do armazenamento externo: storageService.delete(image.getUrl());

    productImageRepository.delete(image);
  }

  private ProductImage findById(Long id) {
    return productImageRepository
        .findById(id)
        .orElseThrow(() -> new ProductImageNotFoundException("Image not found with id: " + id));
  }
}
