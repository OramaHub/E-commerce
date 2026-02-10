package com.orama.e_commerce.repository;

import com.orama.e_commerce.models.RevokedToken;
import java.time.Instant;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RevokedTokenRepository extends JpaRepository<RevokedToken, Long> {
  boolean existsByTokenHash(String tokenHash);

  void deleteAllByExpiresAtBefore(Instant now);
}
