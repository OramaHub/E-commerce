package com.orama.e_commerce.service.gateway;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
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
import com.orama.e_commerce.exceptions.payment.PaymentGatewayException;
import com.orama.e_commerce.exceptions.payment.PermanentPaymentGatewayException;
import com.orama.e_commerce.exceptions.payment.TransientPaymentGatewayException;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class MercadoPagoGateway implements PaymentGateway {

  private static final Logger log = LoggerFactory.getLogger(MercadoPagoGateway.class);
  private static final Gson GSON = new Gson();
  private static final String DEFAULT_STATEMENT_DESCRIPTOR = "MTP BONES";
  private static final String DEFAULT_ITEM_CATEGORY_ID = "fashion";
  private static final int STATEMENT_DESCRIPTOR_MAX_LENGTH = 13;

  private final CustomOrderClient customOrderClient;
  private final OrderClient orderClient;
  private final String statementDescriptor;
  private final String itemCategoryId;

  public MercadoPagoGateway(
      CustomOrderClient customOrderClient,
      OrderClient orderClient,
      @Value("${mercadopago.statement-descriptor:MTP BONES}") String statementDescriptor,
      @Value("${mercadopago.item-category-id:fashion}") String itemCategoryId) {
    this.customOrderClient = customOrderClient;
    this.orderClient = orderClient;
    this.statementDescriptor = normalizeStatementDescriptor(statementDescriptor);
    this.itemCategoryId = normalizeConfiguredValue(itemCategoryId, DEFAULT_ITEM_CATEGORY_ID);
  }

  @Override
  public GatewayPaymentResult createPayment(CreatePaymentCommand command) {
    try {
      OrderCreateRequest request = buildOrderRequest(command);
      JsonObject payload = Serializer.serializeToJson(request);
      applyInstallmentsWorkaround(payload, command);
      applyIntegrationQualityData(payload, command);
      applyThreeDsConfig(payload, command);

      MPRequestOptions options =
          MPRequestOptions.builder()
              .customHeaders(Map.of("X-Idempotency-Key", command.idempotencyKey()))
              .build();

      Order mpOrder = customOrderClient.createOrder(payload, options);
      GatewayPaymentResult result = mapPaymentResponse(mpOrder);
      log.info(
          "MP order created: orderId={}, paymentId={}, status={}, statusDetail={}, challengeUrlPresent={}",
          result.providerOrderId(),
          result.providerPaymentId(),
          result.status(),
          result.statusDetail(),
          result.challengeUrl() != null);
      return result;

    } catch (MPApiException e) {
      throw classifyMpApiException(e, "criar order de pagamento");
    } catch (MPException e) {
      log.error("MP SDK error ao criar order: {}", e.getMessage());
      throw new PermanentPaymentGatewayException(
          "Erro de SDK ao criar order de pagamento: " + e.getMessage(), e);
    }
  }

  @Override
  public GatewayOrderResult getOrderStatus(String providerOrderId) {
    try {
      Order mpOrder = orderClient.get(providerOrderId);
      return mapOrderResponse(mpOrder);
    } catch (MPApiException e) {
      throw classifyMpApiException(e, "consultar order");
    } catch (MPException e) {
      log.error("MP SDK error ao consultar order: {}", e.getMessage());
      throw new PermanentPaymentGatewayException(
          "Erro de SDK ao consultar order: " + e.getMessage(), e);
    }
  }

  @Override
  public GatewayOrderResult cancelOrder(String providerOrderId, String idempotencyKey) {
    try {
      Order mpOrder = orderClient.cancel(providerOrderId, buildIdempotencyOptions(idempotencyKey));
      return mapOrderResponse(mpOrder);
    } catch (MPApiException e) {
      throw classifyMpApiException(e, "cancelar order");
    } catch (MPException e) {
      log.error("MP SDK error ao cancelar order: {}", e.getMessage());
      throw new PermanentPaymentGatewayException(
          "Erro de SDK ao cancelar order: " + e.getMessage(), e);
    }
  }

  @Override
  public GatewayOrderResult refundOrder(String providerOrderId, String idempotencyKey) {
    try {
      Order mpOrder = orderClient.refund(providerOrderId, buildIdempotencyOptions(idempotencyKey));
      return mapOrderResponse(mpOrder);
    } catch (MPApiException e) {
      throw classifyMpApiException(e, "reembolsar order");
    } catch (MPException e) {
      log.error("MP SDK error ao reembolsar order: {}", e.getMessage());
      throw new PermanentPaymentGatewayException(
          "Erro de SDK ao reembolsar order: " + e.getMessage(), e);
    }
  }

  private MPRequestOptions buildIdempotencyOptions(String idempotencyKey) {
    return MPRequestOptions.builder()
        .customHeaders(Map.of("X-Idempotency-Key", idempotencyKey))
        .build();
  }

  private OrderCreateRequest buildOrderRequest(CreatePaymentCommand command) {
    List<OrderItemRequest> items =
        command.items().stream()
            .map(
                item ->
                    buildItemRequest(
                        item.title(), item.unitPrice().toPlainString(), item.quantity()))
            .toList();

    return OrderCreateRequest.builder()
        .type("online")
        .processingMode("automatic")
        .captureMode("automatic_async")
        .totalAmount(command.amount().toPlainString())
        .externalReference(command.externalReference())
        .payer(buildPayer(command.payer()))
        .items(items)
        .transactions(
            OrderTransactionRequest.builder()
                .payments(
                    List.of(
                        OrderPaymentRequest.builder()
                            .amount(command.amount().toPlainString())
                            .paymentMethod(buildPaymentMethod(command.paymentMethod()))
                            .build()))
                .build())
        .build();
  }

  private void applyInstallmentsWorkaround(JsonObject payload, CreatePaymentCommand command) {
    JsonObject paymentMethod = getPaymentMethodPayload(payload);

    if (command.paymentMethod() instanceof CreatePaymentCommand.CreditCard cc) {
      paymentMethod.addProperty("installments", cc.installments());
    } else {
      paymentMethod.remove("installments");
    }
  }

  private void applyIntegrationQualityData(JsonObject payload, CreatePaymentCommand command) {
    applyStatementDescriptor(payload, command.paymentMethod());
    applyItemCategory(payload);
    applyShipment(payload, command.payer().address());
    applyPayerRegistrationDate(payload, command.payer().registrationDate());
  }

  private void applyStatementDescriptor(
      JsonObject payload, CreatePaymentCommand.PaymentMethod paymentMethodCommand) {
    JsonObject paymentMethod = getPaymentMethodPayload(payload);
    if (paymentMethodCommand instanceof CreatePaymentCommand.CreditCard) {
      paymentMethod.addProperty("statement_descriptor", statementDescriptor);
      return;
    }
    paymentMethod.remove("statement_descriptor");
  }

  private void applyItemCategory(JsonObject payload) {
    JsonArray items = getArray(payload, "items");
    if (items == null) {
      return;
    }

    for (JsonElement element : items) {
      if (element.isJsonObject()) {
        element.getAsJsonObject().addProperty("category_id", itemCategoryId);
      }
    }
  }

  private void applyShipment(JsonObject payload, CreatePaymentCommand.Address address) {
    if (address == null || !hasAnyAddressData(address)) {
      return;
    }

    JsonObject shipment = getOrCreateObject(payload, "shipment");
    JsonObject shipmentAddress = getOrCreateObject(shipment, "address");
    addStringIfPresent(shipmentAddress, "zip_code", address.zipCode());
    addStringIfPresent(shipmentAddress, "street_name", address.streetName());
    addStringIfPresent(shipmentAddress, "street_number", address.streetNumber());
    addStringIfPresent(shipmentAddress, "neighborhood", address.neighborhood());
    addStringIfPresent(shipmentAddress, "city", address.city());
    addStringIfPresent(shipmentAddress, "state", address.state());
  }

  private void applyPayerRegistrationDate(JsonObject payload, Instant registrationDate) {
    if (registrationDate == null) {
      return;
    }
    JsonObject additionalInfo = getOrCreateObject(payload, "additional_info");
    additionalInfo.addProperty("payer.registration_date", registrationDate.toString());
  }

  private JsonObject getPaymentMethodPayload(JsonObject payload) {
    return payload
        .getAsJsonObject("transactions")
        .getAsJsonArray("payments")
        .get(0)
        .getAsJsonObject()
        .getAsJsonObject("payment_method");
  }

  private boolean hasAnyAddressData(CreatePaymentCommand.Address address) {
    return hasText(address.zipCode())
        || hasText(address.streetName())
        || hasText(address.streetNumber())
        || hasText(address.neighborhood())
        || hasText(address.city())
        || hasText(address.state());
  }

  private void addStringIfPresent(JsonObject target, String key, String value) {
    if (hasText(value)) {
      target.addProperty(key, value);
    }
  }

  private boolean hasText(String value) {
    return value != null && !value.isBlank();
  }

  private String normalizeStatementDescriptor(String value) {
    String normalized =
        normalizeConfiguredValue(value, DEFAULT_STATEMENT_DESCRIPTOR)
            .toUpperCase(Locale.ROOT)
            .replaceAll("[^A-Z0-9 ]", " ")
            .replaceAll("\\s+", " ")
            .trim();

    if (normalized.isBlank()) {
      normalized = DEFAULT_STATEMENT_DESCRIPTOR;
    }
    if (normalized.length() <= STATEMENT_DESCRIPTOR_MAX_LENGTH) {
      return normalized;
    }
    return normalized.substring(0, STATEMENT_DESCRIPTOR_MAX_LENGTH).trim();
  }

  private String normalizeConfiguredValue(String value, String fallback) {
    if (value == null || value.isBlank()) {
      return fallback;
    }
    return value.trim();
  }

  private GatewayPaymentResult mapPaymentResponse(Order mpOrder) {
    OrderPayment payment = mpOrder.getTransactions().getPayments().get(0);
    OrderPaymentMethod method = payment.getPaymentMethod();

    return new GatewayPaymentResult(
        mpOrder.getId(),
        payment.getId(),
        payment.getStatus(),
        payment.getStatusDetail(),
        method != null ? method.getId() : null,
        method != null ? method.getQrCode() : null,
        method != null ? method.getQrCodeBase64() : null,
        method != null ? method.getTicketUrl() : null,
        method != null ? method.getDigitableLine() : null,
        extractChallengeUrl(mpOrder));
  }

  private GatewayOrderResult mapOrderResponse(Order mpOrder) {
    String status = mpOrder.getStatus();
    String statusDetail = mpOrder.getStatusDetail();
    String paymentMethodId = null;

    if (mpOrder.getTransactions() != null
        && mpOrder.getTransactions().getPayments() != null
        && !mpOrder.getTransactions().getPayments().isEmpty()) {
      OrderPayment payment = mpOrder.getTransactions().getPayments().get(0);
      status = payment.getStatus();
      statusDetail = payment.getStatusDetail();
      if (payment.getPaymentMethod() != null) {
        paymentMethodId = payment.getPaymentMethod().getId();
      }
    }

    return new GatewayOrderResult(mpOrder.getId(), status, statusDetail, paymentMethodId);
  }

  private PaymentGatewayException classifyMpApiException(MPApiException e, String operation) {
    int statusCode = e.getApiResponse().getStatusCode();

    log.error(
        "MP API error em '{}' - status: {}. Body omitido por segurança.", operation, statusCode);

    String message =
        "Erro do gateway de pagamento ao " + operation + " (status " + statusCode + ")";

    if (statusCode == 408 || statusCode == 429 || statusCode >= 500) {
      return new TransientPaymentGatewayException(message, e);
    }
    return new PermanentPaymentGatewayException(message, e);
  }

  private void applyThreeDsConfig(JsonObject payload, CreatePaymentCommand command) {
    if (!(command.paymentMethod() instanceof CreatePaymentCommand.CreditCard)) {
      return;
    }

    JsonObject config = getOrCreateObject(payload, "config");
    JsonObject online = getOrCreateObject(config, "online");
    JsonObject transactionSecurity = new JsonObject();
    transactionSecurity.addProperty("validation", "on_fraud_risk");
    transactionSecurity.addProperty("liability_shift", "required");
    online.add("transaction_security", transactionSecurity);
  }

  private JsonObject getOrCreateObject(JsonObject parent, String key) {
    JsonElement current = parent.get(key);
    if (current != null && current.isJsonObject()) {
      return current.getAsJsonObject();
    }
    JsonObject created = new JsonObject();
    parent.add(key, created);
    return created;
  }

  private String extractChallengeUrl(Order mpOrder) {
    if (mpOrder.getResponse() == null || mpOrder.getResponse().getContent() == null) {
      return null;
    }

    try {
      JsonObject root = GSON.fromJson(mpOrder.getResponse().getContent(), JsonObject.class);
      JsonObject transactions = getObject(root, "transactions");
      JsonArray payments = getArray(transactions, "payments");
      if (payments == null || payments.size() == 0 || !payments.get(0).isJsonObject()) {
        return null;
      }

      JsonObject payment = payments.get(0).getAsJsonObject();
      JsonObject paymentMethod = getObject(payment, "payment_method");
      JsonObject transactionSecurity = getObject(paymentMethod, "transaction_security");
      return getString(transactionSecurity, "url");
    } catch (RuntimeException e) {
      log.debug("Nao foi possivel extrair challengeUrl da resposta do MP: {}", e.getMessage());
      return null;
    }
  }

  private JsonObject getObject(JsonObject parent, String key) {
    if (parent == null) {
      return null;
    }
    JsonElement element = parent.get(key);
    return element != null && element.isJsonObject() ? element.getAsJsonObject() : null;
  }

  private JsonArray getArray(JsonObject parent, String key) {
    if (parent == null) {
      return null;
    }
    JsonElement element = parent.get(key);
    return element != null && element.isJsonArray() ? element.getAsJsonArray() : null;
  }

  private String getString(JsonObject parent, String key) {
    if (parent == null) {
      return null;
    }
    JsonElement element = parent.get(key);
    if (element == null || !element.isJsonPrimitive()) {
      return null;
    }
    String value = element.getAsString();
    return value != null && !value.isBlank() ? value : null;
  }

  private OrderPaymentMethodRequest buildPaymentMethod(CreatePaymentCommand.PaymentMethod method) {
    return switch (method) {
      case CreatePaymentCommand.Pix p -> OrderPaymentMethodRequest.builder()
          .id("pix")
          .type("bank_transfer")
          .build();
      case CreatePaymentCommand.Boleto b -> OrderPaymentMethodRequest.builder()
          .id("boleto")
          .type("ticket")
          .build();
      case CreatePaymentCommand.CreditCard cc -> OrderPaymentMethodRequest.builder()
          .id(cc.paymentMethodId())
          .type("credit_card")
          .token(cc.cardToken())
          .installments(cc.installments())
          .build();
    };
  }

  private OrderPayerRequest buildPayer(CreatePaymentCommand.Payer payer) {
    OrderPayerAddressRequest payerAddress = null;
    if (payer.address() != null) {
      CreatePaymentCommand.Address addr = payer.address();
      payerAddress =
          OrderPayerAddressRequest.builder()
              .streetName(addr.streetName())
              .streetNumber(addr.streetNumber())
              .zipCode(addr.zipCode())
              .neighborhood(addr.neighborhood())
              .city(addr.city())
              .state(addr.state())
              .build();
    }

    return OrderPayerRequest.builder()
        .email(payer.email())
        .firstName(payer.firstName())
        .lastName(payer.lastName())
        .identification(buildIdentification(payer.documentType(), payer.documentNumber()))
        .address(payerAddress)
        .build();
  }

  private Identification buildIdentification(String type, String number) {
    JsonObject json = new JsonObject();
    json.addProperty("type", type);
    json.addProperty("number", number != null ? number : "");
    return GSON.fromJson(json, Identification.class);
  }

  private OrderItemRequest buildItemRequest(String title, String unitPrice, int quantity) {
    JsonObject json = new JsonObject();
    json.addProperty("title", title);
    json.addProperty("unitPrice", unitPrice);
    json.addProperty("quantity", quantity);
    return GSON.fromJson(json, OrderItemRequest.class);
  }
}
