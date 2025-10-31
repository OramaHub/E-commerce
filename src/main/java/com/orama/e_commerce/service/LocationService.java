package com.orama.e_commerce.service;

import com.orama.e_commerce.dtos.location.CityResponseDto;
import com.orama.e_commerce.dtos.location.CitySimpleDto;
import com.orama.e_commerce.dtos.location.CountryResponseDto;
import com.orama.e_commerce.dtos.location.StateResponseDto;
import com.orama.e_commerce.mapper.LocationMapper;
import com.orama.e_commerce.models.City;
import com.orama.e_commerce.models.Country;
import com.orama.e_commerce.models.State;
import com.orama.e_commerce.repository.CityRepository;
import com.orama.e_commerce.repository.CountryRepository;
import com.orama.e_commerce.repository.StateRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LocationService {

  private final CountryRepository countryRepository;
  private final StateRepository stateRepository;
  private final CityRepository cityRepository;
  private final LocationMapper locationMapper;

  public LocationService(
      CountryRepository countryRepository,
      StateRepository stateRepository,
      CityRepository cityRepository,
      LocationMapper locationMapper) {
    this.countryRepository = countryRepository;
    this.stateRepository = stateRepository;
    this.cityRepository = cityRepository;
    this.locationMapper = locationMapper;
  }

  @Transactional(readOnly = true)
  public List<CountryResponseDto> getAllCountries() {
    return countryRepository.findAll().stream().map(locationMapper::toCountryDto).toList();
  }

  @Transactional(readOnly = true)
  public CountryResponseDto getCountryById(Long id) {
    Country country =
        countryRepository.findById(id).orElseThrow(() -> new RuntimeException("Country not found"));
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
        stateRepository.findById(id).orElseThrow(() -> new RuntimeException("State not found"));
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
            .orElseThrow(() -> new RuntimeException("City not found"));
    return locationMapper.toCityDto(city);
  }
}
