package com.orama.e_commerce.service;

import com.orama.e_commerce.models.RevokedToken;
import com.orama.e_commerce.repository.RevokedTokenRepository;
import com.orama.e_commerce.security.JwtService;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;

import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@EnableScheduling
@Service
public class TokenRevocationService {

  private final RevokedTokenRepository revokedTokenRepository;
  private final JwtService jwtService;

  public TokenRevocationService(
      RevokedTokenRepository revokedTokenRepository, JwtService jwtService) {
    this.revokedTokenRepository = revokedTokenRepository;
    this.jwtService = jwtService;
  }

  @Transactional
  public void revokeToken(String token) {
    String hash = hashToken(token);

    if (revokedTokenRepository.existsByTokenHash(hash)) {
      return;
    }

    RevokedToken revokedToken = new RevokedToken();
    revokedToken.setTokenHash(hash);
    revokedToken.setRevokedAt(Instant.now());
    revokedToken.setExpiresAt(jwtService.extractExpiration(token).toInstant());
    revokedTokenRepository.save(revokedToken);
  }

  public boolean isTokenRevoked(String token) {
    return revokedTokenRepository.existsByTokenHash(hashToken(token));
  }

  @Transactional
  @Scheduled(cron = "0 0 3 * * *")
  public void purgeExpiredTokens() {
    revokedTokenRepository.deleteAllByExpiresAtBefore(Instant.now());
  }

  private String hashToken(String token) {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
      return HexFormat.of().formatHex(hash);
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException("SHA-256 não disponível.", e);
    }
  }
}
