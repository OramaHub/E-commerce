package com.orama.e_commerce.repository;

import com.orama.e_commerce.enums.UserRole;
import com.orama.e_commerce.models.Client;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClientRepository extends JpaRepository<Client, Long> {
  Optional<Client> findByEmail(String email);

  Optional<Client> findByCpf(String cpf);

  Page<Client> findByRole(UserRole role, Pageable pageable);

  Page<Client> findByActiveTrue(Pageable pageable);

  boolean existsByEmailAndIdNot(String email, Long id);

  boolean existsByCpf(String cpf);
}
