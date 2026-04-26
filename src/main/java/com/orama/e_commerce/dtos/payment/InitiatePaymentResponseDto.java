package com.orama.e_commerce.dtos.payment;

public record InitiatePaymentResponseDto(
    String mpOrderId,
    String paymentId,
    String status,
    String statusDetail,
    String qrCode,
    String qrCodeBase64,
    String ticketUrl,
    String digitableLine,
    String challengeUrl) {

  public InitiatePaymentResponseDto(
      String mpOrderId,
      String paymentId,
      String status,
      String statusDetail,
      String qrCode,
      String qrCodeBase64,
      String ticketUrl,
      String digitableLine) {
    this(
        mpOrderId,
        paymentId,
        status,
        statusDetail,
        qrCode,
        qrCodeBase64,
        ticketUrl,
        digitableLine,
        null);
  }
}
