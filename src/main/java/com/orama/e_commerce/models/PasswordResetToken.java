package com.orama.e_commerce.models;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.Objects;

@Entity
@Table(name = "tb_password_reset_tokens")
public class PasswordResetToken {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true)
  private String token;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "client_id", nullable = false)
  private Client client;

  @Column(nullable = false)
  private Instant expiresAt;

  @Column(nullable = false)
  private Instant createdAt;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getToken() {
    return token;
  }

  public void setToken(String token) {
    this.token = token;
  }

  public Client getClient() {
    return client;
  }

  public void setClient(Client client) {
    this.client = client;
  }

  public Instant getExpiresAt() {
    return expiresAt;
  }

  public void setExpiresAt(Instant expiresAt) {
    this.expiresAt = expiresAt;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(Instant createdAt) {
    this.createdAt = createdAt;
  }

  @Override
  public boolean equals(Object o) {
    if (o == null || getClass() != o.getClass()) return false;
    PasswordResetToken that = (PasswordResetToken) o;
    return Objects.equals(id, that.id);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(id);
  }
}
