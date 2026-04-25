package com.orama.e_commerce.service;

import com.orama.e_commerce.exceptions.payment.WebhookSignatureException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class WebhookVerifier {

  private static final Logger log = LoggerFactory.getLogger(WebhookVerifier.class);

  @Value("${mercadopago.webhook-secret:}")
  private String webhookSecret;

  @Value("${mercadopago.webhook-ts-tolerance:300}")
  private long webhookTsTolerance;

  public void verify(String xSignature, String xRequestId, String dataId) {
    if (!isValid(xSignature, xRequestId, dataId)) {
      throw new WebhookSignatureException("Assinatura do webhook inválida ou expirada.");
    }
  }

  private boolean isValid(String xSignature, String xRequestId, String dataId) {
    if (webhookSecret == null || webhookSecret.isBlank()) {
      log.warn("Webhook rejeitado: mercadopago.webhook-secret não está configurado.");
      return false;
    }

    if (xSignature == null || xRequestId == null) return false;

    String ts = null;
    String v1 = null;

    for (String part : xSignature.split(",")) {
      if (part.startsWith("ts=")) ts = part.substring(3);
      if (part.startsWith("v1=")) v1 = part.substring(3);
    }

    if (ts == null || v1 == null) return false;

    try {
      long notificationTs = Long.parseLong(ts);
      long nowSeconds = Instant.now().getEpochSecond();
      long notificationTsSeconds =
          notificationTs > 9_999_999_999L ? notificationTs / 1000 : notificationTs;

      if (Math.abs(nowSeconds - notificationTsSeconds) > webhookTsTolerance) {
        log.warn("Webhook rejeitado: timestamp fora da janela de tolerância. ts={}", ts);
        return false;
      }
    } catch (NumberFormatException e) {
      log.warn("Webhook rejeitado: timestamp inválido. ts={}", ts);
      return false;
    }

    String template = "id:" + dataId + ";request-id:" + xRequestId + ";ts:" + ts + ";";

    try {
      Mac mac = Mac.getInstance("HmacSHA256");
      mac.init(new SecretKeySpec(webhookSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
      byte[] hash = mac.doFinal(template.getBytes(StandardCharsets.UTF_8));

      StringBuilder hex = new StringBuilder();
      for (byte b : hash) hex.append(String.format("%02x", b));

      return hex.toString().equals(v1);
    } catch (Exception e) {
      return false;
    }
  }
}
