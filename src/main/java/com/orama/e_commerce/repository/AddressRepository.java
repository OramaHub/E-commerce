package com.orama.e_commerce.repository;

import com.orama.e_commerce.models.Address;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AddressRepository extends JpaRepository<Address, Long> {

  List<Address> findByClientId(Long clientId);

  List<Address> findByCityId(Long cityId);

  boolean existsByIdAndClientId(Long id, Long clientId);

  Optional<Address> findByClientIdAndDefaultAddressTrue(Long clientId);

  boolean existsByClientIdAndDefaultAddressTrue(Long clientId);
}
