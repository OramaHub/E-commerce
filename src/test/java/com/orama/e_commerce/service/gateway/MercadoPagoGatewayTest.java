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
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class MercadoPagoGatewayTest {

  @Test
  void creditCardPaymentAdds3dsConfigAndMapsChallengeUrl() {
    CapturingOrderClient customOrderClient = new CapturingOrderClient();
    MercadoPagoGateway gateway = new MercadoPagoGateway(customOrderClient, mock(OrderClient.class));

    GatewayPaymentResult result = gateway.createPayment(creditCardCommand());

    JsonObject transactionSecurity =
        customOrderClient
            .capturedPayload
            .getAsJsonObject("config")
            .getAsJsonObject("online")
            .getAsJsonObject("transaction_security");

    assertThat(transactionSecurity.get("validation").getAsString()).isEqualTo("on_fraud_risk");
    assertThat(transactionSecurity.get("liability_shift").getAsString()).isEqualTo("required");
    assertThat(result.status()).isEqualTo("action_required");
    assertThat(result.statusDetail()).isEqualTo("pending_challenge");
    assertThat(result.challengeUrl()).isEqualTo("https://www.mercadopago.com.br/challenge");
  }

  private CreatePaymentCommand creditCardCommand() {
    return new CreatePaymentCommand(
        new BigDecimal("150.00"),
        "BRL",
        "ORD-3DS",
        "order-1-attempt-1",
        new CreatePaymentCommand.Payer(
            "buyer@example.com", "Buyer", "Test", "CPF", "12345678909", null),
        List.of(new CreatePaymentCommand.Item("Produto", new BigDecimal("150.00"), 1)),
        new CreatePaymentCommand.CreditCard("master", "card_token", 1));
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
