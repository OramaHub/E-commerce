package com.orama.e_commerce.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.orama.e_commerce.config.RateLimitFilter;
import com.orama.e_commerce.config.RateLimitProperties;
import com.orama.e_commerce.dtos.payment.InitiatePaymentRequestDto;
import com.orama.e_commerce.dtos.payment.InitiatePaymentResponseDto;
import com.orama.e_commerce.dtos.payment.MercadoPagoWebhookDto;
import com.orama.e_commerce.exceptions.handler.GlobalExceptionHandler;
import com.orama.e_commerce.exceptions.payment.OrderOwnershipException;
import com.orama.e_commerce.exceptions.payment.PaymentAlreadyInProgressException;
import com.orama.e_commerce.exceptions.payment.WebhookProcessingException;
import com.orama.e_commerce.exceptions.payment.WebhookSignatureException;
import com.orama.e_commerce.security.JwtAuthenticationFilter;
import com.orama.e_commerce.security.JwtService;
import com.orama.e_commerce.security.SecurityConfig;
import com.orama.e_commerce.service.PaymentApplicationService;
import com.orama.e_commerce.service.TokenRevocationService;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

@WebMvcTest(PaymentController.class)
@Import({
  SecurityConfig.class,
  GlobalExceptionHandler.class,
  RateLimitFilter.class,
  RateLimitProperties.class,
  PaymentControllerTest.SecurityTestConfig.class
})
class PaymentControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockBean private PaymentApplicationService paymentApplicationService;

  @Test
  void shouldRequireAuthenticationWhenInitiatingPayment() throws Exception {
    mockMvc
        .perform(
            post("/api/payments/orders/{orderId}", 10L)
                .contentType("application/json")
                .content("{\"paymentType\":\"PIX\"}"))
        .andExpect(status().isForbidden());

    verify(paymentApplicationService, never())
        .initiatePayment(any(Long.class), any(Long.class), any(InitiatePaymentRequestDto.class));
  }

  @Test
  void shouldInitiatePaymentForAuthenticatedClient() throws Exception {
    InitiatePaymentResponseDto response =
        new InitiatePaymentResponseDto(
            "ORD123", "PAY123", "action_required", "waiting_transfer", "qr", "base64", null, null);
    when(paymentApplicationService.initiatePayment(
            eq(10L), eq(42L), eq(new InitiatePaymentRequestDto("PIX", null, null, null))))
        .thenReturn(response);

    mockMvc
        .perform(
            post("/api/payments/orders/{orderId}", 10L)
                .with(authenticatedClient(42L, "USER"))
                .contentType("application/json")
                .content("{\"paymentType\":\"PIX\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.mpOrderId").value("ORD123"))
        .andExpect(jsonPath("$.paymentId").value("PAY123"))
        .andExpect(jsonPath("$.status").value("action_required"))
        .andExpect(jsonPath("$.statusDetail").value("waiting_transfer"))
        .andExpect(jsonPath("$.qrCode").value("qr"))
        .andExpect(jsonPath("$.qrCodeBase64").value("base64"));

    verify(paymentApplicationService)
        .initiatePayment(
            eq(10L), eq(42L), eq(new InitiatePaymentRequestDto("PIX", null, null, null)));
  }

  @Test
  void shouldRejectRawCardDataBeforeCallingService() throws Exception {
    mockMvc
        .perform(
            post("/api/payments/orders/{orderId}", 10L)
                .with(authenticatedClient(42L, "USER"))
                .contentType("application/json")
                .content(
                    """
                    {
                      "paymentType": "CREDIT_CARD",
                      "cardToken": "card-token",
                      "paymentMethodId": "master",
                      "installments": 1,
                      "cardNumber": "5031755734530604",
                      "securityCode": "123"
                    }
                    """))
        .andExpect(status().isUnprocessableEntity())
        .andExpect(jsonPath("$.status").value(422))
        .andExpect(jsonPath("$.path").value("/api/payments/orders/10"))
        .andExpect(jsonPath("$.validationErrors.tokenizedCardPayload").exists());

    verify(paymentApplicationService, never())
        .initiatePayment(any(Long.class), any(Long.class), any(InitiatePaymentRequestDto.class));
  }

  @Test
  void shouldRejectCreditCardWithoutTokenBeforeCallingService() throws Exception {
    mockMvc
        .perform(
            post("/api/payments/orders/{orderId}", 10L)
                .with(authenticatedClient(42L, "USER"))
                .contentType("application/json")
                .content(
                    """
                    {
                      "paymentType": "CREDIT_CARD",
                      "paymentMethodId": "master",
                      "installments": 1
                    }
                    """))
        .andExpect(status().isUnprocessableEntity())
        .andExpect(jsonPath("$.status").value(422))
        .andExpect(jsonPath("$.validationErrors.creditCardTokenPresent").exists());

    verify(paymentApplicationService, never())
        .initiatePayment(any(Long.class), any(Long.class), any(InitiatePaymentRequestDto.class));
  }

  @Test
  void shouldReturnForbiddenWhenOrderDoesNotBelongToAuthenticatedClient() throws Exception {
    when(paymentApplicationService.initiatePayment(
            eq(10L), eq(42L), eq(new InitiatePaymentRequestDto("PIX", null, null, null))))
        .thenThrow(new OrderOwnershipException("Acesso negado."));

    mockMvc
        .perform(
            post("/api/payments/orders/{orderId}", 10L)
                .with(authenticatedClient(42L, "USER"))
                .contentType("application/json")
                .content("{\"paymentType\":\"PIX\"}"))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.status").value(403))
        .andExpect(jsonPath("$.errorMessage").value("Acesso negado."));
  }

  @Test
  void shouldReturnConflictWhenPaymentIsAlreadyInProgress() throws Exception {
    when(paymentApplicationService.initiatePayment(
            eq(10L), eq(42L), eq(new InitiatePaymentRequestDto("PIX", null, null, null))))
        .thenThrow(new PaymentAlreadyInProgressException("Ja existe um pagamento em andamento."));

    mockMvc
        .perform(
            post("/api/payments/orders/{orderId}", 10L)
                .with(authenticatedClient(42L, "USER"))
                .contentType("application/json")
                .content("{\"paymentType\":\"PIX\"}"))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.status").value(409))
        .andExpect(jsonPath("$.errorMessage").value("Ja existe um pagamento em andamento."));
  }

  @Test
  void shouldSyncPaymentStatusForAuthenticatedClient() throws Exception {
    InitiatePaymentResponseDto response =
        new InitiatePaymentResponseDto(
            "ORD123", "PAY123", "approved", "accredited", null, null, null, null);
    when(paymentApplicationService.syncPaymentStatus(10L, 42L)).thenReturn(response);

    mockMvc
        .perform(
            get("/api/payments/orders/{orderId}/status", 10L)
                .with(authenticatedClient(42L, "USER")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.mpOrderId").value("ORD123"))
        .andExpect(jsonPath("$.status").value("approved"))
        .andExpect(jsonPath("$.statusDetail").value("accredited"));

    verify(paymentApplicationService).syncPaymentStatus(10L, 42L);
  }

  @Test
  void shouldReturnForbiddenWhenSyncingStatusForAnotherClientsOrder() throws Exception {
    when(paymentApplicationService.syncPaymentStatus(10L, 42L))
        .thenThrow(new OrderOwnershipException("Acesso negado."));

    mockMvc
        .perform(
            get("/api/payments/orders/{orderId}/status", 10L)
                .with(authenticatedClient(42L, "USER")))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.status").value(403))
        .andExpect(jsonPath("$.errorMessage").value("Acesso negado."));
  }

  @Test
  void shouldAcceptMercadoPagoWebhookWithoutAuthentication() throws Exception {
    mockMvc
        .perform(
            post("/api/payments/webhook")
                .queryParam("data.id", "ORD123")
                .header("x-signature", "ts=1710000000,v1=signature")
                .header("x-request-id", "request-123")
                .contentType("application/json")
                .content(
                    """
                    {
                      "type": "order",
                      "action": "order.updated",
                      "data": {
                        "id": "ORD123"
                      }
                    }
                    """))
        .andExpect(status().isOk())
        .andExpect(content().string(""));

    verify(paymentApplicationService)
        .handleWebhook(
            eq("ts=1710000000,v1=signature"),
            eq("request-123"),
            eq("ORD123"),
            eq(
                new MercadoPagoWebhookDto(
                    "order", "order.updated", new MercadoPagoWebhookDto.WebhookData("ORD123"))));
  }

  @Test
  void shouldReturnUnauthorizedWhenWebhookSignatureIsInvalid() throws Exception {
    Mockito.doThrow(new WebhookSignatureException("Assinatura invalida."))
        .when(paymentApplicationService)
        .handleWebhook(
            eq("invalid-signature"),
            eq("request-123"),
            eq("ORD123"),
            any(MercadoPagoWebhookDto.class));

    mockMvc
        .perform(
            post("/api/payments/webhook")
                .queryParam("data.id", "ORD123")
                .header("x-signature", "invalid-signature")
                .header("x-request-id", "request-123")
                .contentType("application/json")
                .content(
                    "{\"type\":\"order\",\"action\":\"order.updated\",\"data\":{\"id\":\"ORD123\"}}"))
        .andExpect(status().isUnauthorized())
        .andExpect(content().string(""));
  }

  @Test
  void shouldReturnServerErrorWhenWebhookProcessingFailsTransiently() throws Exception {
    Mockito.doThrow(
            new WebhookProcessingException(
                "Falha ao consultar Mercado Pago.", new RuntimeException("timeout")))
        .when(paymentApplicationService)
        .handleWebhook(
            eq("ts=1710000000,v1=signature"),
            eq("request-123"),
            eq("ORD123"),
            any(MercadoPagoWebhookDto.class));

    mockMvc
        .perform(
            post("/api/payments/webhook")
                .queryParam("data.id", "ORD123")
                .header("x-signature", "ts=1710000000,v1=signature")
                .header("x-request-id", "request-123")
                .contentType("application/json")
                .content(
                    "{\"type\":\"order\",\"action\":\"order.updated\",\"data\":{\"id\":\"ORD123\"}}"))
        .andExpect(status().isInternalServerError())
        .andExpect(content().string(""));
  }

  private static RequestPostProcessor authenticatedClient(Long clientId, String role) {
    Authentication authentication =
        new UsernamePasswordAuthenticationToken(
            "client@test.com", null, List.of(new SimpleGrantedAuthority("ROLE_" + role)));
    ((UsernamePasswordAuthenticationToken) authentication).setDetails(Map.of("id", clientId));
    return authentication(authentication);
  }

  @TestConfiguration(proxyBeanMethods = false)
  static class SecurityTestConfig {

    @Bean
    JwtAuthenticationFilter jwtAuthenticationFilter() {
      return new JwtAuthenticationFilter(
          Mockito.mock(JwtService.class),
          userName -> {
            throw new IllegalStateException("JWT lookup is not used by these MockMvc tests.");
          },
          Mockito.mock(TokenRevocationService.class));
    }

    @Bean
    UserDetailsService userDetailsService() {
      return userName -> {
        throw new IllegalStateException("UserDetailsService is not used by these MockMvc tests.");
      };
    }
  }
}
