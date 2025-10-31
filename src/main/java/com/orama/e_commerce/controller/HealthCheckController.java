package com.orama.e_commerce.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.HashMap;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Health", description = "Monitoramento da aplicação")
public class HealthCheckController {

  @GetMapping("/api/health")
  @Operation(summary = "Health check")
  @ApiResponse(responseCode = "200", description = "Aplicação saudável")
  public ResponseEntity<Map<String, String>> HealthCheck() {
    Map<String, String> status = new HashMap<>();
    status.put("status", "ok");
    return ResponseEntity.ok(status);
  }
}
