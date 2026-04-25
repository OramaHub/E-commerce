package com.orama.e_commerce.repository;

import com.orama.e_commerce.models.PaymentAttempt;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentAttemptRepository extends JpaRepository<PaymentAttempt, Long> {

  Optional<PaymentAttempt> findByProviderOrderId(String providerOrderId);

  Optional<PaymentAttempt> findByIdempotencyKey(String idempotencyKey);

  Optional<PaymentAttempt> findTopByOrderIdOrderByAttemptNumberDesc(Long orderId);
}
