package com.orama.e_commerce.repository;

import com.orama.e_commerce.enums.PaymentAttemptStatus;
import com.orama.e_commerce.models.PaymentAttempt;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PaymentAttemptRepository extends JpaRepository<PaymentAttempt, Long> {

  Optional<PaymentAttempt> findByProviderOrderId(String providerOrderId);

  Optional<PaymentAttempt> findByIdempotencyKey(String idempotencyKey);

  Optional<PaymentAttempt> findTopByOrderIdOrderByAttemptNumberDesc(Long orderId);

  @Query(
      "SELECT pa FROM PaymentAttempt pa WHERE pa.status IN :statuses"
          + " AND pa.providerOrderId IS NOT NULL AND pa.updatedAt < :cutoff")
  List<PaymentAttempt> findStaleAttempts(
      @Param("statuses") List<PaymentAttemptStatus> statuses, @Param("cutoff") Instant cutoff);
}
