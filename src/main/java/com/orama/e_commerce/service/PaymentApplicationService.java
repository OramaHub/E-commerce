package com.orama.e_commerce.service;

import com.orama.e_commerce.dtos.payment.InitiatePaymentRequestDto;
import com.orama.e_commerce.dtos.payment.InitiatePaymentResponseDto;
import com.orama.e_commerce.dtos.payment.MercadoPagoWebhookDto;
import com.orama.e_commerce.enums.OrderStatus;
import com.orama.e_commerce.enums.PaymentAttemptStatus;
import com.orama.e_commerce.events.payment.PaymentApprovedEvent;
import com.orama.e_commerce.events.payment.PaymentFailedEvent;
import com.orama.e_commerce.exceptions.order.OrderNotFoundException;
import com.orama.e_commerce.exceptions.payment.OrderOwnershipException;
import com.orama.e_commerce.exceptions.payment.PaymentAlreadyInProgressException;
import com.orama.e_commerce.exceptions.payment.PaymentGatewayException;
import com.orama.e_commerce.exceptions.payment.WebhookProcessingException;
import com.orama.e_commerce.models.Address;
import com.orama.e_commerce.models.Client;
import com.orama.e_commerce.models.Order;
import com.orama.e_commerce.models.PaymentAttempt;
import com.orama.e_commerce.repository.OrderRepository;
import com.orama.e_commerce.repository.PaymentAttemptRepository;
import com.orama.e_commerce.service.gateway.CreatePaymentCommand;
import com.orama.e_commerce.service.gateway.GatewayOrderResult;
import com.orama.e_commerce.service.gateway.GatewayPaymentResult;
import com.orama.e_commerce.service.gateway.PaymentGateway;
import jakarta.transaction.Transactional;
import java.time.Instant;
import java.util.List;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Service
public class PaymentApplicationService {

  private static final String PROVIDER = "MERCADOPAGO";
  private static final String CURRENCY = "BRL";
  private static final String DOCUMENT_TYPE_CPF = "CPF";

  private final OrderRepository orderRepository;
  private final PaymentAttemptRepository paymentAttemptRepository;
  private final PaymentGateway paymentGateway;
  private final WebhookVerifier webhookVerifier;
  private final PaymentStatusMapper paymentStatusMapper;
  private final ApplicationEventPublisher eventPublisher;

  public PaymentApplicationService(
      OrderRepository orderRepository,
      PaymentAttemptRepository paymentAttemptRepository,
      PaymentGateway paymentGateway,
      WebhookVerifier webhookVerifier,
      PaymentStatusMapper paymentStatusMapper,
      ApplicationEventPublisher eventPublisher) {
    this.orderRepository = orderRepository;
    this.paymentAttemptRepository = paymentAttemptRepository;
    this.paymentGateway = paymentGateway;
    this.webhookVerifier = webhookVerifier;
    this.paymentStatusMapper = paymentStatusMapper;
    this.eventPublisher = eventPublisher;
  }

