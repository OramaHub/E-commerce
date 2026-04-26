package com.orama.e_commerce.dtos.payment;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class InitiatePaymentRequestDtoTest {

  private Validator validator;

  @BeforeEach
  void setUp() {
    validator = Validation.buildDefaultValidatorFactory().getValidator();
  }

  @Test
  void creditCardRequiresCardToken() {
    InitiatePaymentRequestDto dto = new InitiatePaymentRequestDto("CREDIT_CARD", null, 1, "visa");

    assertThat(validator.validate(dto))
        .anyMatch(
            violation ->
                violation
                    .getMessage()
                    .equals("cardToken e obrigatorio para pagamento com cartao de credito."));
  }

  @Test
  void rawCardDataIsRejected() {
    InitiatePaymentRequestDto dto =
        new InitiatePaymentRequestDto(
            "CREDIT_CARD", "tok_123", 1, "visa", "4111111111111111", null, "123", "12", "2030");

    assertThat(validator.validate(dto))
        .anyMatch(
            violation ->
                violation
                    .getMessage()
                    .equals("Dados sensiveis do cartao devem ser tokenizados no frontend."));
  }
}
