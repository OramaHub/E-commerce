package com.orama.e_commerce.service;

import com.orama.e_commerce.exceptions.auth.InvalidPasswordResetTokenException;
import com.orama.e_commerce.models.Client;
import com.orama.e_commerce.models.PasswordResetToken;
import com.orama.e_commerce.repository.ClientRepository;
import com.orama.e_commerce.repository.PasswordResetTokenRepository;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PasswordResetService {

  private static final Logger logger = LoggerFactory.getLogger(PasswordResetService.class);
  private static final int TOKEN_EXPIRATION_MINUTES = 30;

  private final PasswordResetTokenRepository passwordResetTokenRepository;
  private final ClientRepository clientRepository;
  private final PasswordEncoder passwordEncoder;
  private final EmailService emailService;

  @Value("${app.frontend-url}")
  private String frontendUrl;

  public PasswordResetService(
      PasswordResetTokenRepository passwordResetTokenRepository,
      ClientRepository clientRepository,
      PasswordEncoder passwordEncoder,
      EmailService emailService) {
    this.passwordResetTokenRepository = passwordResetTokenRepository;
    this.clientRepository = clientRepository;
    this.passwordEncoder = passwordEncoder;
    this.emailService = emailService;
  }

  @Transactional
  public void requestPasswordReset(String email) {
    Optional<Client> clientOptional = clientRepository.findByEmail(email);

    if (clientOptional.isEmpty()) {
      logger.info("Password reset requested for non-existent email: {}", email);
      return;
    }

    Client client = clientOptional.get();

    passwordResetTokenRepository.deleteAllByClientId(client.getId());

    PasswordResetToken resetToken = new PasswordResetToken();
    resetToken.setToken(UUID.randomUUID().toString());
    resetToken.setClient(client);
    resetToken.setExpiresAt(Instant.now().plus(TOKEN_EXPIRATION_MINUTES, ChronoUnit.MINUTES));
    resetToken.setCreatedAt(Instant.now());

    passwordResetTokenRepository.save(resetToken);

    String resetLink = frontendUrl + "/redefinir-senha?token=" + resetToken.getToken();
    emailService.sendPasswordResetEmail(client.getEmail(), client.getName(), resetLink);

    logger.info("Password reset token generated for client: {}", client.getId());
  }

  @Transactional
  public void resetPassword(String token, String newPassword) {
    PasswordResetToken resetToken =
        passwordResetTokenRepository
            .findByToken(token)
            .orElseThrow(
                () ->
                    new InvalidPasswordResetTokenException(
                        "Invalid or expired password reset token."));

    if (resetToken.getExpiresAt().isBefore(Instant.now())) {
      passwordResetTokenRepository.deleteByToken(token);
      throw new InvalidPasswordResetTokenException("Invalid or expired password reset token.");
    }

    Client client = resetToken.getClient();
    client.setPasswordHash(passwordEncoder.encode(newPassword));
    clientRepository.save(client);

    passwordResetTokenRepository.deleteByToken(token);

    logger.info("Password successfully reset for client: {}", client.getId());
  }
}
