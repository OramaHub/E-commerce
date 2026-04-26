package com.orama.e_commerce.dtos.payment;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;

public record InitiatePaymentRequestDto(
    @NotBlank String paymentType,
    String cardToken,
    Integer installments,
    String paymentMethodId,
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY) String cardNumber,
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY) String securityCode,
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY) String cvv,
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY) String expirationMonth,
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY) String expirationYear) {

  public InitiatePaymentRequestDto(
      String paymentType, String cardToken, Integer installments, String paymentMethodId) {
    this(paymentType, cardToken, installments, paymentMethodId, null, null, null, null, null);
  }

  @AssertTrue(message = "Dados sensiveis do cartao devem ser tokenizados no frontend.")
  public boolean isTokenizedCardPayload() {
    return isBlank(cardNumber)
        && isBlank(securityCode)
        && isBlank(cvv)
        && isBlank(expirationMonth)
        && isBlank(expirationYear);
  }

  @AssertTrue(message = "cardToken e obrigatorio para pagamento com cartao de credito.")
  public boolean isCreditCardTokenPresent() {
    return !"CREDIT_CARD".equals(paymentType) || !isBlank(cardToken);
  }

  private boolean isBlank(String value) {
    return value == null || value.isBlank();
  }
}
