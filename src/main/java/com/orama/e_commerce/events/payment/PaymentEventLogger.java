package com.orama.e_commerce.events.payment;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class PaymentEventLogger {

  private static final Logger log = LoggerFactory.getLogger(PaymentEventLogger.class);

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void onApproved(PaymentApprovedEvent event) {
    try {
      log.info(
          "Pagamento aprovado: orderId={} attemptId={} from={} at={}",
          event.orderId(),
          event.paymentAttemptId(),
          event.previousStatus(),
          event.occurredAt());
    } catch (Exception e) {
      log.error("Falha no listener PaymentEventLogger.onApproved", e);
    }
  }

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void onFailed(PaymentFailedEvent event) {
    try {
      log.info(
          "Pagamento falhou: orderId={} attemptId={} {}->{} at={}",
          event.orderId(),
          event.paymentAttemptId(),
          event.previousStatus(),
          event.newStatus(),
          event.occurredAt());
    } catch (Exception e) {
      log.error("Falha no listener PaymentEventLogger.onFailed", e);
    }
  }
}
