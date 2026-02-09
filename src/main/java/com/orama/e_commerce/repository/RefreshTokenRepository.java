package com.orama.e_commerce.repository;

import com.orama.e_commerce.models.RefreshToken;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
  Optional<RefreshToken> findByToken(String token);

  void deleteByToken(String token);

  void deleteAllByClientId(Long clientId);
}
