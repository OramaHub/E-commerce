package com.orama.e_commerce.config;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class MercadoPagoConfigurationTest {

  @Test
  void shouldFailFastWhenAccessTokenIsBlank() {
    MercadoPagoConfiguration configuration = new MercadoPagoConfiguration();
    ReflectionTestUtils.setField(configuration, "accessToken", " ");
    ReflectionTestUtils.setField(configuration, "webhookSecret", "webhook-secret");
    ReflectionTestUtils.setField(configuration, "webhookSecretRequired", true);

    assertThatThrownBy(configuration::init)
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("MERCADOPAGO_ACCESS_TOKEN");
  }

  @Test
  void shouldFailFastWhenWebhookSecretIsRequiredAndBlank() {
    MercadoPagoConfiguration configuration = new MercadoPagoConfiguration();
    ReflectionTestUtils.setField(configuration, "accessToken", "TEST-access-token");
    ReflectionTestUtils.setField(configuration, "webhookSecret", "");
    ReflectionTestUtils.setField(configuration, "webhookSecretRequired", true);

    assertThatThrownBy(configuration::init)
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("MERCADOPAGO_WEBHOOK_SECRET");
  }
}
