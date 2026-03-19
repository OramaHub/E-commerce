package com.orama.e_commerce.dtos.payment;

public record MercadoPagoWebhookDto(String type, String action, WebhookData data) {
  public record WebhookData(String id) {}
}
