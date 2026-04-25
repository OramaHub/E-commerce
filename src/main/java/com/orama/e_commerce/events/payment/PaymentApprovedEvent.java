package com.orama.e_commerce.events.payment;

import com.orama.e_commerce.enums.PaymentAttemptStatus;
import java.time.Instant;

public record PaymentApprovedEvent(
    Long orderId, Long paymentAttemptId, PaymentAttemptStatus previousStatus, Instant occurredAt)
    implements PaymentEvent {}
