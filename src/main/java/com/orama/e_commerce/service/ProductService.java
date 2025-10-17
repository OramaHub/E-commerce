package com.orama.e_commerce.service;

import com.orama.e_commerce.dtos.product.*;
import com.orama.e_commerce.exceptions.product.ProductAlreadyActiveException;
import com.orama.e_commerce.exceptions.product.ProductAlreadyInactiveException;
import com.orama.e_commerce.exceptions.product.ProductNotFoundException;
import com.orama.e_commerce.exceptions.product.StockNegativeException;
import com.orama.e_commerce.mapper.ProductMapper;
import com.orama.e_commerce.models.Product;
import com.orama.e_commerce.repository.ProductRepository;
import jakarta.transaction.Transactional;
import java.util.ArrayList;
import org.springframework.stereotype.Service;

@Service
public class ProductService {

  private final ProductRepository productRepository;
  private final ProductMapper productMapper;

  public ProductService(ProductRepository productRepository, ProductMapper productMapper) {
    this.productRepository = productRepository;
    this.productMapper = productMapper;
  }

  @Transactional
  public ProductResponseDto createProduct(ProductRequestDto productRequestDto) {
    Product product = productMapper.toEntity(productRequestDto);

    if (product.getImages() == null) {
      product.setImages(new ArrayList<>());
    }

    product.setActive(true);

    productRepository.save(product);

    return productMapper.toResponseDto(product);
  }

  @Transactional
  public ProductResponseDto updateProduct(
      Long id, ProductUpdateRequestDto productUpdateRequestDto) {
    Product product = findById(id);

    productMapper.updateDto(productUpdateRequestDto, product);

    Product updatedProduct = productRepository.save(product);

    return productMapper.toResponseDto(updatedProduct);
  }

  // Muda a quantidade do produto no estoque: Adiciona (ex: 1) ou subtrai (ex: -1)
  @Transactional
  public ProductResponseDto stockAdjustment(Long id, ProductStockAdjustmentDto stockAdjustmentDto) {
    Product product = findById(id);

    int newStock = product.getStock() + stockAdjustmentDto.quantity();
    if (newStock < 0) {
      throw new StockNegativeException("Stock adjustment results in negative stock.");
    }

    product.setStock(newStock);

    productRepository.save(product);

    return productMapper.toResponseDto(product);
  }

  // Muda o valor total do estoque do produto para outro valor
  @Transactional
  public ProductResponseDto stockSet(Long id, ProductStockSetDto stockSetDto) {
    Product product = findById(id);

    int newStockValue = stockSetDto.newStockValue();

    product.setStock(newStockValue);

    productRepository.save(product);

    return productMapper.toResponseDto(product);
  }

  @Transactional
  public void deactivateProduct(Long id) {
    Product product = findById(id);

    if (!product.getActive()) {
      throw new ProductAlreadyInactiveException("Product is already inactive.");
    }

    product.setActive(false);
    productRepository.save(product);
  }

  @Transactional
  public void activateProduct(Long id) {
    Product product = findById(id);

    if (product.getActive()) {
      throw new ProductAlreadyActiveException("Product is already active.");
    }

    product.setActive(true);
    productRepository.save(product);
  }

  protected Product findById(Long id) {
    return productRepository
        .findById(id)
        .orElseThrow(() -> new ProductNotFoundException("Product not found with id: " + id));
  }
}
