package com.orama.e_commerce.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.orama.e_commerce.repository.CityRepository;
import com.orama.e_commerce.repository.CountryRepository;
import com.orama.e_commerce.repository.StateRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CityImportServiceTest {

  @Mock private CountryRepository countryRepository;
  @Mock private StateRepository stateRepository;
  @Mock private CityRepository cityRepository;

  @InjectMocks private CityImportService cityImportService;

  @BeforeEach
  void setUp() {}

  @Test
  void shouldCountCities() {
    when(cityRepository.count()).thenReturn(5570L);

    long result = cityImportService.countCities();

    assertEquals(5570L, result);
    verify(cityRepository).count();
  }

  @Test
  void shouldReturnTrueWhenDatabaseIsPopulated() {
    when(cityRepository.count()).thenReturn(5570L);

    boolean result = cityImportService.isDatabasePopulated();

    assertTrue(result);
    verify(cityRepository).count();
  }

  @Test
  void shouldReturnFalseWhenDatabaseIsEmpty() {
    when(cityRepository.count()).thenReturn(0L);

    boolean result = cityImportService.isDatabasePopulated();

    assertFalse(result);
    verify(cityRepository).count();
  }
}
