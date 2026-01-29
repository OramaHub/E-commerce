package com.orama.e_commerce.service.stripe_service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.orama.e_commerce.dtos.stripe_entities.ProductRequest;
import com.orama.e_commerce.dtos.stripe_entities.StripeResponse;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class StripeServiceTest {

  @InjectMocks private StripeService stripeService;

  private ProductRequest productRequest;

  @BeforeEach
  void setUp() {
    productRequest = new ProductRequest(new BigDecimal("99.90"), 2L, "Produto Teste", "BRL");
  }

  @Test
  void shouldCheckoutProductsSuccessfully() {
    try (MockedStatic<Session> sessionMock = mockStatic(Session.class)) {
      Session mockSession = mock(Session.class);
      when(mockSession.getId()).thenReturn("cs_test_123");
      when(mockSession.getUrl()).thenReturn("https://checkout.stripe.com/pay/cs_test_123");

      sessionMock
          .when(() -> Session.create(any(SessionCreateParams.class)))
          .thenReturn(mockSession);

      StripeResponse result = stripeService.checkoutProducts(productRequest);

      assertNotNull(result);
      assertEquals("SUCCESS", result.status());
      assertEquals("payment session created", result.message());
      assertEquals("cs_test_123", result.sessionId());
      assertEquals("https://checkout.stripe.com/pay/cs_test_123", result.sessionUrl());
    }
  }

  @Test
  void shouldReturnErrorWhenStripeExceptionOccurs() {
    try (MockedStatic<Session> sessionMock = mockStatic(Session.class)) {
      StripeException stripeException = mock(StripeException.class);
      when(stripeException.getMessage()).thenReturn("Invalid API Key");
      when(stripeException.getStatusCode()).thenReturn(401);

      sessionMock
          .when(() -> Session.create(any(SessionCreateParams.class)))
          .thenThrow(stripeException);

      StripeResponse result = stripeService.checkoutProducts(productRequest);

      assertNotNull(result);
      assertEquals("ERROR", result.status());
      assertTrue(result.message().contains("Invalid API Key"));
      assertNull(result.sessionId());
      assertNull(result.sessionUrl());
    }
  }

  @Test
  void shouldUseDefaultCurrencyWhenNotProvided() {
    ProductRequest requestWithoutCurrency =
        new ProductRequest(new BigDecimal("50.00"), 1L, "Produto Sem Moeda", null);

    try (MockedStatic<Session> sessionMock = mockStatic(Session.class)) {
      Session mockSession = mock(Session.class);
      when(mockSession.getId()).thenReturn("cs_test_456");
      when(mockSession.getUrl()).thenReturn("https://checkout.stripe.com/pay/cs_test_456");

      sessionMock
          .when(() -> Session.create(any(SessionCreateParams.class)))
          .thenReturn(mockSession);

      StripeResponse result = stripeService.checkoutProducts(requestWithoutCurrency);

      assertNotNull(result);
      assertEquals("SUCCESS", result.status());
    }
  }

  @Test
  void shouldConvertAmountToCents() {
    ProductRequest request = new ProductRequest(new BigDecimal("100.00"), 1L, "Produto", "BRL");

    try (MockedStatic<Session> sessionMock = mockStatic(Session.class)) {
      Session mockSession = mock(Session.class);
      when(mockSession.getId()).thenReturn("cs_test_789");
      when(mockSession.getUrl()).thenReturn("https://checkout.stripe.com/pay/cs_test_789");

      sessionMock
          .when(() -> Session.create(any(SessionCreateParams.class)))
          .thenAnswer(
              invocation -> {
                return mockSession;
              });

      StripeResponse result = stripeService.checkoutProducts(request);

      assertNotNull(result);
      assertEquals("SUCCESS", result.status());
    }
  }
}
