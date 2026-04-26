package com.orama.e_commerce.service;

import com.orama.e_commerce.dtos.location.CepLookupResponseDto;
import com.orama.e_commerce.exceptions.BadRequestException;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

@Service
public class LocationService {

  private final RestClient restClient;

  public LocationService(RestClient.Builder restClientBuilder) {
    this.restClient = restClientBuilder.baseUrl("https://viacep.com.br").build();
  }

  @Transactional(readOnly = true)
  public CepLookupResponseDto lookupCep(String cep) {
    String cleanZip = cep.replaceAll("\\D", "");
    if (cleanZip.length() != 8) {
      throw new BadRequestException("CEP invalido. Digite um CEP com 8 digitos.");
    }

    Map<?, ?> viaCepData = fetchViaCep(cleanZip);

    if (Boolean.TRUE.equals(viaCepData.get("erro"))) {
      throw new BadRequestException("CEP nao encontrado.");
    }

    String ibgeCode = (String) viaCepData.get("ibge");
    String street = (String) viaCepData.get("logradouro");
    String district = (String) viaCepData.get("bairro");
    String cityName = (String) viaCepData.get("localidade");
    String stateUf = (String) viaCepData.get("uf");
    String formattedZip = cleanZip.substring(0, 5) + "-" + cleanZip.substring(5);

    return new CepLookupResponseDto(
        formattedZip, street, district, cityName, stateUf, "BR", ibgeCode);
  }

  @SuppressWarnings("unchecked")
  private Map<?, ?> fetchViaCep(String cep) {
    try {
      return restClient.get().uri("/ws/{cep}/json/", cep).retrieve().body(Map.class);
    } catch (Exception e) {
      throw new BadRequestException("Nao foi possivel consultar o CEP. Tente novamente.");
    }
  }
}
