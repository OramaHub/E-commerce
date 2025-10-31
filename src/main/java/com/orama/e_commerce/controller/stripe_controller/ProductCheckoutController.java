package com.orama.e_commerce.controller.stripe_controller;

import com.orama.e_commerce.dtos.stripe_entities.ProductRequest;
import com.orama.e_commerce.dtos.stripe_entities.StripeResponse;
import com.orama.e_commerce.service.stripe_service.StripeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/product")
@Tag(name = "Checkout", description = "Integração com Stripe para pagamento de produtos")
public class ProductCheckoutController {

  private final StripeService stripeService;

  public ProductCheckoutController(StripeService stripeService) {
    this.stripeService = stripeService;
  }

  @PostMapping("/checkout")
  @Operation(summary = "Criar sessão de checkout")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Sessão de checkout criada"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos")
      })
  public ResponseEntity<StripeResponse> checkoutProducts(
      @RequestBody ProductRequest productRequest) {
    StripeResponse stripeResponse = stripeService.checkoutProducts(productRequest);
    return ResponseEntity.status(HttpStatus.OK).body(stripeResponse);
  }
}
