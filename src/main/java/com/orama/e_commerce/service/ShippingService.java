package com.orama.e_commerce.service;

import com.orama.e_commerce.dtos.shipping.ShippingCalculateResponseDto;
import com.orama.e_commerce.exceptions.BadRequestException;
import java.math.BigDecimal;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class ShippingService {

  private static final BigDecimal SHIPPING_COST_DEFAULT = new BigDecimal("60.00");
  private static final BigDecimal SHIPPING_COST_FREE = BigDecimal.ZERO;
  private static final int NORDESTE_PREFIX_MIN = 40;
  private static final int NORDESTE_PREFIX_MAX = 65;

  private final RestClient restClient;

  public ShippingService(RestClient.Builder restClientBuilder) {
    this.restClient = restClientBuilder.baseUrl("https://viacep.com.br").build();
  }

  public ShippingCalculateResponseDto calculateShipping(String zipCode) {
    String cleanZip = zipCode.replaceAll("\\D", "");
    if (cleanZip.length() != 8) {
      throw new BadRequestException("CEP inválido. Digite um CEP com 8 dígitos.");
    }

    Map<?, ?> viaCepResponse = fetchViaCep(cleanZip);

    if (Boolean.TRUE.equals(viaCepResponse.get("erro"))) {
      throw new BadRequestException("CEP não encontrado.");
    }

    String city = (String) viaCepResponse.get("localidade");
    String state = (String) viaCepResponse.get("uf");

    int prefix = Integer.parseInt(cleanZip.substring(0, 2));
    boolean isNordeste = prefix >= NORDESTE_PREFIX_MIN && prefix <= NORDESTE_PREFIX_MAX;
    String region = isNordeste ? "Nordeste" : "Demais regiões";
    BigDecimal shippingCost = isNordeste ? SHIPPING_COST_FREE : SHIPPING_COST_DEFAULT;

    String formattedZip = cleanZip.substring(0, 5) + "-" + cleanZip.substring(5);

    return new ShippingCalculateResponseDto(
        formattedZip, city, state, region, shippingCost, isNordeste);
  }

  public BigDecimal getShippingCost(String zipCode) {
    String cleanZip = zipCode.replaceAll("\\D", "");
    if (cleanZip.length() != 8) {
      throw new BadRequestException("CEP inválido. Digite um CEP com 8 dígitos.");
    }
    int prefix = Integer.parseInt(cleanZip.substring(0, 2));
    boolean isNordeste = prefix >= NORDESTE_PREFIX_MIN && prefix <= NORDESTE_PREFIX_MAX;
    return isNordeste ? SHIPPING_COST_FREE : SHIPPING_COST_DEFAULT;
  }

  @SuppressWarnings("unchecked")
  private Map<?, ?> fetchViaCep(String cep) {
    try {
      return restClient.get().uri("/ws/{cep}/json/", cep).retrieve().body(Map.class);
    } catch (Exception e) {
      throw new BadRequestException("Não foi possível consultar o CEP. Tente novamente.");
    }
  }
}
