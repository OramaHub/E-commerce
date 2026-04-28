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
import com.orama.e_commerce.exceptions.payment.PermanentPaymentGatewayException;
import com.orama.e_commerce.exceptions.payment.WebhookProcessingException;
import com.orama.e_commerce.exceptions.payment.WebhookSignatureException;
import com.orama.e_commerce.models.Address;
import com.orama.e_commerce.models.Client;
import com.orama.e_commerce.models.Order;
import com.orama.e_commerce.models.OrderShippingAddress;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Service
public class PaymentApplicationService {

  private static final Logger log = LoggerFactory.getLogger(PaymentApplicationService.class);

  private static final String PROVIDER = "MERCADOPAGO";
  private static final String CURRENCY = "BRL";
  private static final String DOCUMENT_TYPE_CPF = "CPF";

  private final OrderRepository orderRepository;
  private final PaymentAttemptRepository paymentAttemptRepository;
  private final PaymentGateway paymentGateway;
  private final WebhookVerifier webhookVerifier;
  private final PaymentStatusMapper paymentStatusMapper;
  private final ApplicationEventPublisher eventPublisher;
  private final PaymentAttemptService paymentAttemptService;

  public PaymentApplicationService(
      OrderRepository orderRepository,
      PaymentAttemptRepository paymentAttemptRepository,
      PaymentGateway paymentGateway,
      WebhookVerifier webhookVerifier,
      PaymentStatusMapper paymentStatusMapper,
      ApplicationEventPublisher eventPublisher,
      PaymentAttemptService paymentAttemptService) {
    this.orderRepository = orderRepository;
    this.paymentAttemptRepository = paymentAttemptRepository;
    this.paymentGateway = paymentGateway;
    this.webhookVerifier = webhookVerifier;
    this.paymentStatusMapper = paymentStatusMapper;
    this.eventPublisher = eventPublisher;
    this.paymentAttemptService = paymentAttemptService;
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
    paymentAttemptService.openAttempt(attempt, order);

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
          result.digitableLine(),
          result.challengeUrl());

    } catch (RuntimeException e) {
      paymentAttemptService.markFailed(attempt, order, e.getMessage());
      throw e;
    }
  }

  @Transactional
  public void handleWebhook(String xSignature, String xRequestId, MercadoPagoWebhookDto dto) {
    handleWebhook(xSignature, xRequestId, null, dto);
  }

  @Transactional
  public void handleWebhook(
      String xSignature, String xRequestId, String queryDataId, MercadoPagoWebhookDto dto) {
    if (dto == null || !isOrderWebhook(dto.type())) return;

    String dataId = resolveWebhookDataId(queryDataId, dto);
    if (dataId == null || dataId.isBlank()) {
      log.warn(
          "Webhook de order rejeitado: data.id ausente. action={}, xRequestId={}",
          dto.action(),
          maskIdentifier(xRequestId));
      throw new WebhookSignatureException("Webhook de order sem data.id.");
    }

    webhookVerifier.verify(xSignature, xRequestId, dataId);

    try {
      GatewayOrderResult result = paymentGateway.getOrderStatus(dataId);
      PaymentAttempt attempt =
          paymentAttemptRepository.findByProviderOrderId(result.providerOrderId()).orElse(null);
      if (attempt == null) return;
      applyGatewayResult(attempt, result);
    } catch (PermanentPaymentGatewayException e) {
      log.warn(
          "Webhook de order ignorado: gateway nao encontrou/aceitou data.id. dataId={}, xRequestId={}, message={}",
          maskIdentifier(dataId),
          maskIdentifier(xRequestId),
          e.getMessage());
    } catch (PaymentGatewayException e) {
      throw new WebhookProcessingException("Erro ao processar webhook do Mercado Pago.", e);
    }
  }

  @Transactional
  public void applyReconciliation(Long attemptId, GatewayOrderResult result) {
    PaymentAttempt attempt = paymentAttemptRepository.findById(attemptId).orElse(null);
    if (attempt == null) return;
    applyGatewayResult(attempt, result);
  }

  @Transactional
  public InitiatePaymentResponseDto syncPaymentStatus(Long orderId, Long clientId) {
    Order order =
        orderRepository
            .findById(orderId)
            .orElseThrow(
                () -> new OrderNotFoundException("Pedido nÃ£o encontrado com id: " + orderId));

    if (!order.getClient().getId().equals(clientId)) {
      throw new OrderOwnershipException(
          "Acesso negado: o pedido nÃ£o pertence ao usuÃ¡rio autenticado.");
    }

    if (order.getPaymentId() == null || order.getPaymentId().isBlank()) {
      return new InitiatePaymentResponseDto(null, null, "created", null, null, null, null, null);
    }

    GatewayOrderResult result = paymentGateway.getOrderStatus(order.getPaymentId());
    PaymentAttempt attempt =
        paymentAttemptRepository.findByProviderOrderId(result.providerOrderId()).orElse(null);
    String providerPaymentId = attempt != null ? attempt.getProviderPaymentId() : null;

    if (attempt != null) {
      applyGatewayResult(attempt, result);
    }

    return new InitiatePaymentResponseDto(
        result.providerOrderId(),
        providerPaymentId,
        result.status(),
        result.statusDetail(),
        null,
        null,
        null,
        null);
  }

  @Transactional
  public OrderStatus cancelOrRefundRemotePayment(Order order) {
    if (order.getPaymentId() == null || order.getPaymentId().isBlank()) {
      return OrderStatus.CANCELLED;
    }

    GatewayOrderResult currentResult = paymentGateway.getOrderStatus(order.getPaymentId());
    PaymentAttemptStatus currentStatus =
        paymentStatusMapper.toAttemptStatus(currentResult.status(), currentResult.statusDetail());

    if (currentStatus == PaymentAttemptStatus.REFUNDED) {
      applyGatewayResultByProviderOrderId(currentResult);
      return OrderStatus.REFUNDED;
    }
    if (isRemoteCancelledStatus(currentStatus)) {
      applyGatewayResultByProviderOrderId(currentResult);
      return OrderStatus.CANCELLED;
    }
    if (currentStatus == PaymentAttemptStatus.CHARGED_BACK) {
      throw new IllegalArgumentException(
          "Pedido com chargeback nao pode ser cancelado automaticamente.");
    }

    GatewayOrderResult operationResult =
        currentStatus == PaymentAttemptStatus.APPROVED
            ? paymentGateway.refundOrder(order.getPaymentId(), operationKey(order, "refund"))
            : paymentGateway.cancelOrder(order.getPaymentId(), operationKey(order, "cancel"));

    PaymentAttemptStatus operationStatus = applyGatewayResultByProviderOrderId(operationResult);
    OrderStatus orderStatus = paymentStatusMapper.toOrderStatus(operationStatus);
    if (orderStatus == null) {
      throw new IllegalStateException(
          "Mercado Pago retornou status intermediario apos cancelamento/reembolso: "
              + operationResult.status()
              + "/"
              + operationResult.statusDetail());
    }
    return orderStatus;
  }

  private void applyGatewayResult(PaymentAttempt attempt, GatewayOrderResult result) {
    PaymentAttemptStatus oldStatus = attempt.getStatus();
    PaymentAttemptStatus newAttemptStatus =
        paymentStatusMapper.toAttemptStatus(result.status(), result.statusDetail());

    if (!isAllowedAttemptTransition(oldStatus, newAttemptStatus)) {
      log.warn(
          "Transicao de pagamento ignorada. attemptId={}, providerOrderId={}, oldStatus={}, newStatus={}, mpStatus={}, mpStatusDetail={}",
          attempt.getId(),
          result.providerOrderId(),
          oldStatus,
          newAttemptStatus,
          result.status(),
          result.statusDetail());
      return;
    }

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
  }

  private PaymentAttemptStatus applyGatewayResultByProviderOrderId(GatewayOrderResult result) {
    PaymentAttemptStatus attemptStatus =
        paymentStatusMapper.toAttemptStatus(result.status(), result.statusDetail());
    paymentAttemptRepository
        .findByProviderOrderId(result.providerOrderId())
        .ifPresent(attempt -> applyGatewayResult(attempt, result));
    return attemptStatus;
  }

  private CreatePaymentCommand buildCommand(
      Order order, InitiatePaymentRequestDto dto, String idempotencyKey) {

    CreatePaymentCommand.Address address = resolveCommandAddress(order);

    Client client = order.getClient();
    String[] nameParts = splitName(client.getName());

    CreatePaymentCommand.Payer payer =
        new CreatePaymentCommand.Payer(
            client.getEmail(),
            nameParts[0],
            nameParts[1],
            DOCUMENT_TYPE_CPF,
            sanitizeDigitsOnly(client.getCpf()),
            address);

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
    Client client = order.getClient();
    if (client.getAddresses() != null && !client.getAddresses().isEmpty()) {
      return client.getAddresses().get(0);
    }
    return null;
  }

  private CreatePaymentCommand.Address resolveCommandAddress(Order order) {
    if (order.getShippingAddress() != null) {
      return toCommandAddress(order.getShippingAddress());
    }
    Address address = resolveAddress(order);
    return address != null ? toCommandAddress(address) : null;
  }

  private CreatePaymentCommand.Address toCommandAddress(OrderShippingAddress snapshot) {
    return new CreatePaymentCommand.Address(
        snapshot.getStreet(),
        snapshot.getNumber(),
        sanitizeDigitsOnly(snapshot.getZipCode()),
        snapshot.getDistrict(),
        snapshot.getCityName(),
        snapshot.getStateUf());
  }

  private CreatePaymentCommand.Address toCommandAddress(Address jpaAddress) {
    return new CreatePaymentCommand.Address(
        jpaAddress.getStreet(),
        jpaAddress.getNumber(),
        sanitizeDigitsOnly(jpaAddress.getZipCode()),
        jpaAddress.getDistrict(),
        jpaAddress.getCityName(),
        jpaAddress.getStateUf());
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

  private boolean hasText(String value) {
    return value != null && !value.isBlank();
  }

  private String sanitizeExternalReference(String orderNumber) {
    return orderNumber.replaceAll("[^a-zA-Z0-9_-]", "_");
  }

  private boolean isOrderWebhook(String type) {
    return "order".equals(type) || "orders".equals(type);
  }

  private String resolveWebhookDataId(String queryDataId, MercadoPagoWebhookDto dto) {
    if (queryDataId != null && !queryDataId.isBlank()) {
      return queryDataId;
    }
    if (dto.data() == null) {
      return null;
    }
    return dto.data().id();
  }

  private String maskIdentifier(String value) {
    if (value == null || value.isBlank()) return "missing";
    if (value.length() <= 6) return "***";
    return value.substring(0, 3) + "***" + value.substring(value.length() - 3);
  }

  private String operationKey(Order order, String operation) {
    return "order-" + order.getId() + "-" + operation + "-v1";
  }

  private static boolean isTerminalFailureStatus(PaymentAttemptStatus status) {
    return status == PaymentAttemptStatus.FAILED
        || status == PaymentAttemptStatus.CANCELLED
        || status == PaymentAttemptStatus.EXPIRED;
  }

  private static boolean isRemoteCancelledStatus(PaymentAttemptStatus status) {
    return status == PaymentAttemptStatus.FAILED
        || status == PaymentAttemptStatus.CANCELLED
        || status == PaymentAttemptStatus.EXPIRED;
  }

  private static boolean isAllowedAttemptTransition(
      PaymentAttemptStatus oldStatus, PaymentAttemptStatus newStatus) {
    if (oldStatus == null || oldStatus == newStatus) {
      return true;
    }
    if (oldStatus == PaymentAttemptStatus.APPROVED) {
      return newStatus == PaymentAttemptStatus.REFUNDED
          || newStatus == PaymentAttemptStatus.CHARGED_BACK;
    }
    return !isTerminalAttemptStatus(oldStatus);
  }

  private static boolean isTerminalAttemptStatus(PaymentAttemptStatus status) {
    return status == PaymentAttemptStatus.APPROVED
        || status == PaymentAttemptStatus.FAILED
        || status == PaymentAttemptStatus.CANCELLED
        || status == PaymentAttemptStatus.EXPIRED
        || status == PaymentAttemptStatus.REFUNDED
        || status == PaymentAttemptStatus.CHARGED_BACK;
  }
}
