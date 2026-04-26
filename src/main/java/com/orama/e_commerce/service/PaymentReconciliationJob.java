package com.orama.e_commerce.service;

import com.orama.e_commerce.enums.PaymentAttemptStatus;
import com.orama.e_commerce.models.PaymentAttempt;
import com.orama.e_commerce.repository.PaymentAttemptRepository;
import com.orama.e_commerce.service.gateway.GatewayOrderResult;
import com.orama.e_commerce.service.gateway.PaymentGateway;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class PaymentReconciliationJob {

  private static final Logger log = LoggerFactory.getLogger(PaymentReconciliationJob.class);
  private static final List<PaymentAttemptStatus> RECONCILABLE_STATUSES =
      List.of(
          PaymentAttemptStatus.PENDING,
          PaymentAttemptStatus.AUTHORIZED,
          PaymentAttemptStatus.AWAITING_CHALLENGE);

  private final PaymentAttemptRepository paymentAttemptRepository;
  private final PaymentGateway paymentGateway;
  private final PaymentApplicationService paymentApplicationService;
  private final int staleMinutes;

  public PaymentReconciliationJob(
      PaymentAttemptRepository paymentAttemptRepository,
      PaymentGateway paymentGateway,
      PaymentApplicationService paymentApplicationService,
      @Value("${payment.reconciliation.stale-minutes:10}") int staleMinutes) {
    this.paymentAttemptRepository = paymentAttemptRepository;
    this.paymentGateway = paymentGateway;
    this.paymentApplicationService = paymentApplicationService;
    this.staleMinutes = staleMinutes;
  }

  @Scheduled(cron = "${payment.reconciliation.cron:0 */5 * * * *}")
  public void reconcileStaleAttempts() {
    Instant cutoff = Instant.now().minus(staleMinutes, ChronoUnit.MINUTES);
    List<PaymentAttempt> stale =
        paymentAttemptRepository.findStaleAttempts(RECONCILABLE_STATUSES, cutoff);
    log.info("Reconciliando {} tentativas de pagamento stale (cutoff: {})", stale.size(), cutoff);
    for (PaymentAttempt attempt : stale) {
      try {
        GatewayOrderResult result = paymentGateway.getOrderStatus(attempt.getProviderOrderId());
        paymentApplicationService.applyReconciliation(attempt.getId(), result);
      } catch (Exception e) {
        log.warn(
            "Falha ao reconciliar attempt id={} providerOrderId={}: {}",
            attempt.getId(),
            attempt.getProviderOrderId(),
            e.getMessage());
      }
    }
  }
}