  @Transactional
  public InitiatePaymentResponseDto initiatePayment(
      Long orderId, Long clientId, InitiatePaymentRequestDto dto) {

    Order order =
        orderRepository
            .findById(orderId)
            .orElseThrow(
                () -> new OrderNotFoundException("Pedido não encontrado com id: " + orderId));

    if (!order.getClient().getId().equals(clientId)) {
      throw new OrderOwnershipException(
          "Acesso negado: o pedido não pertence ao usuário autenticado.");
    }

    if (order.getStatus() == OrderStatus.PAYMENT_PENDING) {
      throw new PaymentAlreadyInProgressException(
          "Já existe um pagamento em andamento para este pedido.");
    }

    if (order.getStatus() == OrderStatus.PAYMENT_CONFIRMED
        || order.getStatus() == OrderStatus.CANCELLED) {
      throw new IllegalArgumentException(
          "Pedido não pode ser pago no status atual: " + order.getStatus());
    }

    int attemptNumber =
        paymentAttemptRepository
            .findTopByOrderIdOrderByAttemptNumberDesc(orderId)
            .map(a -> a.getAttemptNumber() + 1)
            .orElse(1);

    String idempotencyKey = "order-" + orderId + "-attempt-" + attemptNumber;

    PaymentAttempt attempt = new PaymentAttempt();
    attempt.setOrder(order);
    attempt.setProvider(PROVIDER);
    attempt.setMethod(dto.paymentType());
    attempt.setAmount(order.getTotal());
    attempt.setCurrency(CURRENCY);
    attempt.setIdempotencyKey(idempotencyKey);
    attempt.setAttemptNumber(attemptNumber);
    attempt.setStatus(PaymentAttemptStatus.CREATED);
    paymentAttemptRepository.save(attempt);

    order.setStatus(OrderStatus.PAYMENT_PENDING);
    orderRepository.save(order);

    try {
      CreatePaymentCommand command = buildCommand(order, dto, idempotencyKey);
      GatewayPaymentResult result = paymentGateway.createPayment(command);

      PaymentAttemptStatus attemptStatus =
          paymentStatusMapper.toAttemptStatus(result.status(), result.statusDetail());

      attempt.setProviderOrderId(result.providerOrderId());
      attempt.setProviderPaymentId(result.providerPaymentId());
      attempt.setStatus(attemptStatus);
      attempt.setStatusDetail(result.statusDetail());
      if (result.paymentMethodId() != null) {
        attempt.setMethod(result.paymentMethodId());
      }
      paymentAttemptRepository.save(attempt);

      order.setPaymentId(result.providerOrderId());
      OrderStatus newOrderStatus = paymentStatusMapper.toOrderStatus(attemptStatus);
      if (newOrderStatus != null) {
        order.setStatus(newOrderStatus);
      }
      orderRepository.save(order);

      if (attemptStatus == PaymentAttemptStatus.APPROVED) {
        eventPublisher.publishEvent(
            new PaymentApprovedEvent(
                order.getId(), attempt.getId(), PaymentAttemptStatus.CREATED, Instant.now()));
      }

      return new InitiatePaymentResponseDto(
          result.providerOrderId(),
          result.providerPaymentId(),
          result.status(),
          result.statusDetail(),
          result.qrCode(),
          result.qrCodeBase64(),
          result.ticketUrl(),
          result.digitableLine());

    } catch (RuntimeException e) {
      attempt.setStatus(PaymentAttemptStatus.FAILED);
      attempt.setStatusDetail(e.getMessage());
      paymentAttemptRepository.save(attempt);
      order.setStatus(OrderStatus.PENDING);
      orderRepository.save(order);
      throw e;
    }
  }

  @Transactional
  public void handleWebhook(String xSignature, String xRequestId, MercadoPagoWebhookDto dto) {
    if (!"order".equals(dto.type())) return;
    if (dto.data() == null || dto.data().id() == null) return;

    webhookVerifier.verify(xSignature, xRequestId, dto.data().id());

    try {
      GatewayOrderResult result = paymentGateway.getOrderStatus(dto.data().id());

      PaymentAttempt attempt =
          paymentAttemptRepository.findByProviderOrderId(result.providerOrderId()).orElse(null);
      if (attempt == null) return;

      PaymentAttemptStatus oldStatus = attempt.getStatus();
      PaymentAttemptStatus newAttemptStatus =
          paymentStatusMapper.toAttemptStatus(result.status(), result.statusDetail());

      attempt.setStatus(newAttemptStatus);
      attempt.setStatusDetail(result.statusDetail());

      if (result.paymentMethodId() != null) {
        attempt.setMethod(result.paymentMethodId());
        attempt.getOrder().setPaymentMethod(result.paymentMethodId());
      }
      paymentAttemptRepository.save(attempt);

      OrderStatus newOrderStatus = paymentStatusMapper.toOrderStatus(newAttemptStatus);
      if (newOrderStatus != null) {
        attempt.getOrder().setStatus(newOrderStatus);
        orderRepository.save(attempt.getOrder());
      }

      if (oldStatus != newAttemptStatus) {
        if (newAttemptStatus == PaymentAttemptStatus.APPROVED) {
          eventPublisher.publishEvent(
              new PaymentApprovedEvent(
                  attempt.getOrder().getId(), attempt.getId(), oldStatus, Instant.now()));
        } else if (isTerminalFailureStatus(newAttemptStatus)) {
          eventPublisher.publishEvent(
              new PaymentFailedEvent(
                  attempt.getOrder().getId(),
                  attempt.getId(),
                  oldStatus,
                  newAttemptStatus,
                  Instant.now()));
        }
      }

    } catch (PaymentGatewayException e) {
      throw new WebhookProcessingException("Erro ao processar webhook do Mercado Pago.", e);
    }
  }

