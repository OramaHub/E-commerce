package com.orama.e_commerce.repository;

import com.orama.e_commerce.models.Country;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CountryRepository extends JpaRepository<Country, Long> {

  Optional<Country> findByName(String name);

  Optional<Country> findByAbbreviation(String abbreviation);

  boolean existsByName(String name);
}
