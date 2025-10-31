package com.orama.e_commerce.repository;

import com.orama.e_commerce.models.State;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StateRepository extends JpaRepository<State, Long> {

  List<State> findByCountryId(Long countryId);

  Optional<State> findByAbbreviation(String abbreviation);

  Optional<State> findByNameAndCountryId(String name, Long countryId);

  boolean existsByAbbreviation(String abbreviation);
}
