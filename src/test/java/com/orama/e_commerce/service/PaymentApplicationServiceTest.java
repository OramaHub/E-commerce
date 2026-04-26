package com.orama.e_commerce.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

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
import com.orama.e_commerce.exceptions.payment.PermanentPaymentGatewayException;
import com.orama.e_commerce.exceptions.payment.TransientPaymentGatewayException;
import com.orama.e_commerce.exceptions.payment.WebhookProcessingException;
import com.orama.e_commerce.exceptions.payment.WebhookSignatureException;
import com.orama.e_commerce.models.Address;
import com.orama.e_commerce.models.Client;
import com.orama.e_commerce.models.Order;
import com.orama.e_commerce.models.PaymentAttempt;
import com.orama.e_commerce.repository.OrderRepository;
import com.orama.e_commerce.repository.PaymentAttemptRepository;
import com.orama.e_commerce.service.fake.FakePaymentGateway;
import com.orama.e_commerce.service.gateway.CreatePaymentCommand;
import com.orama.e_commerce.service.gateway.GatewayOrderResult;
import com.orama.e_commerce.service.gateway.GatewayPaymentResult;
import com.orama.e_commerce.testdata.AddressTestBuilder;
import com.orama.e_commerce.testdata.ClientTestBuilder;
import com.orama.e_commerce.testdata.OrderTestBuilder;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.context.ApplicationEventPublisher;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PaymentApplicationServiceTest {

  @Mock OrderRepository orderRepository;
  @Mock PaymentAttemptRepository paymentAttemptRepository;
  @Mock WebhookVerifier webhookVerifier;
  @Mock ApplicationEventPublisher eventPublisher;

  FakePaymentGateway fakeGateway;
  PaymentStatusMapper statusMapper;
  PaymentAttemptService paymentAttemptService;
  PaymentApplicationService service;

  @BeforeEach
  void setUp() {
    fakeGateway = new FakePaymentGateway();
    statusMapper = new PaymentStatusMapper();
    paymentAttemptService =
        new PaymentAttemptService(paymentAttemptRepository, orderRepository, eventPublisher);
    service =
        new PaymentApplicationService(
            orderRepository,
            paymentAttemptRepository,
            fakeGateway,
            webhookVerifier,
            statusMapper,
            eventPublisher,
            paymentAttemptService);
  }

  private InitiatePaymentRequestDto pixRequest() {
    return new InitiatePaymentRequestDto("PIX", null, null, null);
  }

  private InitiatePaymentRequestDto creditCardRequest() {
    return new InitiatePaymentRequestDto("CREDIT_CARD", "tok_abc", 3, "visa");
  }

  private GatewayPaymentResult pixPendingResult() {
    return new GatewayPaymentResult(
        "MP-ORDER-123",
        "MP-PAY-456",
        "processing",
        null,
        "pix",
        "00020126...",
        "BASE64QR",
        null,
        null);
  }

  private GatewayPaymentResult creditCardApprovedResult() {
    return new GatewayPaymentResult(
        "MP-ORDER-789", "MP-PAY-101", "processed", null, "visa", null, null, null, null);
  }

  @Nested
  @DisplayName("Happy paths")
  class HappyPaths {

    @Test
    @DisplayName("PIX pending: attempt PENDING e Order permanece em PAYMENT_PENDING")
    void initiatePayment_pix_pending_keepsOrderInPaymentPending() {
      Address address = AddressTestBuilder.anAddress().withZipCode("01234-567").build();
      Client client =
          ClientTestBuilder.aClient()
              .withId(50L)
              .withName("Maria Souza")
              .withEmail("maria@example.com")
              .withCpf("111.222.333-44")
              .withAddresses(address)
              .build();
      Order order =
          OrderTestBuilder.anOrder()
              .withId(1L)
              .withOrderNumber("ORD-2026-0001")
              .withStatus(OrderStatus.PENDING)
              .withTotal(new BigDecimal("150.00"))
              .withClient(client)
              .withDeliveryAddress(address)
              .withItem("Produto X", new BigDecimal("150.00"), 1)
              .build();

      when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
      when(paymentAttemptRepository.findTopByOrderIdOrderByAttemptNumberDesc(1L))
          .thenReturn(Optional.empty());
      fakeGateway.setNextCreateResult(pixPendingResult());

      InitiatePaymentResponseDto response = service.initiatePayment(1L, 50L, pixRequest());

      assertThat(order.getStatus()).isEqualTo(OrderStatus.PAYMENT_PENDING);
      assertThat(order.getPaymentId()).isEqualTo("MP-ORDER-123");
      assertThat(response.qrCode()).isEqualTo("00020126...");
      assertThat(response.qrCodeBase64()).isEqualTo("BASE64QR");
      assertThat(response.mpOrderId()).isEqualTo("MP-ORDER-123");
      assertThat(response.paymentId()).isEqualTo("MP-PAY-456");
    }

    @Test
    @DisplayName(
        "Cartao aprovado: attempt APPROVED e Order vira PAYMENT_CONFIRMED no fluxo sincrono")
    void initiatePayment_creditCard_approved_setsOrderToConfirmed() {
      Address address = AddressTestBuilder.anAddress().build();
      Client client = ClientTestBuilder.aClient().withId(50L).withAddresses(address).build();
      Order order =
          OrderTestBuilder.anOrder()
              .withId(2L)
              .withClient(client)
              .withDeliveryAddress(address)
              .withStatus(OrderStatus.PENDING)
              .build();

      when(orderRepository.findById(2L)).thenReturn(Optional.of(order));
      when(paymentAttemptRepository.findTopByOrderIdOrderByAttemptNumberDesc(2L))
          .thenReturn(Optional.empty());
      fakeGateway.setNextCreateResult(creditCardApprovedResult());

      service.initiatePayment(2L, 50L, creditCardRequest());

      assertThat(order.getStatus()).isEqualTo(OrderStatus.PAYMENT_CONFIRMED);
      assertThat(order.getPaymentId()).isEqualTo("MP-ORDER-789");
      assertThat(fakeGateway.getCreatePaymentCallCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("Cartao com 3DS challenge: retorna challengeUrl e attempt AWAITING_CHALLENGE")
    void initiatePayment_creditCardPendingChallenge_returnsChallengeUrl() {
      Address address = AddressTestBuilder.anAddress().build();
      Client client = ClientTestBuilder.aClient().withId(50L).withAddresses(address).build();
      Order order =
          OrderTestBuilder.anOrder()
              .withId(3L)
              .withClient(client)
              .withDeliveryAddress(address)
              .withStatus(OrderStatus.PENDING)
              .build();

      when(orderRepository.findById(3L)).thenReturn(Optional.of(order));
      when(paymentAttemptRepository.findTopByOrderIdOrderByAttemptNumberDesc(3L))
          .thenReturn(Optional.empty());
      fakeGateway.setNextCreateResult(
          new GatewayPaymentResult(
              "MP-ORDER-3DS",
              "MP-PAY-3DS",
              "action_required",
              "pending_challenge",
              "master",
              null,
              null,
              null,
              null,
              "https://www.mercadopago.com.br/challenge"));

      InitiatePaymentResponseDto response = service.initiatePayment(3L, 50L, creditCardRequest());

      assertThat(order.getStatus()).isEqualTo(OrderStatus.PAYMENT_PENDING);
      assertThat(response.challengeUrl()).isEqualTo("https://www.mercadopago.com.br/challenge");

      ArgumentCaptor<PaymentAttempt> attemptCaptor = ArgumentCaptor.forClass(PaymentAttempt.class);
      verify(paymentAttemptRepository, times(2)).save(attemptCaptor.capture());
      PaymentAttempt finalAttempt = attemptCaptor.getAllValues().get(1);
      assertThat(finalAttempt.getStatus()).isEqualTo(PaymentAttemptStatus.AWAITING_CHALLENGE);
      assertThat(finalAttempt.getStatusDetail()).isEqualTo("pending_challenge");
    }

    @Test
    @DisplayName("Sync 3DS: consulta MP, atualiza attempt e confirma pedido apos challenge")
    void syncPaymentStatus_afterChallenge_updatesAttemptAndOrder() {
      Client client = ClientTestBuilder.aClient().withId(50L).build();
      Order order =
          OrderTestBuilder.anOrder()
              .withId(4L)
              .withClient(client)
              .withStatus(OrderStatus.PAYMENT_PENDING)
              .build();
      order.setPaymentId("MP-ORDER-3DS");

      PaymentAttempt attempt = new PaymentAttempt();
      attempt.setOrder(order);
      attempt.setProviderOrderId("MP-ORDER-3DS");
      attempt.setProviderPaymentId("MP-PAY-3DS");
      attempt.setStatus(PaymentAttemptStatus.AWAITING_CHALLENGE);

      when(orderRepository.findById(4L)).thenReturn(Optional.of(order));
      when(paymentAttemptRepository.findByProviderOrderId("MP-ORDER-3DS"))
          .thenReturn(Optional.of(attempt));
      fakeGateway.setNextOrderStatusResult(
          new GatewayOrderResult("MP-ORDER-3DS", "processed", "accredited", "master"));

      InitiatePaymentResponseDto response = service.syncPaymentStatus(4L, 50L);

      assertThat(fakeGateway.getLastQueriedOrderId()).isEqualTo("MP-ORDER-3DS");
      assertThat(response.status()).isEqualTo("processed");
      assertThat(response.statusDetail()).isEqualTo("accredited");
      assertThat(response.paymentId()).isEqualTo("MP-PAY-3DS");
      assertThat(attempt.getStatus()).isEqualTo(PaymentAttemptStatus.APPROVED);
      assertThat(order.getStatus()).isEqualTo(OrderStatus.PAYMENT_CONFIRMED);
      verify(eventPublisher).publishEvent(any(PaymentApprovedEvent.class));
    }
  }

  @Nested
  @DisplayName("Construcao do CreatePaymentCommand")
  class CommandConstruction {

    @Test
    @DisplayName("Sanitiza CPF, CEP e orderNumber; faz split de nome; usa idempotencyKey correto")
    void initiatePayment_buildsCommandWithSanitizedFields() {
      Address address = AddressTestBuilder.anAddress().withZipCode("01234-567").build();
      Client client =
          ClientTestBuilder.aClient()
              .withId(50L)
              .withName("Maria Souza")
              .withEmail("maria@example.com")
              .withCpf("111.222.333-44")
              .withAddresses(address)
              .build();
      Order order =
          OrderTestBuilder.anOrder()
              .withId(1L)
              .withOrderNumber("ORD/2026.0001")
              .withStatus(OrderStatus.PENDING)
              .withTotal(new BigDecimal("150.00"))
              .withClient(client)
              .withDeliveryAddress(address)
              .withItem("Produto X", new BigDecimal("150.00"), 1)
              .build();

      when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
      when(paymentAttemptRepository.findTopByOrderIdOrderByAttemptNumberDesc(1L))
          .thenReturn(Optional.empty());
      fakeGateway.setNextCreateResult(pixPendingResult());

      service.initiatePayment(1L, 50L, pixRequest());

      CreatePaymentCommand cmd = fakeGateway.getLastCommand();
      assertThat(cmd).isNotNull();
      assertThat(cmd.idempotencyKey()).isEqualTo("order-1-attempt-1");
      assertThat(cmd.externalReference()).isEqualTo("ORD_2026_0001");
      assertThat(cmd.currency()).isEqualTo("BRL");
      assertThat(cmd.amount()).isEqualByComparingTo("150.00");
      assertThat(cmd.payer().email()).isEqualTo("maria@example.com");
      assertThat(cmd.payer().firstName()).isEqualTo("Maria");
      assertThat(cmd.payer().lastName()).isEqualTo("Souza");
      assertThat(cmd.payer().documentType()).isEqualTo("CPF");
      assertThat(cmd.payer().documentNumber()).isEqualTo("11122233344");
      assertThat(cmd.payer().address()).isNotNull();
      assertThat(cmd.payer().address().zipCode()).isEqualTo("01234567");
      assertThat(cmd.payer().address().city()).isEqualTo("Sao Paulo");
      assertThat(cmd.payer().address().state()).isEqualTo("SP");
      assertThat(cmd.paymentMethod()).isInstanceOf(CreatePaymentCommand.Pix.class);
      assertThat(cmd.items()).hasSize(1);
      assertThat(cmd.items().get(0).title()).isEqualTo("Produto X");
      assertThat(cmd.items().get(0).unitPrice()).isEqualByComparingTo("150.00");
      assertThat(cmd.items().get(0).quantity()).isEqualTo(1);
    }

    @Test
    @DisplayName(
        "CREDIT_CARD: cria CreditCard sealed com paymentMethodId, cardToken e installments")
    void initiatePayment_creditCard_buildsCreditCardPaymentMethod() {
      Address address = AddressTestBuilder.anAddress().build();
      Client client = ClientTestBuilder.aClient().withId(50L).withAddresses(address).build();
      Order order =
          OrderTestBuilder.anOrder()
              .withId(2L)
              .withClient(client)
              .withDeliveryAddress(address)
              .build();

      when(orderRepository.findById(2L)).thenReturn(Optional.of(order));
      when(paymentAttemptRepository.findTopByOrderIdOrderByAttemptNumberDesc(2L))
          .thenReturn(Optional.empty());
      fakeGateway.setNextCreateResult(creditCardApprovedResult());

      service.initiatePayment(2L, 50L, creditCardRequest());

      CreatePaymentCommand cmd = fakeGateway.getLastCommand();
      assertThat(cmd.paymentMethod()).isInstanceOf(CreatePaymentCommand.CreditCard.class);
      CreatePaymentCommand.CreditCard cc = (CreatePaymentCommand.CreditCard) cmd.paymentMethod();
      assertThat(cc.paymentMethodId()).isEqualTo("visa");
      assertThat(cc.cardToken()).isEqualTo("tok_abc");
      assertThat(cc.installments()).isEqualTo(3);
    }
  }

  @Nested
  @DisplayName("Fallback de endereco")
  class AddressFallback {

    @Test
    @DisplayName("Order sem deliveryAddress usa primeiro endereco do Client")
    void initiatePayment_resolvesAddressFromClient_whenDeliveryAddressIsNull() {
      Address a1 = AddressTestBuilder.anAddress().withId(10L).withStreet("Rua Primeiro").build();
      Address a2 = AddressTestBuilder.anAddress().withId(20L).withStreet("Rua Segundo").build();
      Client client = ClientTestBuilder.aClient().withId(50L).withAddresses(a1, a2).build();
      Order order =
          OrderTestBuilder.anOrder().withId(3L).withClient(client).withoutDeliveryAddress().build();

      when(orderRepository.findById(3L)).thenReturn(Optional.of(order));
      when(paymentAttemptRepository.findTopByOrderIdOrderByAttemptNumberDesc(3L))
          .thenReturn(Optional.empty());
      fakeGateway.setNextCreateResult(pixPendingResult());

      service.initiatePayment(3L, 50L, pixRequest());

      CreatePaymentCommand cmd = fakeGateway.getLastCommand();
      assertThat(cmd.payer().address()).isNotNull();
      assertThat(cmd.payer().address().streetName()).isEqualTo("Rua Primeiro");
    }
  }

  @Nested
  @DisplayName("Guards (gateway nunca eh chamado)")
  class Guards {

    @Test
    @DisplayName("Order nao encontrado: lanca OrderNotFoundException")
    void initiatePayment_orderNotFound_throwsOrderNotFoundException() {
      when(orderRepository.findById(99L)).thenReturn(Optional.empty());

      assertThatThrownBy(() -> service.initiatePayment(99L, 1L, pixRequest()))
          .isInstanceOf(OrderNotFoundException.class)
          .hasMessageContaining("99");

      assertThat(fakeGateway.getCreatePaymentCallCount()).isZero();
    }

    @Test
    @DisplayName("Order pertence a outro client: lanca OrderOwnershipException")
    void initiatePayment_orderBelongsToAnotherClient_throwsOwnershipException() {
      Client owner = ClientTestBuilder.aClient().withId(50L).build();
      Order order = OrderTestBuilder.anOrder().withId(1L).withClient(owner).build();

      when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

      assertThatThrownBy(() -> service.initiatePayment(1L, 999L, pixRequest()))
          .isInstanceOf(OrderOwnershipException.class);

      assertThat(fakeGateway.getCreatePaymentCallCount()).isZero();
    }

    @Test
    @DisplayName("Sync status de order de outro client: lanca OrderOwnershipException")
    void syncPaymentStatus_orderBelongsToAnotherClient_throwsOwnershipException() {
      Client owner = ClientTestBuilder.aClient().withId(50L).build();
      Order order = OrderTestBuilder.anOrder().withId(1L).withClient(owner).build();
      order.setPaymentId("MP-ORDER-123");

      when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

      assertThatThrownBy(() -> service.syncPaymentStatus(1L, 999L))
          .isInstanceOf(OrderOwnershipException.class);

      assertThat(fakeGateway.getGetOrderStatusCallCount()).isZero();
    }

    @Test
    @DisplayName("Order em PAYMENT_PENDING: lanca PaymentAlreadyInProgressException")
    void initiatePayment_orderInPaymentPending_throwsAlreadyInProgress() {
      Client client = ClientTestBuilder.aClient().withId(50L).build();
      Order order =
          OrderTestBuilder.anOrder()
              .withId(1L)
              .withClient(client)
              .withStatus(OrderStatus.PAYMENT_PENDING)
              .build();

      when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

      assertThatThrownBy(() -> service.initiatePayment(1L, 50L, pixRequest()))
          .isInstanceOf(PaymentAlreadyInProgressException.class);

      assertThat(fakeGateway.getCreatePaymentCallCount()).isZero();
    }

    @Test
    @DisplayName("Order em PAYMENT_CONFIRMED: lanca IllegalArgumentException")
    void initiatePayment_orderInPaymentConfirmed_throwsIllegalArgument() {
      Client client = ClientTestBuilder.aClient().withId(50L).build();
      Order order =
          OrderTestBuilder.anOrder()
              .withId(1L)
              .withClient(client)
              .withStatus(OrderStatus.PAYMENT_CONFIRMED)
              .build();

      when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

      assertThatThrownBy(() -> service.initiatePayment(1L, 50L, pixRequest()))
          .isInstanceOf(IllegalArgumentException.class);

      assertThat(fakeGateway.getCreatePaymentCallCount()).isZero();
    }

    @Test
    @DisplayName("Order CANCELLED: lanca IllegalArgumentException")
    void initiatePayment_orderCancelled_throwsIllegalArgument() {
      Client client = ClientTestBuilder.aClient().withId(50L).build();
      Order order =
          OrderTestBuilder.anOrder()
              .withId(1L)
              .withClient(client)
              .withStatus(OrderStatus.CANCELLED)
              .build();

      when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

      assertThatThrownBy(() -> service.initiatePayment(1L, 50L, pixRequest()))
          .isInstanceOf(IllegalArgumentException.class);

      assertThat(fakeGateway.getCreatePaymentCallCount()).isZero();
    }
  }

  @Nested
  @DisplayName("Falhas do gateway (rollback)")
  class GatewayFailures {

    @Test
    @DisplayName(
        "TransientPaymentGatewayException: attempt FAILED, Order volta para PENDING, propaga")
    void initiatePayment_transientGatewayException_rollsBackOrder() {
      Client client = ClientTestBuilder.aClient().withId(50L).build();
      Order order =
          OrderTestBuilder.anOrder()
              .withId(1L)
              .withClient(client)
              .withStatus(OrderStatus.PENDING)
              .build();

      when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
      when(paymentAttemptRepository.findTopByOrderIdOrderByAttemptNumberDesc(1L))
          .thenReturn(Optional.empty());

      fakeGateway.setNextCreateException(
          new TransientPaymentGatewayException("Gateway temporariamente indisponivel"));

      assertThatThrownBy(() -> service.initiatePayment(1L, 50L, pixRequest()))
          .isInstanceOf(TransientPaymentGatewayException.class)
          .hasMessageContaining("temporariamente");

      assertThat(order.getStatus()).isEqualTo(OrderStatus.PENDING);
    }

    @Test
    @DisplayName(
        "PermanentPaymentGatewayException: attempt FAILED, Order volta para PENDING, propaga")
    void initiatePayment_permanentGatewayException_rollsBackOrder() {
      Client client = ClientTestBuilder.aClient().withId(50L).build();
      Order order =
          OrderTestBuilder.anOrder()
              .withId(1L)
              .withClient(client)
              .withStatus(OrderStatus.PENDING)
              .build();

      when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
      when(paymentAttemptRepository.findTopByOrderIdOrderByAttemptNumberDesc(1L))
          .thenReturn(Optional.empty());

      fakeGateway.setNextCreateException(new PermanentPaymentGatewayException("Dados invalidos"));

      assertThatThrownBy(() -> service.initiatePayment(1L, 50L, pixRequest()))
          .isInstanceOf(PermanentPaymentGatewayException.class);

      assertThat(order.getStatus()).isEqualTo(OrderStatus.PENDING);
    }

    @Test
    @DisplayName(
        "Gateway exception: attempt salvo com FAILED via markFailed independente do rollback")
    void initiatePayment_gatewayException_savesAttemptAsFailedViaRequiresNew() {
      Client client = ClientTestBuilder.aClient().withId(50L).build();
      Order order =
          OrderTestBuilder.anOrder()
              .withId(1L)
              .withClient(client)
              .withStatus(OrderStatus.PENDING)
              .build();

      when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
      when(paymentAttemptRepository.findTopByOrderIdOrderByAttemptNumberDesc(1L))
          .thenReturn(Optional.empty());
      fakeGateway.setNextCreateException(new TransientPaymentGatewayException("timeout"));

      assertThatThrownBy(() -> service.initiatePayment(1L, 50L, pixRequest()))
          .isInstanceOf(TransientPaymentGatewayException.class);

      verify(paymentAttemptRepository, times(2)).save(any());
    }
  }

  @Nested
  @DisplayName("Idempotencia")
  class Idempotency {

    @Test
    @DisplayName("Segunda tentativa apos FAILED: attemptNumber=2 e idempotencyKey distinto")
    void initiatePayment_secondAttempt_incrementsAttemptNumber() {
      Client client = ClientTestBuilder.aClient().withId(50L).build();
      Order order =
          OrderTestBuilder.anOrder()
              .withId(1L)
              .withClient(client)
              .withStatus(OrderStatus.PENDING)
              .build();

      PaymentAttempt previousAttempt = new PaymentAttempt();
      previousAttempt.setAttemptNumber(1);

      when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
      when(paymentAttemptRepository.findTopByOrderIdOrderByAttemptNumberDesc(1L))
          .thenReturn(Optional.of(previousAttempt));
      fakeGateway.setNextCreateResult(pixPendingResult());

      service.initiatePayment(1L, 50L, pixRequest());

      CreatePaymentCommand cmd = fakeGateway.getLastCommand();
      assertThat(cmd.idempotencyKey()).isEqualTo("order-1-attempt-2");
    }
  }

  @Nested
  @DisplayName("Webhook")
  class Webhook {

    @Test
    @DisplayName("Order aprovada: attempt APPROVED, Order vira PAYMENT_CONFIRMED, verify chamado")
    void handleWebhook_approvedOrder_setsOrderToConfirmed() {
      Order order =
          OrderTestBuilder.anOrder().withId(1L).withStatus(OrderStatus.PAYMENT_PENDING).build();
      PaymentAttempt attempt = new PaymentAttempt();
      attempt.setOrder(order);
      attempt.setStatus(PaymentAttemptStatus.PENDING);
      attempt.setProviderOrderId("MP-ORDER-123");

      MercadoPagoWebhookDto dto =
          new MercadoPagoWebhookDto(
              "order", "order.updated", new MercadoPagoWebhookDto.WebhookData("MP-ORDER-123"));

      when(paymentAttemptRepository.findByProviderOrderId("MP-ORDER-123"))
          .thenReturn(Optional.of(attempt));
      fakeGateway.setNextOrderStatusResult(
          new GatewayOrderResult("MP-ORDER-123", "processed", null, "visa"));

      service.handleWebhook("ts=1,v1=abc", "req-id-1", dto);

      assertThat(attempt.getStatus()).isEqualTo(PaymentAttemptStatus.APPROVED);
      assertThat(order.getStatus()).isEqualTo(OrderStatus.PAYMENT_CONFIRMED);
      assertThat(order.getPaymentMethod()).isEqualTo("visa");
      verify(webhookVerifier).verify("ts=1,v1=abc", "req-id-1", "MP-ORDER-123");
    }

    @Test
    @DisplayName("data.id via query tem precedencia sobre body")
    void handleWebhook_queryDataId_takesPrecedenceOverBodyDataId() {
      Order order =
          OrderTestBuilder.anOrder().withId(1L).withStatus(OrderStatus.PAYMENT_PENDING).build();
      PaymentAttempt attempt = new PaymentAttempt();
      attempt.setOrder(order);
      attempt.setStatus(PaymentAttemptStatus.PENDING);
      attempt.setProviderOrderId("MP-ORDER-QUERY");

      MercadoPagoWebhookDto dto =
          new MercadoPagoWebhookDto(
              "order", "order.updated", new MercadoPagoWebhookDto.WebhookData("MP-ORDER-BODY"));

      when(paymentAttemptRepository.findByProviderOrderId("MP-ORDER-QUERY"))
          .thenReturn(Optional.of(attempt));
      fakeGateway.setNextOrderStatusResult(
          new GatewayOrderResult("MP-ORDER-QUERY", "processed", null, "visa"));

      service.handleWebhook("sig", "req-id-1", "MP-ORDER-QUERY", dto);

      assertThat(fakeGateway.getLastQueriedOrderId()).isEqualTo("MP-ORDER-QUERY");
      verify(webhookVerifier).verify("sig", "req-id-1", "MP-ORDER-QUERY");
    }

    @Test
    @DisplayName("Webhook de order sem data.id e rejeitado antes de consultar gateway")
    void handleWebhook_missingDataId_throwsSignatureException() {
      MercadoPagoWebhookDto dto = new MercadoPagoWebhookDto("order", "order.updated", null);

      assertThatThrownBy(() -> service.handleWebhook("sig", "req-id-1", null, dto))
          .isInstanceOf(WebhookSignatureException.class);

      verify(webhookVerifier, never()).verify(any(), any(), any());
      assertThat(fakeGateway.getGetOrderStatusCallCount()).isZero();
    }

    @Test
    @DisplayName("ProviderOrderId desconhecido: no-op silencioso, sem update")
    void handleWebhook_unknownProviderOrderId_isNoOp() {
      MercadoPagoWebhookDto dto =
          new MercadoPagoWebhookDto(
              "order", "order.updated", new MercadoPagoWebhookDto.WebhookData("UNKNOWN-ID"));

      when(paymentAttemptRepository.findByProviderOrderId("UNKNOWN-ID"))
          .thenReturn(Optional.empty());
      fakeGateway.setNextOrderStatusResult(
          new GatewayOrderResult("UNKNOWN-ID", "processing", null, null));

      assertThatNoException().isThrownBy(() -> service.handleWebhook("sig", "req-id", dto));

      verify(webhookVerifier).verify("sig", "req-id", "UNKNOWN-ID");
      verify(paymentAttemptRepository, never()).save(any());
      verify(orderRepository, never()).save(any());
    }

    @Test
    @DisplayName("Gateway lanca PaymentGatewayException: traduz para WebhookProcessingException")
    void handleWebhook_gatewayException_throwsWebhookProcessingException() {
      MercadoPagoWebhookDto dto =
          new MercadoPagoWebhookDto(
              "order", "order.updated", new MercadoPagoWebhookDto.WebhookData("MP-ORDER-123"));

      fakeGateway.setNextOrderStatusException(
          new TransientPaymentGatewayException("MP fora do ar"));

      assertThatThrownBy(() -> service.handleWebhook("sig", "req-id", dto))
          .isInstanceOf(WebhookProcessingException.class)
          .hasCauseInstanceOf(TransientPaymentGatewayException.class);
    }

    @Test
    @DisplayName("type != 'order': no-op silencioso, verifier nao eh chamado")
    void handleWebhook_typeIsNotOrder_isNoOpWithoutVerifying() {
      MercadoPagoWebhookDto dto =
          new MercadoPagoWebhookDto(
              "payment", "payment.created", new MercadoPagoWebhookDto.WebhookData("MP-PAY-999"));

      service.handleWebhook("sig", "req-id", dto);

      verify(webhookVerifier, never()).verify(any(), any(), any());
      assertThat(fakeGateway.getGetOrderStatusCallCount()).isZero();
    }
  }

  @Nested
  @DisplayName("Eventos de dominio")
  class Events {

    @Test
    @DisplayName(
        "Cartao aprovado no initiate: publica PaymentApprovedEvent com previousStatus=CREATED")
    void initiatePayment_cardApproved_publishesPaymentApprovedEvent() {
      Client client = ClientTestBuilder.aClient().withId(50L).build();
      Order order =
          OrderTestBuilder.anOrder()
              .withId(10L)
              .withClient(client)
              .withStatus(OrderStatus.PENDING)
              .build();

      when(orderRepository.findById(10L)).thenReturn(Optional.of(order));
      when(paymentAttemptRepository.findTopByOrderIdOrderByAttemptNumberDesc(10L))
          .thenReturn(Optional.empty());
      fakeGateway.setNextCreateResult(creditCardApprovedResult());

      service.initiatePayment(10L, 50L, creditCardRequest());

      ArgumentCaptor<PaymentApprovedEvent> captor =
          ArgumentCaptor.forClass(PaymentApprovedEvent.class);
      verify(eventPublisher).publishEvent(captor.capture());
      PaymentApprovedEvent event = captor.getValue();
      assertThat(event.orderId()).isEqualTo(10L);
      assertThat(event.previousStatus()).isEqualTo(PaymentAttemptStatus.CREATED);
      assertThat(event.occurredAt()).isNotNull();
    }

    @Test
    @DisplayName("PIX pending no initiate: nenhum evento publicado")
    void initiatePayment_pixPending_doesNotPublishAnyEvent() {
      Client client = ClientTestBuilder.aClient().withId(50L).build();
      Order order =
          OrderTestBuilder.anOrder()
              .withId(10L)
              .withClient(client)
              .withStatus(OrderStatus.PENDING)
              .build();

      when(orderRepository.findById(10L)).thenReturn(Optional.of(order));
      when(paymentAttemptRepository.findTopByOrderIdOrderByAttemptNumberDesc(10L))
          .thenReturn(Optional.empty());
      fakeGateway.setNextCreateResult(pixPendingResult());

      service.initiatePayment(10L, 50L, pixRequest());

      verifyNoInteractions(eventPublisher);
    }

    @Test
    @DisplayName(
        "Gateway falha no initiate (rollback path): publica PaymentFailedEvent com previousStatus=CREATED")
    void initiatePayment_gatewayException_publishesPaymentFailedEvent() {
      Client client = ClientTestBuilder.aClient().withId(50L).build();
      Order order =
          OrderTestBuilder.anOrder()
              .withId(10L)
              .withClient(client)
              .withStatus(OrderStatus.PENDING)
              .build();

      when(orderRepository.findById(10L)).thenReturn(Optional.of(order));
      when(paymentAttemptRepository.findTopByOrderIdOrderByAttemptNumberDesc(10L))
          .thenReturn(Optional.empty());
      fakeGateway.setNextCreateException(new TransientPaymentGatewayException("timeout"));

      assertThatThrownBy(() -> service.initiatePayment(10L, 50L, pixRequest()))
          .isInstanceOf(TransientPaymentGatewayException.class);

      ArgumentCaptor<PaymentFailedEvent> captor = ArgumentCaptor.forClass(PaymentFailedEvent.class);
      verify(eventPublisher).publishEvent(captor.capture());
      PaymentFailedEvent event = captor.getValue();
      assertThat(event.previousStatus()).isEqualTo(PaymentAttemptStatus.CREATED);
      assertThat(event.newStatus()).isEqualTo(PaymentAttemptStatus.FAILED);
    }

    @Test
    @DisplayName(
        "Webhook aprova attempt PENDING: publica PaymentApprovedEvent com previousStatus=PENDING")
    void handleWebhook_approvesAttempt_publishesPaymentApprovedEventWithPreviousStatusPending() {
      Order order =
          OrderTestBuilder.anOrder().withId(5L).withStatus(OrderStatus.PAYMENT_PENDING).build();
      PaymentAttempt attempt = new PaymentAttempt();
      attempt.setOrder(order);
      attempt.setStatus(PaymentAttemptStatus.PENDING);
      attempt.setProviderOrderId("MP-ORDER-APRV");

      MercadoPagoWebhookDto dto =
          new MercadoPagoWebhookDto(
              "order", "order.updated", new MercadoPagoWebhookDto.WebhookData("MP-ORDER-APRV"));

      when(paymentAttemptRepository.findByProviderOrderId("MP-ORDER-APRV"))
          .thenReturn(Optional.of(attempt));
      fakeGateway.setNextOrderStatusResult(
          new GatewayOrderResult("MP-ORDER-APRV", "processed", null, null));

      service.handleWebhook("sig", "req-id", dto);

      ArgumentCaptor<PaymentApprovedEvent> captor =
          ArgumentCaptor.forClass(PaymentApprovedEvent.class);
      verify(eventPublisher).publishEvent(captor.capture());
      assertThat(captor.getValue().previousStatus()).isEqualTo(PaymentAttemptStatus.PENDING);
    }

    @Test
    @DisplayName("Webhook duplicado (status ja eh o mesmo): nenhum evento publicado")
    void handleWebhook_sameStatus_doesNotPublishAnyEvent() {
      Order order =
          OrderTestBuilder.anOrder().withId(5L).withStatus(OrderStatus.PAYMENT_PENDING).build();
      PaymentAttempt attempt = new PaymentAttempt();
      attempt.setOrder(order);
      attempt.setStatus(PaymentAttemptStatus.PENDING);
      attempt.setProviderOrderId("MP-ORDER-DUP");

      MercadoPagoWebhookDto dto =
          new MercadoPagoWebhookDto(
              "order", "order.updated", new MercadoPagoWebhookDto.WebhookData("MP-ORDER-DUP"));

      when(paymentAttemptRepository.findByProviderOrderId("MP-ORDER-DUP"))
          .thenReturn(Optional.of(attempt));
      fakeGateway.setNextOrderStatusResult(
          new GatewayOrderResult("MP-ORDER-DUP", "processing", null, null));

      service.handleWebhook("sig", "req-id", dto);

      verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    @DisplayName("Webhook com status FAILED: publica PaymentFailedEvent com newStatus=FAILED")
    void handleWebhook_failedStatus_publishesPaymentFailedEvent() {
      Order order =
          OrderTestBuilder.anOrder().withId(5L).withStatus(OrderStatus.PAYMENT_PENDING).build();
      PaymentAttempt attempt = new PaymentAttempt();
      attempt.setOrder(order);
      attempt.setStatus(PaymentAttemptStatus.PENDING);
      attempt.setProviderOrderId("MP-ORDER-FAIL");

      MercadoPagoWebhookDto dto =
          new MercadoPagoWebhookDto(
              "order", "order.updated", new MercadoPagoWebhookDto.WebhookData("MP-ORDER-FAIL"));

      when(paymentAttemptRepository.findByProviderOrderId("MP-ORDER-FAIL"))
          .thenReturn(Optional.of(attempt));
      fakeGateway.setNextOrderStatusResult(
          new GatewayOrderResult("MP-ORDER-FAIL", "failed", null, null));

      service.handleWebhook("sig", "req-id", dto);

      ArgumentCaptor<PaymentFailedEvent> captor = ArgumentCaptor.forClass(PaymentFailedEvent.class);
      verify(eventPublisher).publishEvent(captor.capture());
      PaymentFailedEvent event = captor.getValue();
      assertThat(event.previousStatus()).isEqualTo(PaymentAttemptStatus.PENDING);
      assertThat(event.newStatus()).isEqualTo(PaymentAttemptStatus.FAILED);
    }

    @Test
    @DisplayName("Webhook com excecao no gateway: nenhum evento publicado")
    void handleWebhook_gatewayException_doesNotPublishAnyEvent() {
      MercadoPagoWebhookDto dto =
          new MercadoPagoWebhookDto(
              "order", "order.updated", new MercadoPagoWebhookDto.WebhookData("MP-ORDER-ERR"));

      fakeGateway.setNextOrderStatusException(new TransientPaymentGatewayException("gateway down"));

      assertThatThrownBy(() -> service.handleWebhook("sig", "req-id", dto))
          .isInstanceOf(WebhookProcessingException.class);

      verifyNoInteractions(eventPublisher);
    }
  }

  @Nested
  @DisplayName("Cancelamento e reembolso")
  class CancellationAndRefund {

    @Test
    @DisplayName("Pedido sem paymentId remoto: retorna CANCELLED sem chamar gateway")
    void cancelOrRefundRemotePayment_withoutPaymentId_returnsCancelled() {
      Order order = OrderTestBuilder.anOrder().withId(10L).withStatus(OrderStatus.PENDING).build();

      OrderStatus result = service.cancelOrRefundRemotePayment(order);

      assertThat(result).isEqualTo(OrderStatus.CANCELLED);
      assertThat(fakeGateway.getGetOrderStatusCallCount()).isZero();
      assertThat(fakeGateway.getCancelOrderCallCount()).isZero();
      assertThat(fakeGateway.getRefundOrderCallCount()).isZero();
    }

    @Test
    @DisplayName(
        "Pagamento pendente remoto: consulta MP, cancela com chave idempotente e retorna CANCELLED")
    void cancelOrRefundRemotePayment_pendingRemotePayment_cancelsRemoteOrder() {
      Order order =
          OrderTestBuilder.anOrder().withId(10L).withStatus(OrderStatus.PAYMENT_PENDING).build();
      order.setPaymentId("MP-ORDER-PENDING");
      PaymentAttempt attempt = new PaymentAttempt();
      attempt.setOrder(order);
      attempt.setStatus(PaymentAttemptStatus.PENDING);
      attempt.setProviderOrderId("MP-ORDER-PENDING");

      when(paymentAttemptRepository.findByProviderOrderId("MP-ORDER-PENDING"))
          .thenReturn(Optional.of(attempt));
      fakeGateway.setNextOrderStatusResult(
          new GatewayOrderResult("MP-ORDER-PENDING", "action_required", "waiting_payment", "pix"));
      fakeGateway.setNextCancelOrderResult(
          new GatewayOrderResult("MP-ORDER-PENDING", "canceled", "canceled", "pix"));

      OrderStatus result = service.cancelOrRefundRemotePayment(order);

      assertThat(result).isEqualTo(OrderStatus.CANCELLED);
      assertThat(fakeGateway.getLastQueriedOrderId()).isEqualTo("MP-ORDER-PENDING");
      assertThat(fakeGateway.getLastCancelledOrderId()).isEqualTo("MP-ORDER-PENDING");
      assertThat(fakeGateway.getLastCancelIdempotencyKey()).isEqualTo("order-10-cancel-v1");
      assertThat(fakeGateway.getRefundOrderCallCount()).isZero();
      assertThat(attempt.getStatus()).isEqualTo(PaymentAttemptStatus.CANCELLED);
      assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELLED);
    }

    @Test
    @DisplayName(
        "Pagamento aprovado remoto: consulta MP, reembolsa com chave idempotente e retorna REFUNDED")
    void cancelOrRefundRemotePayment_approvedRemotePayment_refundsRemoteOrder() {
      Order order =
          OrderTestBuilder.anOrder().withId(11L).withStatus(OrderStatus.PAYMENT_CONFIRMED).build();
      order.setPaymentId("MP-ORDER-APPROVED");
      PaymentAttempt attempt = new PaymentAttempt();
      attempt.setOrder(order);
      attempt.setStatus(PaymentAttemptStatus.APPROVED);
      attempt.setProviderOrderId("MP-ORDER-APPROVED");

      when(paymentAttemptRepository.findByProviderOrderId("MP-ORDER-APPROVED"))
          .thenReturn(Optional.of(attempt));
      fakeGateway.setNextOrderStatusResult(
          new GatewayOrderResult("MP-ORDER-APPROVED", "processed", "accredited", "visa"));
      fakeGateway.setNextRefundOrderResult(
          new GatewayOrderResult("MP-ORDER-APPROVED", "refunded", "refunded", "visa"));

      OrderStatus result = service.cancelOrRefundRemotePayment(order);

      assertThat(result).isEqualTo(OrderStatus.REFUNDED);
      assertThat(fakeGateway.getLastQueriedOrderId()).isEqualTo("MP-ORDER-APPROVED");
      assertThat(fakeGateway.getLastRefundedOrderId()).isEqualTo("MP-ORDER-APPROVED");
      assertThat(fakeGateway.getLastRefundIdempotencyKey()).isEqualTo("order-11-refund-v1");
      assertThat(fakeGateway.getCancelOrderCallCount()).isZero();
      assertThat(attempt.getStatus()).isEqualTo(PaymentAttemptStatus.REFUNDED);
      assertThat(order.getStatus()).isEqualTo(OrderStatus.REFUNDED);
    }

    @Test
    @DisplayName("Pagamento ja reembolsado no MP: nao duplica reembolso")
    void cancelOrRefundRemotePayment_alreadyRefunded_doesNotRefundAgain() {
      Order order =
          OrderTestBuilder.anOrder().withId(12L).withStatus(OrderStatus.PAYMENT_CONFIRMED).build();
      order.setPaymentId("MP-ORDER-REFUNDED");
      PaymentAttempt attempt = new PaymentAttempt();
      attempt.setOrder(order);
      attempt.setStatus(PaymentAttemptStatus.APPROVED);
      attempt.setProviderOrderId("MP-ORDER-REFUNDED");

      when(paymentAttemptRepository.findByProviderOrderId("MP-ORDER-REFUNDED"))
          .thenReturn(Optional.of(attempt));
      fakeGateway.setNextOrderStatusResult(
          new GatewayOrderResult("MP-ORDER-REFUNDED", "refunded", "refunded", "visa"));

      OrderStatus result = service.cancelOrRefundRemotePayment(order);

      assertThat(result).isEqualTo(OrderStatus.REFUNDED);
      assertThat(fakeGateway.getRefundOrderCallCount()).isZero();
      assertThat(fakeGateway.getCancelOrderCallCount()).isZero();
      assertThat(attempt.getStatus()).isEqualTo(PaymentAttemptStatus.REFUNDED);
    }
  }

  @Nested
  @DisplayName("Reconciliacao")
  class Reconciliation {

    @Test
    @DisplayName("PENDING -> APPROVED: attempt APPROVED, Order PAYMENT_CONFIRMED, evento publicado")
    void applyReconciliation_pendingToApproved_updatesAndPublishesEvent() {
      Order order =
          OrderTestBuilder.anOrder().withId(5L).withStatus(OrderStatus.PAYMENT_PENDING).build();
      PaymentAttempt attempt = new PaymentAttempt();
      attempt.setOrder(order);
      attempt.setStatus(PaymentAttemptStatus.PENDING);

      when(paymentAttemptRepository.findById(99L)).thenReturn(Optional.of(attempt));

      service.applyReconciliation(99L, new GatewayOrderResult("MP-ORD", "processed", null, "visa"));

      assertThat(attempt.getStatus()).isEqualTo(PaymentAttemptStatus.APPROVED);
      assertThat(order.getStatus()).isEqualTo(OrderStatus.PAYMENT_CONFIRMED);
      ArgumentCaptor<PaymentApprovedEvent> captor =
          ArgumentCaptor.forClass(PaymentApprovedEvent.class);
      verify(eventPublisher).publishEvent(captor.capture());
      assertThat(captor.getValue().previousStatus()).isEqualTo(PaymentAttemptStatus.PENDING);
    }

    @Test
    @DisplayName("Status igual (PENDING -> PENDING): nenhum evento publicado")
    void applyReconciliation_sameStatus_doesNotPublishEvent() {
      Order order =
          OrderTestBuilder.anOrder().withId(5L).withStatus(OrderStatus.PAYMENT_PENDING).build();
      PaymentAttempt attempt = new PaymentAttempt();
      attempt.setOrder(order);
      attempt.setStatus(PaymentAttemptStatus.PENDING);

      when(paymentAttemptRepository.findById(99L)).thenReturn(Optional.of(attempt));

      service.applyReconciliation(99L, new GatewayOrderResult("MP-ORD", "processing", null, null));

      verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    @DisplayName("Attempt nao encontrado: no-op silencioso")
    void applyReconciliation_attemptNotFound_isNoOp() {
      when(paymentAttemptRepository.findById(404L)).thenReturn(Optional.empty());

      assertThatNoException()
          .isThrownBy(
              () ->
                  service.applyReconciliation(
                      404L, new GatewayOrderResult("X", "processed", null, null)));

      verify(paymentAttemptRepository, never()).save(any());
      verify(orderRepository, never()).save(any());
      verifyNoInteractions(eventPublisher);
    }

    @Test
    @DisplayName("APPROVED -> PENDING: transicao regressiva ignorada")
    void applyReconciliation_approvedToPending_isIgnored() {
      Order order =
          OrderTestBuilder.anOrder().withId(5L).withStatus(OrderStatus.PAYMENT_CONFIRMED).build();
      PaymentAttempt attempt = new PaymentAttempt();
      attempt.setOrder(order);
      attempt.setStatus(PaymentAttemptStatus.APPROVED);

      when(paymentAttemptRepository.findById(99L)).thenReturn(Optional.of(attempt));

      service.applyReconciliation(99L, new GatewayOrderResult("MP-ORD", "processing", null, null));

      assertThat(attempt.getStatus()).isEqualTo(PaymentAttemptStatus.APPROVED);
      assertThat(order.getStatus()).isEqualTo(OrderStatus.PAYMENT_CONFIRMED);
      verify(paymentAttemptRepository, never()).save(any());
      verify(orderRepository, never()).save(any());
      verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    @DisplayName("APPROVED -> REFUNDED: transicao final permitida")
    void applyReconciliation_approvedToRefunded_isAllowed() {
      Order order =
          OrderTestBuilder.anOrder().withId(5L).withStatus(OrderStatus.PAYMENT_CONFIRMED).build();
      PaymentAttempt attempt = new PaymentAttempt();
      attempt.setOrder(order);
      attempt.setStatus(PaymentAttemptStatus.APPROVED);

      when(paymentAttemptRepository.findById(99L)).thenReturn(Optional.of(attempt));

      service.applyReconciliation(
          99L, new GatewayOrderResult("MP-ORD", "refunded", "refunded", null));

      assertThat(attempt.getStatus()).isEqualTo(PaymentAttemptStatus.REFUNDED);
      assertThat(order.getStatus()).isEqualTo(OrderStatus.REFUNDED);
      verify(paymentAttemptRepository).save(attempt);
      verify(orderRepository).save(order);
    }
  }
}
