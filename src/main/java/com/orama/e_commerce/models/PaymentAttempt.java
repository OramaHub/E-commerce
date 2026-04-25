package com.orama.e_commerce.models;

import com.orama.e_commerce.enums.PaymentAttemptStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "tb_payment_attempt")
public class PaymentAttempt {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "order_id", nullable = false)
  private Order order;

  @Column(name = "provider", length = 50, nullable = false)
  private String provider;

  @Column(name = "provider_order_id", length = 100)
  private String providerOrderId;

  @Column(name = "provider_payment_id", length = 100)
  private String providerPaymentId;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", length = 30, nullable = false)
  private PaymentAttemptStatus status;

  @Column(name = "status_detail", length = 100)
  private String statusDetail;

  @Column(name = "method", length = 30)
  private String method;

  @Column(name = "amount", precision = 15, scale = 2)
  private BigDecimal amount;

  @Column(name = "currency", length = 3)
  private String currency = "BRL";

  @Column(name = "idempotency_key", length = 100, unique = true)
  private String idempotencyKey;

  @Column(name = "attempt_number", nullable = false)
  private int attemptNumber;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at")
  private Instant updatedAt;

  public PaymentAttempt() {}

  public Long getId() {
    return id;
  }

  public Order getOrder() {
    return order;
  }

  public void setOrder(Order order) {
    this.order = order;
  }

  public String getProvider() {
    return provider;
  }

  public void setProvider(String provider) {
    this.provider = provider;
  }

  public String getProviderOrderId() {
    return providerOrderId;
  }

  public void setProviderOrderId(String providerOrderId) {
    this.providerOrderId = providerOrderId;
  }

  public String getProviderPaymentId() {
    return providerPaymentId;
  }

  public void setProviderPaymentId(String providerPaymentId) {
    this.providerPaymentId = providerPaymentId;
  }

  public PaymentAttemptStatus getStatus() {
    return status;
  }

  public void setStatus(PaymentAttemptStatus status) {
    this.status = status;
  }

  public String getStatusDetail() {
    return statusDetail;
  }

  public void setStatusDetail(String statusDetail) {
    this.statusDetail = statusDetail;
  }

  public String getMethod() {
    return method;
  }

  public void setMethod(String method) {
    this.method = method;
  }

  public BigDecimal getAmount() {
    return amount;
  }

  public void setAmount(BigDecimal amount) {
    this.amount = amount;
  }

  public String getCurrency() {
    return currency;
  }

  public void setCurrency(String currency) {
    this.currency = currency;
  }

  public String getIdempotencyKey() {
    return idempotencyKey;
  }

  public void setIdempotencyKey(String idempotencyKey) {
    this.idempotencyKey = idempotencyKey;
  }

  public int getAttemptNumber() {
    return attemptNumber;
  }

  public void setAttemptNumber(int attemptNumber) {
    this.attemptNumber = attemptNumber;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public Instant getUpdatedAt() {
    return updatedAt;
  }

  @Override
  public boolean equals(Object o) {
    return this == o || (o instanceof PaymentAttempt pa && Objects.equals(id, pa.id));
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(id);
  }

  @Override
  public String toString() {
    return "PaymentAttempt{id=" + id + ", provider='" + provider + "', status=" + status + "}";
  }
}
