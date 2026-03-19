package com.orama.e_commerce.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

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
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LocationServiceTest {

  @Mock private CountryRepository countryRepository;
  @Mock private StateRepository stateRepository;
  @Mock private CityRepository cityRepository;
  @Mock private LocationMapper locationMapper;

  @InjectMocks private LocationService locationService;

  private Country country;
  private State state;
  private City city;
  private CountryResponseDto countryResponseDto;
  private StateResponseDto stateResponseDto;
  private CityResponseDto cityResponseDto;
  private CitySimpleDto citySimpleDto;

  @BeforeEach
  void setUp() {
    country = new Country();
    country.setId(1L);
    country.setName("Brasil");
    country.setAbbreviation("BR");

    state = new State();
    state.setId(1L);
    state.setName("São Paulo");
    state.setAbbreviation("SP");
    state.setCountry(country);

    city = new City();
    city.setId(1L);
    city.setName("São Paulo");
    city.setIbgeCode("3550308");
    city.setState(state);

    countryResponseDto = new CountryResponseDto(1L, "Brasil", "BR");
    stateResponseDto = new StateResponseDto(1L, "São Paulo", "SP", countryResponseDto);
    cityResponseDto = new CityResponseDto(1L, "São Paulo", "3550308", stateResponseDto);
    citySimpleDto = new CitySimpleDto(1L, "São Paulo", "3550308", 1L, "São Paulo", "SP");
  }

  @Test
  void shouldGetAllCountries() {
    when(countryRepository.findAll()).thenReturn(List.of(country));
    when(locationMapper.toCountryDto(country)).thenReturn(countryResponseDto);

    List<CountryResponseDto> result = locationService.getAllCountries();

    assertNotNull(result);
    assertEquals(1, result.size());
    assertEquals("Brasil", result.get(0).name());
    verify(countryRepository).findAll();
  }

  @Test
  void shouldGetCountryById() {
    when(countryRepository.findById(1L)).thenReturn(Optional.of(country));
    when(locationMapper.toCountryDto(country)).thenReturn(countryResponseDto);

    CountryResponseDto result = locationService.getCountryById(1L);

    assertNotNull(result);
    assertEquals(1L, result.id());
    assertEquals("Brasil", result.name());
    verify(countryRepository).findById(1L);
  }

  @Test
  void shouldThrowExceptionWhenCountryNotFound() {
    when(countryRepository.findById(99L)).thenReturn(Optional.empty());

    assertThrows(RuntimeException.class, () -> locationService.getCountryById(99L));
  }

  @Test
  void shouldGetAllStates() {
    when(stateRepository.findAll()).thenReturn(List.of(state));
    when(locationMapper.toStateDto(state)).thenReturn(stateResponseDto);

    List<StateResponseDto> result = locationService.getAllStates();

    assertNotNull(result);
    assertEquals(1, result.size());
    assertEquals("São Paulo", result.get(0).name());
    verify(stateRepository).findAll();
  }

  @Test
  void shouldGetStatesByCountry() {
    when(stateRepository.findByCountryId(1L)).thenReturn(List.of(state));
    when(locationMapper.toStateDto(state)).thenReturn(stateResponseDto);

    List<StateResponseDto> result = locationService.getStatesByCountry(1L);

    assertNotNull(result);
    assertEquals(1, result.size());
    verify(stateRepository).findByCountryId(1L);
  }

  @Test
  void shouldGetStateById() {
    when(stateRepository.findById(1L)).thenReturn(Optional.of(state));
    when(locationMapper.toStateDto(state)).thenReturn(stateResponseDto);

    StateResponseDto result = locationService.getStateById(1L);

    assertNotNull(result);
    assertEquals(1L, result.id());
    assertEquals("São Paulo", result.name());
    verify(stateRepository).findById(1L);
  }

  @Test
  void shouldThrowExceptionWhenStateNotFound() {
    when(stateRepository.findById(99L)).thenReturn(Optional.empty());

    assertThrows(RuntimeException.class, () -> locationService.getStateById(99L));
  }

  @Test
  void shouldGetAllCities() {
    when(cityRepository.findAll()).thenReturn(List.of(city));
    when(locationMapper.toCitySimpleDto(city)).thenReturn(citySimpleDto);

    List<CitySimpleDto> result = locationService.getAllCities();

    assertNotNull(result);
    assertEquals(1, result.size());
    assertEquals("São Paulo", result.get(0).name());
    verify(cityRepository).findAll();
  }

  @Test
  void shouldGetCitiesByState() {
    when(cityRepository.findByStateIdWithDetails(1L)).thenReturn(List.of(city));
    when(locationMapper.toCitySimpleDto(city)).thenReturn(citySimpleDto);

    List<CitySimpleDto> result = locationService.getCitiesByState(1L);

    assertNotNull(result);
    assertEquals(1, result.size());
    verify(cityRepository).findByStateIdWithDetails(1L);
  }

  @Test
  void shouldSearchCities() {
    when(cityRepository.searchCitiesWithDetails("São Paulo")).thenReturn(List.of(city));
    when(locationMapper.toCitySimpleDto(city)).thenReturn(citySimpleDto);

    List<CitySimpleDto> result = locationService.searchCities("São Paulo");

    assertNotNull(result);
    assertEquals(1, result.size());
    assertEquals("São Paulo", result.get(0).name());
    verify(cityRepository).searchCitiesWithDetails("São Paulo");
  }

  @Test
  void shouldGetCityById() {
    when(cityRepository.findByIdWithStateAndCountry(1L)).thenReturn(Optional.of(city));
    when(locationMapper.toCityDto(city)).thenReturn(cityResponseDto);

    CityResponseDto result = locationService.getCityById(1L);

    assertNotNull(result);
    assertEquals(1L, result.id());
    assertEquals("São Paulo", result.name());
    verify(cityRepository).findByIdWithStateAndCountry(1L);
  }

  @Test
  void shouldThrowExceptionWhenCityNotFound() {
    when(cityRepository.findByIdWithStateAndCountry(99L)).thenReturn(Optional.empty());

    assertThrows(RuntimeException.class, () -> locationService.getCityById(99L));
  }
}
