package com.orama.e_commerce.controller;

import com.orama.e_commerce.dtos.location.CityResponseDto;
import com.orama.e_commerce.dtos.location.CitySimpleDto;
import com.orama.e_commerce.dtos.location.CountryResponseDto;
import com.orama.e_commerce.dtos.location.StateResponseDto;
import com.orama.e_commerce.service.CityImportService;
import com.orama.e_commerce.service.LocationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/locations")
@Tag(name = "Localidades", description = "Consulta de países, estados e cidades")
public class LocationController {

  private final LocationService locationService;
  private final CityImportService cityImportService;

  public LocationController(LocationService locationService, CityImportService cityImportService) {
    this.locationService = locationService;
    this.cityImportService = cityImportService;
  }

  @GetMapping("/countries")
  @Operation(summary = "Listar países")
  @ApiResponses(@ApiResponse(responseCode = "200", description = "Lista de países retornada"))
  public ResponseEntity<List<CountryResponseDto>> getAllCountries() {
    List<CountryResponseDto> countries = locationService.getAllCountries();
    return ResponseEntity.ok(countries);
  }

  @GetMapping("/countries/{id}")
  @Operation(summary = "Buscar país por ID")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "País encontrado"),
        @ApiResponse(responseCode = "404", description = "País não encontrado")
      })
  public ResponseEntity<CountryResponseDto> getCountryById(@PathVariable Long id) {
    CountryResponseDto country = locationService.getCountryById(id);
    return ResponseEntity.ok(country);
  }

  @GetMapping("/states")
  @Operation(summary = "Listar estados", description = "Permite filtrar por ID do país")
  @ApiResponses(@ApiResponse(responseCode = "200", description = "Lista de estados retornada"))
  public ResponseEntity<List<StateResponseDto>> getAllStates(
      @RequestParam(required = false) Long countryId) {
    List<StateResponseDto> states;
    if (countryId != null) {
      states = locationService.getStatesByCountry(countryId);
    } else {
      states = locationService.getAllStates();
    }
    return ResponseEntity.ok(states);
  }

  @GetMapping("/states/{id}")
  @Operation(summary = "Buscar estado por ID")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Estado encontrado"),
        @ApiResponse(responseCode = "404", description = "Estado não encontrado")
      })
  public ResponseEntity<StateResponseDto> getStateById(@PathVariable Long id) {
    StateResponseDto state = locationService.getStateById(id);
    return ResponseEntity.ok(state);
  }

  @GetMapping("/cities")
  @Operation(
      summary = "Listar cidades",
      description = "Filtra por estado ou pesquisa pelo nome quando informado")
  @ApiResponses(@ApiResponse(responseCode = "200", description = "Lista de cidades retornada"))
  public ResponseEntity<List<CitySimpleDto>> getCities(
      @RequestParam(required = false) Long stateId, @RequestParam(required = false) String name) {
    List<CitySimpleDto> cities;

    if (name != null && !name.isBlank()) {
      cities = locationService.searchCities(name);
    } else if (stateId != null) {
      cities = locationService.getCitiesByState(stateId);
    } else {
      cities = locationService.getAllCities();
    }

    return ResponseEntity.ok(cities);
  }

  @GetMapping("/cities/{id}")
  @Operation(summary = "Buscar cidade por ID")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Cidade encontrada"),
        @ApiResponse(responseCode = "404", description = "Cidade não encontrada")
      })
  public ResponseEntity<CityResponseDto> getCityById(@PathVariable Long id) {
    CityResponseDto city = locationService.getCityById(id);
    return ResponseEntity.ok(city);
  }

  @PreAuthorize("hasRole('ADMIN')")
  @PostMapping("/cities/import")
  @Operation(summary = "Importar cidades do CSV", description = "Disponível apenas para ADMIN")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Importação concluída"),
        @ApiResponse(responseCode = "400", description = "Banco já populado"),
        @ApiResponse(responseCode = "401", description = "Não autenticado"),
        @ApiResponse(responseCode = "403", description = "Sem permissão")
      })
  @SecurityRequirement(name = "bearerAuth")
  public ResponseEntity<String> importCities() {
    if (cityImportService.isDatabasePopulated()) {
      return ResponseEntity.badRequest()
          .body("Banco de dados já contém cidades. Use DELETE antes de importar novamente.");
    }

    cityImportService.importCitiesFromCSV("data/brazilian-cities.csv");
    long count = cityImportService.countCities();

    return ResponseEntity.ok("Importação concluída! Total de cidades: " + count);
  }

  @GetMapping("/cities/count")
  @Operation(summary = "Consultar total de cidades")
  @ApiResponses(@ApiResponse(responseCode = "200", description = "Contagem retornada"))
  public ResponseEntity<Long> countCities() {
    long count = cityImportService.countCities();
    return ResponseEntity.ok(count);
  }
}
