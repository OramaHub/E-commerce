package com.orama.e_commerce.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.orama.e_commerce.enums.UserRole;
import com.orama.e_commerce.exceptions.auth.InvalidRefreshTokenException;
import com.orama.e_commerce.models.Client;
import com.orama.e_commerce.models.RefreshToken;
import com.orama.e_commerce.repository.RefreshTokenRepository;
import com.orama.e_commerce.security.JwtService;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

  @Mock private RefreshTokenRepository refreshTokenRepository;
  @Mock private JwtService jwtService;

  @InjectMocks private RefreshTokenService refreshTokenService;

  private Client client;
  private RefreshToken refreshToken;

  @BeforeEach
  void setUp() {
    client = new Client();
    client.setId(1L);
    client.setName("João Silva");
    client.setEmail("joao@email.com");
    client.setRole(UserRole.USER);

    refreshToken = new RefreshToken();
    refreshToken.setId(1L);
    refreshToken.setToken("refresh-token-uuid");
    refreshToken.setClient(client);
    refreshToken.setExpiresAt(Instant.now().plusMillis(604800000));
    refreshToken.setCreatedAt(Instant.now());
  }

  @Test
  void shouldCreateRefreshToken() {
    when(jwtService.getRefreshExpirationTime()).thenReturn(604800000L);
    when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(i -> i.getArgument(0));

    RefreshToken result = refreshTokenService.createRefreshToken(client);

    assertNotNull(result);
    assertNotNull(result.getToken());
    assertEquals(client, result.getClient());
    assertNotNull(result.getExpiresAt());
    assertNotNull(result.getCreatedAt());
    verify(refreshTokenRepository).save(any(RefreshToken.class));
  }

  @Test
  void shouldValidateRefreshTokenSuccessfully() {
    when(refreshTokenRepository.findByToken("refresh-token-uuid"))
        .thenReturn(Optional.of(refreshToken));

    RefreshToken result = refreshTokenService.validateRefreshToken("refresh-token-uuid");

    assertNotNull(result);
    assertEquals("refresh-token-uuid", result.getToken());
  }

  @Test
  void shouldThrowWhenRefreshTokenNotFound() {
    when(refreshTokenRepository.findByToken("invalid-token")).thenReturn(Optional.empty());

    InvalidRefreshTokenException exception =
        assertThrows(
            InvalidRefreshTokenException.class,
            () -> refreshTokenService.validateRefreshToken("invalid-token"));

    assertEquals("Refresh token not found.", exception.getMessage());
  }

  @Test
  void shouldThrowWhenRefreshTokenExpired() {
    refreshToken.setExpiresAt(Instant.now().minusMillis(1000));
    when(refreshTokenRepository.findByToken("refresh-token-uuid"))
        .thenReturn(Optional.of(refreshToken));

    InvalidRefreshTokenException exception =
        assertThrows(
            InvalidRefreshTokenException.class,
            () -> refreshTokenService.validateRefreshToken("refresh-token-uuid"));

    assertEquals("Refresh token expired.", exception.getMessage());
    verify(refreshTokenRepository).delete(refreshToken);
  }

  @Test
  void shouldRotateRefreshToken() {
    when(jwtService.getRefreshExpirationTime()).thenReturn(604800000L);
    when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(i -> i.getArgument(0));

    RefreshToken result = refreshTokenService.rotateRefreshToken(refreshToken);

    assertNotNull(result);
    assertNotEquals("refresh-token-uuid", result.getToken());
    assertEquals(client, result.getClient());
    verify(refreshTokenRepository).delete(refreshToken);
    verify(refreshTokenRepository).save(any(RefreshToken.class));
  }

  @Test
  void shouldDeleteByToken() {
    refreshTokenService.deleteByToken("refresh-token-uuid");

    verify(refreshTokenRepository).deleteByToken("refresh-token-uuid");
  }

  @Test
  void shouldDeleteAllByClientId() {
    refreshTokenService.deleteAllByClientId(1L);

    verify(refreshTokenRepository).deleteAllByClientId(1L);
  }
}
