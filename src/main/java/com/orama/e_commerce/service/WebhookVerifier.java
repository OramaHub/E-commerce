package com.orama.e_commerce.service;

import com.orama.e_commerce.exceptions.payment.WebhookSignatureException;
import jakarta.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Locale;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class WebhookVerifier {

  private static final Logger log = LoggerFactory.getLogger(WebhookVerifier.class);

  private final String webhookSecret;
  private final long webhookTsTolerance;
  private final boolean webhookSecretRequired;

  public WebhookVerifier(
      @Value("${mercadopago.webhook-secret:}") String webhookSecret,
      @Value("${mercadopago.webhook-ts-tolerance:300}") long webhookTsTolerance,
      @Value("${mercadopago.webhook-secret-required:false}") boolean webhookSecretRequired) {
    this.webhookSecret = webhookSecret;
    this.webhookTsTolerance = webhookTsTolerance;
    this.webhookSecretRequired = webhookSecretRequired;
  }

  @PostConstruct
  void validateConfiguration() {
    if (webhookSecretRequired && isBlank(webhookSecret)) {
      throw new IllegalStateException(
          "MERCADOPAGO_WEBHOOK_SECRET nao esta configurado. "
              + "A aplicacao nao pode receber webhooks em producao sem o segredo.");
    }
  }

  public void verify(String xSignature, String xRequestId, String dataId) {
    if (!isValid(xSignature, xRequestId, dataId)) {
      throw new WebhookSignatureException("Assinatura do webhook invalida ou expirada.");
    }
  }

  private boolean isValid(String xSignature, String xRequestId, String dataId) {
    if (isBlank(webhookSecret)) {
      log.warn(
          "Webhook rejeitado: mercadopago.webhook-secret nao configurado. required={}, xRequestId={}, dataId={}",
          webhookSecretRequired,
          maskIdentifier(xRequestId),
          maskIdentifier(dataId));
      return false;
    }

    if (isBlank(xSignature) || isBlank(xRequestId) || isBlank(dataId)) {
      log.warn(
          "Webhook rejeitado: assinatura, request id ou data id ausente. xRequestId={}, dataId={}",
          maskIdentifier(xRequestId),
          maskIdentifier(dataId));
      return false;
    }

    String ts = null;
    String v1 = null;

    for (String part : xSignature.split(",")) {
      String trimmed = part.trim();
      if (trimmed.startsWith("ts=")) ts = trimmed.substring(3);
      if (trimmed.startsWith("v1=")) v1 = trimmed.substring(3);
    }

    if (ts == null || v1 == null) {
      log.warn(
          "Webhook rejeitado: x-signature sem ts ou v1. xRequestId={}, dataId={}",
          maskIdentifier(xRequestId),
          maskIdentifier(dataId));
      return false;
    }

    try {
      long notificationTs = Long.parseLong(ts);
      long nowSeconds = Instant.now().getEpochSecond();
      long notificationTsSeconds =
          notificationTs > 9_999_999_999L ? notificationTs / 1000 : notificationTs;

      if (Math.abs(nowSeconds - notificationTsSeconds) > webhookTsTolerance) {
        log.warn(
            "Webhook rejeitado: timestamp fora da janela de tolerancia. ts={}, xRequestId={}, dataId={}",
            ts,
            maskIdentifier(xRequestId),
            maskIdentifier(dataId));
        return false;
      }
    } catch (NumberFormatException e) {
      log.warn(
          "Webhook rejeitado: timestamp invalido. ts={}, xRequestId={}, dataId={}",
          ts,
          maskIdentifier(xRequestId),
          maskIdentifier(dataId));
      return false;
    }

    String template =
        "id:" + signatureDataId(dataId) + ";request-id:" + xRequestId + ";ts:" + ts + ";";

    try {
      Mac mac = Mac.getInstance("HmacSHA256");
      mac.init(new SecretKeySpec(webhookSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
      byte[] hash = mac.doFinal(template.getBytes(StandardCharsets.UTF_8));

      StringBuilder hex = new StringBuilder();
      for (byte b : hash) hex.append(String.format("%02x", b));

      boolean matches =
          MessageDigest.isEqual(
              hex.toString().getBytes(StandardCharsets.UTF_8), v1.getBytes(StandardCharsets.UTF_8));
      if (!matches) {
        log.warn(
            "Webhook rejeitado: assinatura invalida. xRequestId={}, dataId={}",
            maskIdentifier(xRequestId),
            maskIdentifier(dataId));
      }
      return matches;
    } catch (Exception e) {
      log.warn(
          "Webhook rejeitado: erro ao validar assinatura. xRequestId={}, dataId={}",
          maskIdentifier(xRequestId),
          maskIdentifier(dataId));
      return false;
    }
  }

  private boolean isBlank(String value) {
    return value == null || value.isBlank();
  }

  private String signatureDataId(String dataId) {
    return isAlphanumeric(dataId) ? dataId.toLowerCase(Locale.ROOT) : dataId;
  }

  private boolean isAlphanumeric(String value) {
    if (value == null || value.isBlank()) return false;
    for (int i = 0; i < value.length(); i++) {
      if (!Character.isLetterOrDigit(value.charAt(i))) return false;
    }
    return true;
  }

  private String maskIdentifier(String value) {
    if (value == null || value.isBlank()) return "missing";
    if (value.length() <= 6) return "***";
    return value.substring(0, 3) + "***" + value.substring(value.length() - 3);
  }
}
