package com.orama.e_commerce.controller;

import com.orama.e_commerce.dtos.location.CepLookupResponseDto;
import com.orama.e_commerce.service.LocationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/locations")
@Tag(name = "Localizacoes")
public class LocationController {

  private final LocationService locationService;

  public LocationController(LocationService locationService) {
    this.locationService = locationService;
  }

  @GetMapping("/cep/{cep}")
  @Operation(summary = "Busca endereco pelo CEP e retorna cidade/UF textuais")
  public ResponseEntity<CepLookupResponseDto> lookupCep(@PathVariable String cep) {
    return ResponseEntity.ok(locationService.lookupCep(cep));
  }
}
