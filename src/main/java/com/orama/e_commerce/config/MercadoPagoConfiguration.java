package com.orama.e_commerce.config;

import com.mercadopago.MercadoPagoConfig;
import com.mercadopago.client.order.OrderClient;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MercadoPagoConfiguration {

  private static final Logger log = LoggerFactory.getLogger(MercadoPagoConfiguration.class);

  @Value("${mercadopago.access-token}")
  private String accessToken;

  @Value("${mercadopago.webhook-secret:}")
  private String webhookSecret;

  @Value("${mercadopago.webhook-secret-required:false}")
  private boolean webhookSecretRequired;

  @PostConstruct
  public void init() {
    MercadoPagoConfig.setAccessToken(accessToken);

    if (webhookSecretRequired && (webhookSecret == null || webhookSecret.isBlank())) {
      throw new IllegalStateException(
          "MERCADOPAGO_WEBHOOK_SECRET não está configurado. "
              + "A aplicação não pode ser iniciada em produção sem o segredo do webhook.");
    }

    if (webhookSecret == null || webhookSecret.isBlank()) {
      log.warn(
          "ATENÇÃO: mercadopago.webhook-secret não configurado. "
              + "Todas as requisições ao endpoint de webhook serão rejeitadas. "
              + "Configure MERCADOPAGO_WEBHOOK_SECRET para habilitar o recebimento de notificações.");
    }
  }

  @Bean
  public CustomOrderClient customOrderClient() {
    return new CustomOrderClient();
  }

  @Bean
  public OrderClient orderClient() {
    return new OrderClient();
  }
}
