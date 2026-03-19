package com.orama.e_commerce.service;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.mercadopago.client.order.OrderClient;
import com.mercadopago.client.order.OrderCreateRequest;
import com.mercadopago.client.order.OrderItemRequest;
import com.mercadopago.client.order.OrderPayerAddressRequest;
import com.mercadopago.client.order.OrderPayerRequest;
import com.mercadopago.client.order.OrderPaymentMethodRequest;
import com.mercadopago.client.order.OrderPaymentRequest;
import com.mercadopago.client.order.OrderTransactionRequest;
import com.mercadopago.core.MPRequestOptions;
import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.exceptions.MPException;
import com.mercadopago.resources.common.Identification;
import com.mercadopago.resources.order.Order;
import com.mercadopago.resources.order.OrderPayment;
import com.mercadopago.resources.order.OrderPaymentMethod;
import com.mercadopago.serialization.Serializer;
import com.orama.e_commerce.config.CustomOrderClient;
import com.orama.e_commerce.dtos.payment.InitiatePaymentRequestDto;
import com.orama.e_commerce.dtos.payment.InitiatePaymentResponseDto;
import com.orama.e_commerce.dtos.payment.MercadoPagoWebhookDto;
import com.orama.e_commerce.enums.OrderStatus;
import com.orama.e_commerce.exceptions.order.OrderNotFoundException;
import com.orama.e_commerce.models.Address;
import com.orama.e_commerce.models.Client;
import com.orama.e_commerce.repository.OrderRepository;
import jakarta.transaction.Transactional;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class PaymentService {

  private static final Logger log = LoggerFactory.getLogger(PaymentService.class);
  private static final Gson GSON = new Gson();

  private final OrderRepository orderRepository;

  @Value("${mercadopago.webhook-secret:}")
  private String webhookSecret;

  public PaymentService(OrderRepository orderRepository) {
    this.orderRepository = orderRepository;
  }

  @Transactional
  public InitiatePaymentResponseDto initiatePayment(Long orderId, InitiatePaymentRequestDto dto) {

    com.orama.e_commerce.models.Order order =
        orderRepository
            .findById(orderId)
            .orElseThrow(
                () -> new OrderNotFoundException("Pedido não encontrado com id: " + orderId));

    if (order.getStatus() == OrderStatus.PAYMENT_CONFIRMED
        || order.getStatus() == OrderStatus.CANCELLED) {
      throw new IllegalArgumentException(
          "Pedido não pode ser pago no status atual: " + order.getStatus());
    }

    Client client = order.getClient();
    Address address = order.getDeliveryAddress();
    if (address == null && client.getAddresses() != null && !client.getAddresses().isEmpty()) {
      address = client.getAddresses().get(0);
    }

    List<OrderItemRequest> items =
        order.getItems().stream()
            .map(
                item ->
                    buildItemRequest(
                        item.getProduct().getName(),
                        item.getUnitPrice().toPlainString(),
                        item.getQuantity()))
            .toList();

    OrderCreateRequest request =
        OrderCreateRequest.builder()
            .type("online")
            .processingMode("automatic")
            .captureMode("automatic_async")
            .totalAmount(order.getTotal().toPlainString())
            .externalReference(order.getOrderNumber().replaceAll("[^a-zA-Z0-9_-]", "_"))
            .payer(buildPayer(client, address))
            .items(items)
            .transactions(
                OrderTransactionRequest.builder()
                    .payments(
                        List.of(
                            OrderPaymentRequest.builder()
                                .amount(order.getTotal().toPlainString())
                                .paymentMethod(buildPaymentMethod(dto))
                                .build()))
                    .build())
            .build();

    try {
      JsonObject payload = Serializer.serializeToJson(request);

      boolean isCard = "CREDIT_CARD".equals(dto.paymentType());
      JsonObject paymentMethod =
          payload
              .getAsJsonObject("transactions")
              .getAsJsonArray("payments")
              .get(0)
              .getAsJsonObject()
              .getAsJsonObject("payment_method");

      if (!isCard) {
        paymentMethod.remove("installments");
        paymentMethod.remove("statement_descriptor");
      } else {
        paymentMethod.addProperty(
            "installments", dto.installments() != null ? dto.installments() : 1);
      }

      CustomOrderClient orderClient = new CustomOrderClient();
      MPRequestOptions options =
          MPRequestOptions.builder()
              .customHeaders(Map.of("X-Idempotency-Key", UUID.randomUUID().toString()))
              .build();

      Order mpOrder = orderClient.createOrder(payload, options);

      order.setPaymentId(mpOrder.getId());
      orderRepository.save(order);

      OrderPayment payment = mpOrder.getTransactions().getPayments().get(0);
      OrderPaymentMethod paymentMethodResponse = payment.getPaymentMethod();

      return new InitiatePaymentResponseDto(
          mpOrder.getId(),
          payment.getId(),
          payment.getStatus(),
          payment.getStatusDetail(),
          paymentMethodResponse != null ? paymentMethodResponse.getQrCode() : null,
          paymentMethodResponse != null ? paymentMethodResponse.getQrCodeBase64() : null,
          paymentMethodResponse != null ? paymentMethodResponse.getTicketUrl() : null,
          paymentMethodResponse != null ? paymentMethodResponse.getDigitableLine() : null);

    } catch (MPApiException e) {
      log.error(
          "MP API error - status: {}, body: {}",
          e.getApiResponse().getStatusCode(),
          e.getApiResponse().getContent());
      throw new RuntimeException("Erro ao criar order de pagamento: " + e.getMessage());
    } catch (MPException e) {
      log.error("MP SDK error: {}", e.getMessage());
      throw new RuntimeException("Erro ao criar order de pagamento: " + e.getMessage());
    }
  }

  @Transactional
  public void handleWebhook(String xSignature, String xRequestId, MercadoPagoWebhookDto dto) {

    if (!"order".equals(dto.type())) return;
    if (dto.data() == null || dto.data().id() == null) return;

    if (!isValidSignature(xSignature, xRequestId, dto.data().id())) {
      throw new SecurityException("Assinatura do webhook inválida");
    }

    try {
      OrderClient orderClient = new OrderClient();
      Order mpOrder = orderClient.get(dto.data().id());

      com.orama.e_commerce.models.Order order =
          orderRepository.findByPaymentId(mpOrder.getId()).orElse(null);
      if (order == null) return;

      if (mpOrder.getTransactions() != null
          && mpOrder.getTransactions().getPayments() != null
          && !mpOrder.getTransactions().getPayments().isEmpty()) {

        OrderPayment payment = mpOrder.getTransactions().getPayments().get(0);
        if (payment.getPaymentMethod() != null) {
          order.setPaymentMethod(payment.getPaymentMethod().getId());
        }
      }

      switch (dto.action()) {
        case "order.paid" -> order.setStatus(OrderStatus.PAYMENT_CONFIRMED);
        case "order.cancelled", "order.expired" -> order.setStatus(OrderStatus.CANCELLED);
        default -> {}
      }

      orderRepository.save(order);

    } catch (MPException | MPApiException e) {
      throw new RuntimeException("Erro ao processar webhook: " + e.getMessage());
    }
  }

  private OrderPaymentMethodRequest buildPaymentMethod(InitiatePaymentRequestDto dto) {
    return switch (dto.paymentType()) {
      case "PIX" -> OrderPaymentMethodRequest.builder().id("pix").type("bank_transfer").build();
      case "BOLETO" -> OrderPaymentMethodRequest.builder().id("boleto").type("ticket").build();
      case "CREDIT_CARD" -> OrderPaymentMethodRequest.builder()
          .id(dto.paymentMethodId())
          .type("credit_card")
          .token(dto.cardToken())
          .installments(dto.installments() != null ? dto.installments() : 1)
          .build();
      default -> throw new IllegalArgumentException(
          "Método de pagamento inválido: " + dto.paymentType());
    };
  }

  private OrderPayerRequest buildPayer(Client client, Address address) {
    String[] nameParts = splitName(client.getName());

    OrderPayerAddressRequest payerAddress = null;
    if (address != null) {
      payerAddress =
          OrderPayerAddressRequest.builder()
              .streetName(address.getStreet())
              .streetNumber(address.getNumber())
              .zipCode(address.getZipCode().replaceAll("\\D", ""))
              .neighborhood(address.getDistrict())
              .city(address.getCity().getName())
              .state(address.getCity().getState().getAbbreviation())
              .build();
    }

    return OrderPayerRequest.builder()
        .email(client.getEmail())
        .firstName(nameParts[0])
        .lastName(nameParts[1])
        .identification(buildIdentification(client.getCpf()))
        .address(payerAddress)
        .build();
  }

  private Identification buildIdentification(String cpf) {
    JsonObject json = new JsonObject();
    json.addProperty("type", "CPF");
    json.addProperty("number", cpf != null ? cpf.replaceAll("\\D", "") : "");
    return GSON.fromJson(json, Identification.class);
  }

  private OrderItemRequest buildItemRequest(String title, String unitPrice, int quantity) {
    JsonObject json = new JsonObject();
    json.addProperty("title", title);
    json.addProperty("unitPrice", unitPrice);
    json.addProperty("quantity", quantity);
    return GSON.fromJson(json, OrderItemRequest.class);
  }

  private String[] splitName(String fullName) {
    if (fullName == null || fullName.isBlank()) return new String[] {"N/A", "N/A"};
    int spaceIndex = fullName.indexOf(' ');
    if (spaceIndex == -1) return new String[] {fullName, fullName};
    return new String[] {fullName.substring(0, spaceIndex), fullName.substring(spaceIndex + 1)};
  }

  private boolean isValidSignature(String xSignature, String xRequestId, String dataId) {
    if (webhookSecret == null || webhookSecret.isBlank()) return true;
    if (xSignature == null || xRequestId == null) return false;

    String ts = null;
    String v1 = null;

    for (String part : xSignature.split(",")) {
      if (part.startsWith("ts=")) ts = part.substring(3);
      if (part.startsWith("v1=")) v1 = part.substring(3);
    }

    if (ts == null || v1 == null) return false;

    String template = "id:" + dataId + ";request-id:" + xRequestId + ";ts:" + ts + ";";

    try {
      Mac mac = Mac.getInstance("HmacSHA256");
      mac.init(new SecretKeySpec(webhookSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
      byte[] hash = mac.doFinal(template.getBytes(StandardCharsets.UTF_8));

      StringBuilder hex = new StringBuilder();
      for (byte b : hash) hex.append(String.format("%02x", b));

      return hex.toString().equals(v1);
    } catch (Exception e) {
      return false;
    }
  }
}
