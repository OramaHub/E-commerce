package com.orama.e_commerce.service.gateway;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import com.google.gson.JsonObject;
import com.mercadopago.client.order.OrderClient;
import com.mercadopago.core.MPRequestOptions;
import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.exceptions.MPException;
import com.mercadopago.net.MPResponse;
import com.mercadopago.resources.order.Order;
import com.mercadopago.serialization.Serializer;
import com.orama.e_commerce.config.CustomOrderClient;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class MercadoPagoGatewayTest {

  @Test
  void creditCardPaymentAdds3dsConfigAndMapsChallengeUrl() {
    CapturingOrderClient customOrderClient = new CapturingOrderClient();
    MercadoPagoGateway gateway =
        new MercadoPagoGateway(customOrderClient, mock(OrderClient.class), "MTP BONES", "fashion");

    GatewayPaymentResult result = gateway.createPayment(creditCardCommand());

    JsonObject paymentMethod =
        customOrderClient
            .capturedPayload
            .getAsJsonObject("transactions")
            .getAsJsonArray("payments")
            .get(0)
            .getAsJsonObject()
            .getAsJsonObject("payment_method");
    JsonObject transactionSecurity =
        customOrderClient
            .capturedPayload
            .getAsJsonObject("config")
            .getAsJsonObject("online")
            .getAsJsonObject("transaction_security");
    JsonObject firstItem =
        customOrderClient.capturedPayload.getAsJsonArray("items").get(0).getAsJsonObject();
    JsonObject shipmentAddress =
        customOrderClient.capturedPayload.getAsJsonObject("shipment").getAsJsonObject("address");
    JsonObject additionalInfo =
        customOrderClient.capturedPayload.getAsJsonObject("additional_info");

    assertThat(paymentMethod.get("statement_descriptor").getAsString()).isEqualTo("MTP BONES");
    assertThat(transactionSecurity.get("validation").getAsString()).isEqualTo("on_fraud_risk");
    assertThat(transactionSecurity.get("liability_shift").getAsString()).isEqualTo("required");
    assertThat(firstItem.get("category_id").getAsString()).isEqualTo("fashion");
    assertThat(shipmentAddress.get("zip_code").getAsString()).isEqualTo("01310100");
    assertThat(shipmentAddress.get("city").getAsString()).isEqualTo("Sao Paulo");
    assertThat(shipmentAddress.get("state").getAsString()).isEqualTo("SP");
    assertThat(additionalInfo.get("payer.registration_date").getAsString())
        .isEqualTo("2026-04-28T10:00:00Z");
    assertThat(result.status()).isEqualTo("action_required");
    assertThat(result.statusDetail()).isEqualTo("pending_challenge");
    assertThat(result.challengeUrl()).isEqualTo("https://www.mercadopago.com.br/challenge");
  }

  @Test
  void nonCardPaymentDoesNotSendStatementDescriptor() {
    CapturingOrderClient customOrderClient = new CapturingOrderClient();
    MercadoPagoGateway gateway =
        new MercadoPagoGateway(customOrderClient, mock(OrderClient.class), "MTP BONES", "fashion");

    gateway.createPayment(pixCommand());

    JsonObject paymentMethod =
        customOrderClient
            .capturedPayload
            .getAsJsonObject("transactions")
            .getAsJsonArray("payments")
            .get(0)
            .getAsJsonObject()
            .getAsJsonObject("payment_method");

    assertThat(paymentMethod.has("statement_descriptor")).isFalse();
    assertThat(paymentMethod.has("installments")).isFalse();
  }

  private CreatePaymentCommand creditCardCommand() {
    return new CreatePaymentCommand(
        new BigDecimal("150.00"),
        "BRL",
        "ORD-3DS",
        "order-1-attempt-1",
        new CreatePaymentCommand.Payer(
            "buyer@example.com",
            "Buyer",
            "Test",
            "CPF",
            "12345678909",
            Instant.parse("2026-04-28T10:00:00Z"),
            address()),
        List.of(new CreatePaymentCommand.Item("Produto", new BigDecimal("150.00"), 1)),
        new CreatePaymentCommand.CreditCard("master", "card_token", 1));
  }

  private CreatePaymentCommand pixCommand() {
    return new CreatePaymentCommand(
        new BigDecimal("150.00"),
        "BRL",
        "ORD-PIX",
        "order-1-attempt-2",
        new CreatePaymentCommand.Payer(
            "buyer@example.com",
            "Buyer",
            "Test",
            "CPF",
            "12345678909",
            Instant.parse("2026-04-28T10:00:00Z"),
            address()),
        List.of(new CreatePaymentCommand.Item("Produto", new BigDecimal("150.00"), 1)),
        new CreatePaymentCommand.Pix());
  }

  private CreatePaymentCommand.Address address() {
    return new CreatePaymentCommand.Address(
        "Rua Teste", "100", "01310100", "Centro", "Sao Paulo", "SP");
  }

  private static class CapturingOrderClient extends CustomOrderClient {
    private JsonObject capturedPayload;

    @Override
    public Order createOrder(JsonObject payload, MPRequestOptions options)
        throws MPException, MPApiException {
      this.capturedPayload = payload;
      String responseJson =
          """
          {
            "id": "ORD-3DS",
            "transactions": {
              "payments": [
                {
                  "id": "PAY-3DS",
                  "status": "action_required",
                  "status_detail": "pending_challenge",
                  "payment_method": {
                    "id": "master",
                    "transaction_security": {
                      "url": "https://www.mercadopago.com.br/challenge"
                    }
                  }
                }
              ]
            }
          }
          """;

      Order order = Serializer.deserializeFromJson(Order.class, responseJson);
      order.setResponse(new MPResponse(201, Map.of(), responseJson));
      return order;
    }
  }
}
