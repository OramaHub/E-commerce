package com.orama.e_commerce.repository;

import com.orama.e_commerce.models.Order;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {
  Optional<Order> findByOrderNumber(String orderNumber);

  List<Order> findByClientId(Long clientId);

  boolean existsByOrderNumber(String orderNumber);
}
