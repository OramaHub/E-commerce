package com.orama.e_commerce.repository;

import com.orama.e_commerce.models.PasswordResetToken;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

  Optional<PasswordResetToken> findByToken(String token);

  void deleteAllByClientId(Long clientId);

  void deleteByToken(String token);
}
