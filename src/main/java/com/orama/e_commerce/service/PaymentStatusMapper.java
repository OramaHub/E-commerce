package com.orama.e_commerce.service;

import com.orama.e_commerce.enums.OrderStatus;
import com.orama.e_commerce.enums.PaymentAttemptStatus;
import org.springframework.stereotype.Component;

@Component
public class PaymentStatusMapper {

  public PaymentAttemptStatus toAttemptStatus(String mpStatus, String mpStatusDetail) {
    if (mpStatus == null) return PaymentAttemptStatus.PENDING;

    return switch (mpStatus) {
      case "created" -> PaymentAttemptStatus.CREATED;
      case "processing", "in_review" -> PaymentAttemptStatus.PENDING;
      case "action_required" -> switch (mpStatusDetail != null ? mpStatusDetail : "") {
        case "waiting_capture" -> PaymentAttemptStatus.AUTHORIZED;
        case "pending_challenge" -> PaymentAttemptStatus.AWAITING_CHALLENGE;
        default -> PaymentAttemptStatus.PENDING;
      };
      case "processed" -> PaymentAttemptStatus.APPROVED;
      case "failed" -> PaymentAttemptStatus.FAILED;
      case "canceled" -> PaymentAttemptStatus.CANCELLED;
      case "expired" -> PaymentAttemptStatus.EXPIRED;
      case "refunded" -> PaymentAttemptStatus.REFUNDED;
      case "charged_back" -> PaymentAttemptStatus.CHARGED_BACK;
      default -> PaymentAttemptStatus.PENDING;
    };
  }

  public OrderStatus toOrderStatus(PaymentAttemptStatus attemptStatus) {
    return switch (attemptStatus) {
      case APPROVED -> OrderStatus.PAYMENT_CONFIRMED;
      case FAILED -> OrderStatus.PENDING;
      case CANCELLED, EXPIRED -> OrderStatus.CANCELLED;
      case REFUNDED -> OrderStatus.REFUNDED;
      default -> null;
    };
  }
}
