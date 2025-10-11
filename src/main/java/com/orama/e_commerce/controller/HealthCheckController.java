package com.orama.e_commerce.controller;

import java.util.HashMap;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthCheckController {

  @GetMapping("/api/health")
  public ResponseEntity<Map<String, String>> HealthCheck() {
    Map<String, String> status = new HashMap<>();
    status.put("status", "ok");
    return ResponseEntity.ok(status);
  }
}
