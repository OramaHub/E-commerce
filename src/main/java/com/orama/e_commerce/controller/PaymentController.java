package com.orama.e_commerce.controller;

import com.orama.e_commerce.dtos.payment.InitiatePaymentRequestDto;
import com.orama.e_commerce.dtos.payment.InitiatePaymentResponseDto;
import com.orama.e_commerce.dtos.payment.MercadoPagoWebhookDto;
import com.orama.e_commerce.exceptions.payment.WebhookProcessingException;
import com.orama.e_commerce.exceptions.payment.WebhookSignatureException;
import com.orama.e_commerce.service.PaymentApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payments")
@Tag(name = "Pagamentos / Mercado Pago")
public class PaymentController {

  private final PaymentApplicationService paymentApplicationService;

  public PaymentController(PaymentApplicationService paymentApplicationService) {
    this.paymentApplicationService = paymentApplicationService;
  }

  @PostMapping("/orders/{orderId}")
  @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
  @Operation(summary = "Inicia o pagamento de um pedido via Mercado Pago")
  public ResponseEntity<InitiatePaymentResponseDto> initiatePayment(
      @PathVariable Long orderId,
      @Valid @RequestBody InitiatePaymentRequestDto dto,
      Authentication authentication) {
    Long clientId = (Long) ((Map<?, ?>) authentication.getDetails()).get("id");
    return ResponseEntity.ok(paymentApplicationService.initiatePayment(orderId, clientId, dto));
  }

  @PostMapping("/webhook")
  @Operation(summary = "Webhook de notificações do Mercado Pago")
  public ResponseEntity<Void> webhook(
      @RequestHeader(value = "x-signature", required = false) String xSignature,
      @RequestHeader(value = "x-request-id", required = false) String xRequestId,
      @RequestBody MercadoPagoWebhookDto dto) {
    try {
      paymentApplicationService.handleWebhook(xSignature, xRequestId, dto);
      return ResponseEntity.ok().build();
    } catch (WebhookSignatureException e) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    } catch (WebhookProcessingException e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
  }
}
