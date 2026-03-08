package com.orama.e_commerce.repository;

import com.orama.e_commerce.models.CustomOrder;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomOrderRepository extends JpaRepository<CustomOrder, Long> {

  List<CustomOrder> findByClientId(Long clientId);

  Optional<CustomOrder> findByOrderNumber(String orderNumber);

  boolean existsByOrderNumber(String orderNumber);
}
