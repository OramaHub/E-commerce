package com.orama.e_commerce.config;

import com.orama.e_commerce.enums.UserRole;
import com.orama.e_commerce.models.Client;
import com.orama.e_commerce.repository.ClientRepository;
import java.time.Instant;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

  private final ClientRepository clientRepository;

  public DataInitializer(ClientRepository clientRepository) {
    this.clientRepository = clientRepository;
  }

  @Override
  public void run(String... args) {
    if (clientRepository.findByEmail("admin@email.com").isEmpty()) {
      Client admin = new Client();
      admin.setName("First Admin");
      admin.setEmail("admin@email.com");
      admin.setPasswordHash("123456");
      admin.setRole(UserRole.ADMIN);
      admin.setCpf("00000000000");
      admin.setPhone("(00) 00000-0000");
      admin.setActive(true);
      admin.setCreatedAt(Instant.now());

      clientRepository.save(admin);
    }
  }
}
