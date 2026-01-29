package com.orama.e_commerce.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.HashMap;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Health")
public class HealthCheckController {

  @GetMapping("/api/health")
  @Operation(summary = "Verifica se a API está disponível")
  public ResponseEntity<Map<String, String>> HealthCheck() {
    Map<String, String> status = new HashMap<>();
    status.put("status", "ok");
    return ResponseEntity.ok(status);
  }
}
