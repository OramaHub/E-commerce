package com.orama.e_commerce.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

  private static final int MIN_SECRET_LENGTH = 32;

  @Value("${jwt.secret}")
  private String secret;

  @Value("${jwt.access-expiration}")
  private Long accessExpiration;

  @Value("${jwt.refresh-expiration}")
  private Long refreshExpiration;

  @PostConstruct
  public void validateSecret() {
    if (secret == null || secret.isBlank()) {
      throw new IllegalStateException("JWT_SECRET não está configurado.");
    }
    if (secret.getBytes().length < MIN_SECRET_LENGTH) {
      throw new IllegalStateException(
          "JWT_SECRET deve ter no mínimo " + MIN_SECRET_LENGTH + " bytes (256 bits).");
    }
  }

  public String extractUsername(String token) {
    return extractClaim(token, Claims::getSubject);
  }

  public Date extractExpiration(String token) {
    return extractClaim(token, Claims::getExpiration);
  }

  public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
    final Claims claims = extractAllClaims(token);
    return claimsResolver.apply(claims);
  }

  private Claims extractAllClaims(String token) {
    return Jwts.parser().verifyWith(getSigningKey()).build().parseSignedClaims(token).getPayload();
  }

  private Boolean isTokenExpired(String token) {
    return extractExpiration(token).before(new Date());
  }

  public String generateToken(UserDetails userDetails, Long clientId) {
    Map<String, Object> claims = new HashMap<>();
    claims.put("id", clientId);
    return createToken(claims, userDetails.getUsername());
  }

  public Long extractUserId(String token) {
    return extractClaim(token, claims -> claims.get("id", Long.class));
  }

  private String createToken(Map<String, Object> claims, String subject) {
    Date now = new Date();
    Date expiryDate = new Date(now.getTime() + accessExpiration);

    return Jwts.builder()
        .claims(claims)
        .subject(subject)
        .issuedAt(now)
        .expiration(expiryDate)
        .signWith(getSigningKey())
        .compact();
  }

  private SecretKey getSigningKey() {
    byte[] keyBytes = secret.getBytes();
    return Keys.hmacShaKeyFor(keyBytes);
  }

  public Boolean validateToken(String token, UserDetails userDetails) {
    final String username = extractUsername(token);
    return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
  }

  public Long getAccessExpirationTime() {
    return accessExpiration;
  }

  public Long getRefreshExpirationTime() {
    return refreshExpiration;
  }
}
