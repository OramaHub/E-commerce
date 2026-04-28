package com.orama.e_commerce.service.gateway;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record CreatePaymentCommand(
    BigDecimal amount,
    String currency,
    String externalReference,
    String idempotencyKey,
    Payer payer,
    List<Item> items,
    PaymentMethod paymentMethod) {

  public record Payer(
      String email,
      String firstName,
      String lastName,
      String documentType,
      String documentNumber,
      Instant registrationDate,
      Address address) {}

  public record Address(
      String streetName,
      String streetNumber,
      String zipCode,
      String neighborhood,
      String city,
      String state) {}

  public record Item(String title, BigDecimal unitPrice, int quantity) {}

  public sealed interface PaymentMethod permits Pix, Boleto, CreditCard {}

  public record Pix() implements PaymentMethod {}

  public record Boleto() implements PaymentMethod {}

  public record CreditCard(String paymentMethodId, String cardToken, int installments)
      implements PaymentMethod {}
}
