package com.orama.e_commerce.service;

import com.orama.e_commerce.models.City;
import com.orama.e_commerce.models.Country;
import com.orama.e_commerce.models.State;
import com.orama.e_commerce.repository.CityRepository;
import com.orama.e_commerce.repository.CountryRepository;
import com.orama.e_commerce.repository.StateRepository;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CityImportService {

  private static final Logger logger = LoggerFactory.getLogger(CityImportService.class);

  private final CountryRepository countryRepository;
  private final StateRepository stateRepository;
  private final CityRepository cityRepository;

  private static final Map<String, String> STATE_NAMES =
      Map.ofEntries(
          Map.entry("AC", "Acre"),
          Map.entry("AL", "Alagoas"),
          Map.entry("AP", "Amapá"),
          Map.entry("AM", "Amazonas"),
          Map.entry("BA", "Bahia"),
          Map.entry("CE", "Ceará"),
          Map.entry("DF", "Distrito Federal"),
          Map.entry("ES", "Espírito Santo"),
          Map.entry("GO", "Goiás"),
          Map.entry("MA", "Maranhão"),
          Map.entry("MT", "Mato Grosso"),
          Map.entry("MS", "Mato Grosso do Sul"),
          Map.entry("MG", "Minas Gerais"),
          Map.entry("PA", "Pará"),
          Map.entry("PB", "Paraíba"),
          Map.entry("PR", "Paraná"),
          Map.entry("PE", "Pernambuco"),
          Map.entry("PI", "Piauí"),
          Map.entry("RJ", "Rio de Janeiro"),
          Map.entry("RN", "Rio Grande do Norte"),
          Map.entry("RS", "Rio Grande do Sul"),
          Map.entry("RO", "Rondônia"),
          Map.entry("RR", "Roraima"),
          Map.entry("SC", "Santa Catarina"),
          Map.entry("SP", "São Paulo"),
          Map.entry("SE", "Sergipe"),
          Map.entry("TO", "Tocantins"));

  public CityImportService(
      CountryRepository countryRepository,
      StateRepository stateRepository,
      CityRepository cityRepository) {
    this.countryRepository = countryRepository;
    this.stateRepository = stateRepository;
    this.cityRepository = cityRepository;
  }

  @Transactional
  public void importCitiesFromCSV(String filePath) {
    try {
      long startTime = System.currentTimeMillis();
      logger.info("Iniciando importação de cidades do arquivo: {}", filePath);

      Country brazil = getOrCreateBrazil();

      Map<String, State> stateCache = new HashMap<>();

      ClassPathResource resource = new ClassPathResource(filePath);
      try (BufferedReader reader =
          new BufferedReader(
              new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {

        String line;
        boolean isFirstLine = true;
        int count = 0;
        int batchSize = 100;

        while ((line = reader.readLine()) != null) {
          if (isFirstLine) {
            isFirstLine = false;
            continue;
          }

          String[] parts = line.split(",");
          if (parts.length < 3) {
            logger.warn("Linha inválida: {}", line);
            continue;
          }

          String cityName = parts[1].trim();
          String ibgeCode = parts[2].trim();
          String stateRef = parts[3].trim();

          String stateAbbrev = extractStateAbbreviation(stateRef);

          if (stateAbbrev == null) {
            logger.warn("Estado não encontrado na linha: {}", line);
            continue;
          }

          State state =
              stateCache.computeIfAbsent(stateAbbrev, abbrev -> getOrCreateState(abbrev, brazil));

          if (cityRepository.findByIbgeCode(ibgeCode).isEmpty()) {
            City city = new City();
            city.setName(cityName);
            city.setIbgeCode(ibgeCode);
            city.setState(state);
            cityRepository.save(city);
            count++;

            if (count % batchSize == 0) {
              logger.info("Importadas {} cidades...", count);
            }
          }
        }

        long endTime = System.currentTimeMillis();
        logger.info(
            "Importação concluída! {} cidades importadas em {} ms", count, (endTime - startTime));
      }

    } catch (IOException e) {
      logger.error("Erro ao importar cidades do CSV", e);
      throw new RuntimeException("Erro ao importar cidades: " + e.getMessage(), e);
    }
  }

  private Country getOrCreateBrazil() {
    return countryRepository
        .findByName("Brasil")
        .orElseGet(
            () -> {
              Country brazil = new Country();
              brazil.setName("Brasil");
              brazil.setAbbreviation("BR");
              return countryRepository.save(brazil);
            });
  }

  private State getOrCreateState(String abbreviation, Country country) {
    return stateRepository
        .findByAbbreviation(abbreviation)
        .orElseGet(
            () -> {
              State state = new State();
              state.setAbbreviation(abbreviation);
              state.setName(STATE_NAMES.getOrDefault(abbreviation, abbreviation));
              state.setCountry(country);
              return stateRepository.save(state);
            });
  }

  private String extractStateAbbreviation(String stateRef) {
    if (stateRef.contains("state_br_")) {
      String[] parts = stateRef.split("state_br_");
      if (parts.length > 1) {
        return parts[1].toUpperCase();
      }
    }
    return null;
  }

  @Transactional(readOnly = true)
  public long countCities() {
    return cityRepository.count();
  }

  @Transactional(readOnly = true)
  public boolean isDatabasePopulated() {
    return cityRepository.count() > 0;
  }
}
