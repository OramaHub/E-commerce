package com.orama.e_commerce.repository;

import com.orama.e_commerce.models.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {}
