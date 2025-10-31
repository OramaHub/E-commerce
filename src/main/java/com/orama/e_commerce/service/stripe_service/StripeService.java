package com.orama.e_commerce.service.stripe_service;

import com.orama.e_commerce.dtos.stripe_entities.ProductRequest;
import com.orama.e_commerce.dtos.stripe_entities.StripeResponse;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import org.springframework.stereotype.Service;

@Service
public class StripeService {

  public StripeResponse checkoutProducts(ProductRequest productRequest) {

    // Log para debug
    System.out.println("🔵 Iniciando criação de sessão Stripe...");
    System.out.println("🔑 API Key configurada: " + (Stripe.apiKey != null ? "SIM" : "NÃO"));
    System.out.println("📦 Produto: " + productRequest.name());
    System.out.println("💰 Valor: " + productRequest.amount());
    System.out.println("📊 Quantidade: " + productRequest.quantity());
    System.out.println("💵 Moeda: " + productRequest.currency());

    SessionCreateParams.LineItem.PriceData.ProductData productData =
        SessionCreateParams.LineItem.PriceData.ProductData.builder()
            .setName(productRequest.name())
            .build();

    SessionCreateParams.LineItem.PriceData priceData =
        SessionCreateParams.LineItem.PriceData.builder()
            .setCurrency(productRequest.currency() != null ? productRequest.currency() : "USD")
            .setUnitAmount(productRequest.amount())
            .setProductData(productData)
            .build();

    SessionCreateParams.LineItem lineItem =
        SessionCreateParams.LineItem.builder()
            .setQuantity(productRequest.quantity())
            .setPriceData(priceData)
            .build();

    SessionCreateParams params =
        SessionCreateParams.builder()
            .setMode(SessionCreateParams.Mode.PAYMENT)
            .setSuccessUrl("http://localhost:8080/success")
            .setCancelUrl("http://localhost:8080/cancel")
            .addLineItem(lineItem)
            .build();

    try {
      System.out.println("🚀 Chamando API do Stripe...");
      Session session = Session.create(params);
      System.out.println("✅ Sessão criada com sucesso! ID: " + session.getId());

      return StripeResponse.builder()
          .status("SUCCESS")
          .message("payment session created")
          .sessionId(session.getId())
          .sessionUrl(session.getUrl())
          .build();

    } catch (StripeException e) {
      System.err.println("❌ ERRO STRIPE: " + e.getMessage());
      System.err.println("❌ Tipo do erro: " + e.getClass().getSimpleName());
      System.err.println("❌ Status Code: " + e.getStatusCode());
      e.printStackTrace();

      return StripeResponse.builder()
          .status("ERROR")
          .message("Erro ao criar sessão de pagamento: " + e.getMessage())
          .sessionId(null)
          .sessionUrl(null)
          .build();
    }
  }
}
