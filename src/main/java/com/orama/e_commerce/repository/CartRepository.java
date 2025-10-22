package com.orama.e_commerce.repository;

import com.orama.e_commerce.models.Cart;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface CartRepository extends JpaRepository<Cart, Long> {
  List<Cart> findByClientId(Long clientId);

  @Query(
      "SELECT c FROM Cart c LEFT JOIN FETCH c.items WHERE c.client.id = :clientId AND c.order IS"
          + " NULL ORDER BY c.updatedAt DESC")
  Optional<Cart> findActiveCartByClientId(Long clientId);

  Optional<Cart> findBySessionId(String sessionId);
}
