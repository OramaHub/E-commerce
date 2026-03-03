package com.orama.e_commerce.controller;

import com.orama.e_commerce.dtos.shipping.ShippingCalculateRequestDto;
import com.orama.e_commerce.dtos.shipping.ShippingCalculateResponseDto;
import com.orama.e_commerce.service.ShippingService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/shipping")
public class ShippingController {

  private final ShippingService shippingService;

  public ShippingController(ShippingService shippingService) {
    this.shippingService = shippingService;
  }

  @PostMapping("/calculate")
  public ResponseEntity<ShippingCalculateResponseDto> calculate(
      @Valid @RequestBody ShippingCalculateRequestDto request) {
    ShippingCalculateResponseDto response = shippingService.calculateShipping(request.zipCode());
    return ResponseEntity.ok(response);
  }
}
