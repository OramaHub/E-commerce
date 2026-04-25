package com.orama.e_commerce.events.payment;

import java.time.Instant;

public sealed interface PaymentEvent permits PaymentApprovedEvent, PaymentFailedEvent {
  Long orderId();

  Long paymentAttemptId();

  Instant occurredAt();
}
