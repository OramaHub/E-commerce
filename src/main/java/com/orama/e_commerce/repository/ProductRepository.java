package com.orama.e_commerce.repository;

import com.orama.e_commerce.models.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {
  Page<Product> findByActiveTrue(Pageable pageable);

  Page<Product> findByNameContainingIgnoreCaseAndActiveTrue(String name, Pageable pageable);
}
