package com.orama.e_commerce.events.payment;

import com.orama.e_commerce.enums.PaymentAttemptStatus;
import java.time.Instant;

public record PaymentFailedEvent(
    Long orderId,
    Long paymentAttemptId,
    PaymentAttemptStatus previousStatus,
    PaymentAttemptStatus newStatus,
    Instant occurredAt)
    implements PaymentEvent {}
