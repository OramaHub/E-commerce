package com.orama.e_commerce.service;

import com.orama.e_commerce.dtos.location.CepLookupResponseDto;
import com.orama.e_commerce.dtos.location.CityResponseDto;
import com.orama.e_commerce.dtos.location.CitySimpleDto;
import com.orama.e_commerce.dtos.location.CountryResponseDto;
import com.orama.e_commerce.dtos.location.StateResponseDto;
import com.orama.e_commerce.exceptions.BadRequestException;
import com.orama.e_commerce.mapper.LocationMapper;
import com.orama.e_commerce.models.City;
import com.orama.e_commerce.models.Country;
import com.orama.e_commerce.models.State;
import com.orama.e_commerce.repository.CityRepository;
import com.orama.e_commerce.repository.CountryRepository;
import com.orama.e_commerce.repository.StateRepository;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

@Service
public class LocationService {

  private final CountryRepository countryRepository;
  private final StateRepository stateRepository;
  private final CityRepository cityRepository;
  private final LocationMapper locationMapper;
  private final RestClient restClient;

  public LocationService(
      CountryRepository countryRepository,
      StateRepository stateRepository,
      CityRepository cityRepository,
      LocationMapper locationMapper,
      RestClient.Builder restClientBuilder) {
    this.countryRepository = countryRepository;
    this.stateRepository = stateRepository;
    this.cityRepository = cityRepository;
    this.locationMapper = locationMapper;
    this.restClient = restClientBuilder.baseUrl("https://viacep.com.br").build();
  }

  @Transactional(readOnly = true)
  public List<CountryResponseDto> getAllCountries() {
    return countryRepository.findAll().stream().map(locationMapper::toCountryDto).toList();
  }

  @Transactional(readOnly = true)
  public CountryResponseDto getCountryById(Long id) {
    Country country =
        countryRepository
            .findById(id)
            .orElseThrow(() -> new RuntimeException("País não encontrado"));
    return locationMapper.toCountryDto(country);
  }

  @Transactional(readOnly = true)
  public List<StateResponseDto> getAllStates() {
    return stateRepository.findAll().stream().map(locationMapper::toStateDto).toList();
  }

  @Transactional(readOnly = true)
  public List<StateResponseDto> getStatesByCountry(Long countryId) {
    return stateRepository.findByCountryId(countryId).stream()
        .map(locationMapper::toStateDto)
        .toList();
  }

  @Transactional(readOnly = true)
  public StateResponseDto getStateById(Long id) {
    State state =
        stateRepository
            .findById(id)
            .orElseThrow(() -> new RuntimeException("Estado não encontrado"));
    return locationMapper.toStateDto(state);
  }

  @Transactional(readOnly = true)
  public List<CitySimpleDto> getAllCities() {
    return cityRepository.findAll().stream().map(locationMapper::toCitySimpleDto).toList();
  }

  @Transactional(readOnly = true)
  public List<CitySimpleDto> getCitiesByState(Long stateId) {
    return cityRepository.findByStateIdWithDetails(stateId).stream()
        .map(locationMapper::toCitySimpleDto)
        .toList();
  }

  @Transactional(readOnly = true)
  public List<CitySimpleDto> searchCities(String name) {
    return cityRepository.searchCitiesWithDetails(name).stream()
        .map(locationMapper::toCitySimpleDto)
        .toList();
  }

  @Transactional(readOnly = true)
  public CityResponseDto getCityById(Long id) {
    City city =
        cityRepository
            .findByIdWithStateAndCountry(id)
            .orElseThrow(() -> new RuntimeException("Cidade não encontrada"));
    return locationMapper.toCityDto(city);
  }

  @Transactional(readOnly = true)
  public CepLookupResponseDto lookupCep(String cep) {
    String cleanZip = cep.replaceAll("\\D", "");
    if (cleanZip.length() != 8) {
      throw new BadRequestException("CEP inválido. Digite um CEP com 8 dígitos.");
    }

    Map<?, ?> viaCepData = fetchViaCep(cleanZip);

    if (Boolean.TRUE.equals(viaCepData.get("erro"))) {
      throw new BadRequestException("CEP não encontrado.");
    }

    String ibgeCode = (String) viaCepData.get("ibge");
    String street = (String) viaCepData.get("logradouro");
    String district = (String) viaCepData.get("bairro");
    String cityName = (String) viaCepData.get("localidade");
    String state = (String) viaCepData.get("uf");
    String formattedZip = cleanZip.substring(0, 5) + "-" + cleanZip.substring(5);

    Long cityId =
        ibgeCode != null
            ? cityRepository.findByIbgeCode(ibgeCode).map(City::getId).orElse(null)
            : null;

    return new CepLookupResponseDto(formattedZip, street, district, cityName, state, cityId);
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