  private CreatePaymentCommand buildCommand(
      Order order, InitiatePaymentRequestDto dto, String idempotencyKey) {

    Address address = resolveAddress(order);

    Client client = order.getClient();
    String[] nameParts = splitName(client.getName());

    CreatePaymentCommand.Payer payer =
        new CreatePaymentCommand.Payer(
            client.getEmail(),
            nameParts[0],
            nameParts[1],
            DOCUMENT_TYPE_CPF,
            sanitizeDigitsOnly(client.getCpf()),
            address != null ? toCommandAddress(address) : null);

    List<CreatePaymentCommand.Item> items =
        order.getItems().stream()
            .map(
                item ->
                    new CreatePaymentCommand.Item(
                        item.getProduct().getName(), item.getUnitPrice(), item.getQuantity()))
            .toList();

    return new CreatePaymentCommand(
        order.getTotal(),
        CURRENCY,
        sanitizeExternalReference(order.getOrderNumber()),
        idempotencyKey,
        payer,
        items,
        toPaymentMethod(dto));
  }

  private Address resolveAddress(Order order) {
    if (order.getDeliveryAddress() != null) {
      return order.getDeliveryAddress();
    }
    Client client = order.getClient();
    if (client.getAddresses() != null && !client.getAddresses().isEmpty()) {
      return client.getAddresses().get(0);
    }
    return null;
  }

  private CreatePaymentCommand.Address toCommandAddress(Address jpaAddress) {
    return new CreatePaymentCommand.Address(
        jpaAddress.getStreet(),
        jpaAddress.getNumber(),
        sanitizeDigitsOnly(jpaAddress.getZipCode()),
        jpaAddress.getDistrict(),
        jpaAddress.getCity().getName(),
        jpaAddress.getCity().getState().getAbbreviation());
  }

  private CreatePaymentCommand.PaymentMethod toPaymentMethod(InitiatePaymentRequestDto dto) {
    return switch (dto.paymentType()) {
      case "PIX" -> new CreatePaymentCommand.Pix();
      case "BOLETO" -> new CreatePaymentCommand.Boleto();
      case "CREDIT_CARD" -> new CreatePaymentCommand.CreditCard(
          dto.paymentMethodId(),
          dto.cardToken(),
          dto.installments() != null ? dto.installments() : 1);
      default -> throw new IllegalArgumentException(
          "Método de pagamento inválido: " + dto.paymentType());
    };
  }

  private String[] splitName(String fullName) {
    if (fullName == null || fullName.isBlank()) return new String[] {"N/A", "N/A"};
    int spaceIndex = fullName.indexOf(' ');
    if (spaceIndex == -1) return new String[] {fullName, fullName};
    return new String[] {fullName.substring(0, spaceIndex), fullName.substring(spaceIndex + 1)};
  }

  private String sanitizeDigitsOnly(String value) {
    return value != null ? value.replaceAll("\\D", "") : "";
  }

  private String sanitizeExternalReference(String orderNumber) {
    return orderNumber.replaceAll("[^a-zA-Z0-9_-]", "_");
  }

  private static boolean isTerminalFailureStatus(PaymentAttemptStatus status) {
    return status == PaymentAttemptStatus.FAILED
        || status == PaymentAttemptStatus.CANCELLED
        || status == PaymentAttemptStatus.EXPIRED;
  }
}
