package com.orama.e_commerce.service;

import com.orama.e_commerce.enums.OrderStatus;
import com.orama.e_commerce.enums.PaymentAttemptStatus;
import com.orama.e_commerce.events.payment.PaymentFailedEvent;
import com.orama.e_commerce.models.Order;
import com.orama.e_commerce.models.PaymentAttempt;
import com.orama.e_commerce.repository.OrderRepository;
import com.orama.e_commerce.repository.PaymentAttemptRepository;
import java.time.Instant;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PaymentAttemptService {

  private final PaymentAttemptRepository paymentAttemptRepository;
  private final OrderRepository orderRepository;
  private final ApplicationEventPublisher eventPublisher;

  public PaymentAttemptService(
      PaymentAttemptRepository paymentAttemptRepository,
      OrderRepository orderRepository,
      ApplicationEventPublisher eventPublisher) {
    this.paymentAttemptRepository = paymentAttemptRepository;
    this.orderRepository = orderRepository;
    this.eventPublisher = eventPublisher;
  }

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void openAttempt(PaymentAttempt attempt, Order order) {
    paymentAttemptRepository.save(attempt);
    order.setStatus(OrderStatus.PAYMENT_PENDING);
    orderRepository.save(order);
  }

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void markFailed(PaymentAttempt attempt, Order order, String errorMessage) {
    PaymentAttemptStatus previousStatus = attempt.getStatus();
    attempt.setStatus(PaymentAttemptStatus.FAILED);
    attempt.setStatusDetail(errorMessage);
    paymentAttemptRepository.save(attempt);
    order.setStatus(OrderStatus.PENDING);
    orderRepository.save(order);
    eventPublisher.publishEvent(
        new PaymentFailedEvent(
            order.getId(),
            attempt.getId(),
            previousStatus,
            PaymentAttemptStatus.FAILED,
            Instant.now()));
  }
}
