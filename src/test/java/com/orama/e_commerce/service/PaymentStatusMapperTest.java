package com.orama.e_commerce.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.orama.e_commerce.enums.PaymentAttemptStatus;
import org.junit.jupiter.api.Test;

class PaymentStatusMapperTest {

  private final PaymentStatusMapper mapper = new PaymentStatusMapper();

  @Test
  void actionRequiredPendingChallengeMapsToAwaitingChallenge() {
    assertThat(mapper.toAttemptStatus("action_required", "pending_challenge"))
        .isEqualTo(PaymentAttemptStatus.AWAITING_CHALLENGE);
  }

  @Test
  void actionRequiredWithoutDetailMapsToPending() {
    assertThat(mapper.toAttemptStatus("action_required", null))
        .isEqualTo(PaymentAttemptStatus.PENDING);
  }
}
