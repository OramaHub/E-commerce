package com.orama.e_commerce.repository;

import com.orama.e_commerce.models.ProductImage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductImageRepository extends JpaRepository<ProductImage, Long> {
  Page<ProductImage> findAllByProductId(Long productId, Pageable pageable);
}
