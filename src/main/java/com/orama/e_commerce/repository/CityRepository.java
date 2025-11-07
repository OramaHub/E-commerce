package com.orama.e_commerce.repository;

import com.orama.e_commerce.models.City;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CityRepository extends JpaRepository<City, Long> {

  List<City> findByStateId(Long stateId);

  List<City> findByNameContainingIgnoreCase(String name);

  Optional<City> findByIbgeCode(String ibgeCode);

  @Query("SELECT c FROM City c JOIN FETCH c.state s JOIN FETCH s.country WHERE c.id = :id")
  Optional<City> findByIdWithStateAndCountry(@Param("id") Long id);

  @Query(
      "SELECT c FROM City c JOIN FETCH c.state s JOIN FETCH s.country "
          + "WHERE LOWER(c.name) LIKE LOWER(CONCAT('%', :name, '%')) "
          + "ORDER BY c.name")
  List<City> searchCitiesWithDetails(@Param("name") String name);

  @Query(
      "SELECT c FROM City c JOIN FETCH c.state s JOIN FETCH s.country "
          + "WHERE s.id = :stateId "
          + "ORDER BY c.name")
  List<City> findByStateIdWithDetails(@Param("stateId") Long stateId);
}
