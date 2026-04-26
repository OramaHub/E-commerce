package com.orama.e_commerce.service;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.orama.e_commerce.enums.OrderStatus;
import com.orama.e_commerce.enums.PaymentAttemptStatus;
import com.orama.e_commerce.models.Order;
import com.orama.e_commerce.models.PaymentAttempt;
import com.orama.e_commerce.repository.PaymentAttemptRepository;
import com.orama.e_commerce.service.gateway.GatewayOrderResult;
import com.orama.e_commerce.service.gateway.PaymentGateway;
import com.orama.e_commerce.testdata.OrderTestBuilder;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PaymentReconciliationJobTest {

  @Mock PaymentAttemptRepository paymentAttemptRepository;
  @Mock PaymentGateway paymentGateway;
  @Mock PaymentApplicationService paymentApplicationService;

  PaymentReconciliationJob job;

  @BeforeEach
  void setUp() {
    job =
        new PaymentReconciliationJob(
            paymentAttemptRepository, paymentGateway, paymentApplicationService, 10);
  }

  private PaymentAttempt staleAttempt(String providerOrderId) {
    Order order =
        OrderTestBuilder.anOrder().withId(1L).withStatus(OrderStatus.PAYMENT_PENDING).build();
    PaymentAttempt attempt = new PaymentAttempt();
    attempt.setOrder(order);
    attempt.setStatus(PaymentAttemptStatus.PENDING);
    attempt.setProviderOrderId(providerOrderId);
    return attempt;
  }

  @Test
  @DisplayName("Dois attempts stale: gateway e applyReconciliation chamados para cada um")
  void reconcile_twoStaleAttempts_callsGatewayAndApplyForEach() {
    PaymentAttempt a1 = staleAttempt("MP-ORDER-A");
    PaymentAttempt a2 = staleAttempt("MP-ORDER-B");
    GatewayOrderResult r1 = new GatewayOrderResult("MP-ORDER-A", "processing", null, null);
    GatewayOrderResult r2 = new GatewayOrderResult("MP-ORDER-B", "processed", null, null);

    when(paymentAttemptRepository.findStaleAttempts(any(), any(Instant.class)))
        .thenReturn(List.of(a1, a2));
    when(paymentGateway.getOrderStatus("MP-ORDER-A")).thenReturn(r1);
    when(paymentGateway.getOrderStatus("MP-ORDER-B")).thenReturn(r2);

    job.reconcileStaleAttempts();

    verify(paymentApplicationService).applyReconciliation(any(), eq(r1));
    verify(paymentApplicationService).applyReconciliation(any(), eq(r2));
  }

  @Test
  @DisplayName("Nenhum attempt stale: gateway nunca chamado")
  void reconcile_noStaleAttempts_neverCallsGateway() {
    when(paymentAttemptRepository.findStaleAttempts(any(), any(Instant.class)))
        .thenReturn(List.of());

    job.reconcileStaleAttempts();

    verify(paymentGateway, never()).getOrderStatus(any());
    verify(paymentApplicationService, never()).applyReconciliation(any(), any());
  }

  @Test
  @DisplayName("Gateway falha em um attempt: job continua e processa o proximo")
  void reconcile_gatewayFailsOnFirst_continuesWithSecond() {
    PaymentAttempt a1 = staleAttempt("MP-ORDER-FAIL");
    PaymentAttempt a2 = staleAttempt("MP-ORDER-OK");
    GatewayOrderResult r2 = new GatewayOrderResult("MP-ORDER-OK", "processed", null, null);

    when(paymentAttemptRepository.findStaleAttempts(any(), any(Instant.class)))
        .thenReturn(List.of(a1, a2));
    when(paymentGateway.getOrderStatus("MP-ORDER-FAIL"))
        .thenThrow(new RuntimeException("gateway timeout"));
    when(paymentGateway.getOrderStatus("MP-ORDER-OK")).thenReturn(r2);

    assertThatNoException().isThrownBy(() -> job.reconcileStaleAttempts());

    verify(paymentApplicationService).applyReconciliation(any(), eq(r2));
  }

  @Test
  @DisplayName("applyReconciliation falha em um attempt: job continua e nao propaga excecao")
  void reconcile_applyFailsOnFirst_continuesWithSecond() {
    PaymentAttempt a1 = staleAttempt("MP-ORDER-A");
    PaymentAttempt a2 = staleAttempt("MP-ORDER-B");
    GatewayOrderResult r1 = new GatewayOrderResult("MP-ORDER-A", "processed", null, null);
    GatewayOrderResult r2 = new GatewayOrderResult("MP-ORDER-B", "processed", null, null);

    when(paymentAttemptRepository.findStaleAttempts(any(), any(Instant.class)))
        .thenReturn(List.of(a1, a2));
    when(paymentGateway.getOrderStatus("MP-ORDER-A")).thenReturn(r1);
    when(paymentGateway.getOrderStatus("MP-ORDER-B")).thenReturn(r2);
    doThrow(new RuntimeException("db error"))
        .when(paymentApplicationService)
        .applyReconciliation(any(), eq(r1));

    assertThatNoException().isThrownBy(() -> job.reconcileStaleAttempts());

    verify(paymentApplicationService).applyReconciliation(any(), eq(r2));
  }
}
