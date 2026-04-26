package com.orama.e_commerce.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.orama.e_commerce.dtos.location.CepLookupResponseDto;
import com.orama.e_commerce.exceptions.BadRequestException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

class LocationServiceTest {

  private MockRestServiceServer server;
  private LocationService locationService;

  @BeforeEach
  void setUp() {
    RestClient.Builder builder = RestClient.builder();
    server = MockRestServiceServer.bindTo(builder).build();
    locationService = new LocationService(builder);
  }

  @Test
  void shouldLookupCepWithTextualLocation() {
    server
        .expect(requestTo("https://viacep.com.br/ws/01234567/json/"))
        .andRespond(
            withSuccess(
                """
                {
                  "cep": "01234-567",
                  "logradouro": "Rua das Flores",
                  "bairro": "Centro",
                  "localidade": "Sao Paulo",
                  "uf": "SP",
                  "ibge": "3550308"
                }
                """,
                MediaType.APPLICATION_JSON));

    CepLookupResponseDto result = locationService.lookupCep("01234-567");

    assertNotNull(result);
    assertEquals("01234-567", result.zipCode());
    assertEquals("Rua das Flores", result.street());
    assertEquals("Centro", result.district());
    assertEquals("Sao Paulo", result.cityName());
    assertEquals("SP", result.stateUf());
    assertEquals("BR", result.countryCode());
    assertEquals("3550308", result.ibgeCode());
    server.verify();
  }

  @Test
  void shouldThrowWhenCepIsInvalid() {
    assertThrows(BadRequestException.class, () -> locationService.lookupCep("123"));
  }

  @Test
  void shouldThrowWhenViaCepReturnsError() {
    server
        .expect(requestTo("https://viacep.com.br/ws/99999999/json/"))
        .andRespond(withSuccess("{\"erro\": true}", MediaType.APPLICATION_JSON));

    assertThrows(BadRequestException.class, () -> locationService.lookupCep("99999-999"));
    server.verify();
  }
}
