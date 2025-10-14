package com.orama.e_commerce.repository;

import com.orama.e_commerce.models.Client;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClientRepository extends JpaRepository<Client, Long> {
  Optional<Client> findByEmail(String mail);
}
