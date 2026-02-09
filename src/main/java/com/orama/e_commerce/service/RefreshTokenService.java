package com.orama.e_commerce.service;

import com.orama.e_commerce.exceptions.auth.InvalidRefreshTokenException;
import com.orama.e_commerce.models.Client;
import com.orama.e_commerce.models.RefreshToken;
import com.orama.e_commerce.repository.RefreshTokenRepository;
import com.orama.e_commerce.security.JwtService;
import java.time.Instant;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RefreshTokenService {

  private final RefreshTokenRepository refreshTokenRepository;
  private final JwtService jwtService;

  public RefreshTokenService(RefreshTokenRepository refreshTokenRepository, JwtService jwtService) {
    this.refreshTokenRepository = refreshTokenRepository;
    this.jwtService = jwtService;
  }

  public RefreshToken createRefreshToken(Client client) {
    RefreshToken refreshToken = new RefreshToken();
    refreshToken.setClient(client);
    refreshToken.setToken(UUID.randomUUID().toString());
    refreshToken.setCreatedAt(Instant.now());
    refreshToken.setExpiresAt(Instant.now().plusMillis(jwtService.getRefreshExpirationTime()));
    return refreshTokenRepository.save(refreshToken);
  }

  public RefreshToken validateRefreshToken(String token) {
    RefreshToken refreshToken =
        refreshTokenRepository
            .findByToken(token)
            .orElseThrow(() -> new InvalidRefreshTokenException("Refresh token not found."));

    if (refreshToken.getExpiresAt().isBefore(Instant.now())) {
      refreshTokenRepository.delete(refreshToken);
      throw new InvalidRefreshTokenException("Refresh token expired.");
    }

    return refreshToken;
  }

  @Transactional
  public RefreshToken rotateRefreshToken(RefreshToken oldToken) {
    Client client = oldToken.getClient();
    refreshTokenRepository.delete(oldToken);
    return createRefreshToken(client);
  }

  @Transactional
  public void deleteByToken(String token) {
    refreshTokenRepository.deleteByToken(token);
  }

  @Transactional
  public void deleteAllByClientId(Long clientId) {
    refreshTokenRepository.deleteAllByClientId(clientId);
  }
}
