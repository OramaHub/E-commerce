package com.orama.e_commerce.service;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.orama.e_commerce.exceptions.payment.WebhookSignatureException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.junit.jupiter.api.Test;

class WebhookVerifierTest {

  private static final String SECRET = "whsec_test_secret";

  @Test
  void verify_validSignature_doesNotThrow() {
    WebhookVerifier verifier = new WebhookVerifier(SECRET, 300, true);
    String ts = String.valueOf(Instant.now().getEpochSecond());
    String signature = "ts=" + ts + ",v1=" + hmac("MP-ORDER-123", "req-123", ts);

    assertThatNoException().isThrownBy(() -> verifier.verify(signature, "req-123", "MP-ORDER-123"));
  }

  @Test
  void verify_alphanumericDataId_usesLowercaseInManifest() {
    WebhookVerifier verifier = new WebhookVerifier(SECRET, 300, true);
    String ts = String.valueOf(Instant.now().getEpochSecond());
    String signature = "ts=" + ts + ",v1=" + hmac("ord01jq4s4ky8hwq6na5pxb65b3d3", "req-123", ts);

    assertThatNoException()
        .isThrownBy(() -> verifier.verify(signature, "req-123", "ORD01JQ4S4KY8HWQ6NA5PXB65B3D3"));
  }

  @Test
  void verify_alphanumericDataId_rejectsUppercaseManifestSignature() {
    WebhookVerifier verifier = new WebhookVerifier(SECRET, 300, true);
    String ts = String.valueOf(Instant.now().getEpochSecond());
    String signature = "ts=" + ts + ",v1=" + hmac("ORD01JQ4S4KY8HWQ6NA5PXB65B3D3", "req-123", ts);

    assertThatThrownBy(() -> verifier.verify(signature, "req-123", "ORD01JQ4S4KY8HWQ6NA5PXB65B3D3"))
        .isInstanceOf(WebhookSignatureException.class);
  }

  @Test
  void verify_invalidSignature_throwsWebhookSignatureException() {
    WebhookVerifier verifier = new WebhookVerifier(SECRET, 300, true);
    String ts = String.valueOf(Instant.now().getEpochSecond());

    assertThatThrownBy(() -> verifier.verify("ts=" + ts + ",v1=invalid", "req-123", "MP-ORDER-123"))
        .isInstanceOf(WebhookSignatureException.class);
  }

  @Test
  void verify_expiredTimestamp_throwsWebhookSignatureException() {
    WebhookVerifier verifier = new WebhookVerifier(SECRET, 1, true);
    String ts = String.valueOf(Instant.now().minusSeconds(60).getEpochSecond());
    String signature = "ts=" + ts + ",v1=" + hmac("MP-ORDER-123", "req-123", ts);

    assertThatThrownBy(() -> verifier.verify(signature, "req-123", "MP-ORDER-123"))
        .isInstanceOf(WebhookSignatureException.class);
  }

  @Test
  void validateConfiguration_requiredSecretMissing_failsStartup() {
    WebhookVerifier verifier = new WebhookVerifier("", 300, true);

    assertThatThrownBy(verifier::validateConfiguration).isInstanceOf(IllegalStateException.class);
  }

  private String hmac(String dataId, String requestId, String ts) {
    try {
      String template = "id:" + dataId + ";request-id:" + requestId + ";ts:" + ts + ";";
      Mac mac = Mac.getInstance("HmacSHA256");
      mac.init(new SecretKeySpec(SECRET.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
      byte[] hash = mac.doFinal(template.getBytes(StandardCharsets.UTF_8));

      StringBuilder hex = new StringBuilder();
      for (byte b : hash) hex.append(String.format("%02x", b));
      return hex.toString();
    } catch (Exception e) {
      throw new IllegalStateException(e);
    }
  }
}
