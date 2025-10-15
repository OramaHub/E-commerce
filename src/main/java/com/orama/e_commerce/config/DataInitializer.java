package com.orama.e_commerce.config;

import com.orama.e_commerce.enums.UserRole;
import com.orama.e_commerce.models.Client;
import com.orama.e_commerce.repository.ClientRepository;
import java.time.Instant;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

  private final ClientRepository clientRepository;
  private final PasswordEncoder passwordEncoder;

  @Value("${admin.email}")
  private String adminEmail;

  @Value("${admin.password}")
  private String adminPassword;

  @Value("${admin.name}")
  private String adminName;

  @Value("${admin.cpf}")
  private String adminCpf;

  @Value("${admin.phone}")
  private String adminPhone;

  public DataInitializer(ClientRepository clientRepository, PasswordEncoder passwordEncoder) {
    this.clientRepository = clientRepository;
    this.passwordEncoder = passwordEncoder;
  }

  @Override
  public void run(String... args) {
    if (clientRepository.findByEmail(adminEmail).isEmpty()) {
      Client admin = new Client();
      admin.setName(adminName);
      admin.setEmail(adminEmail);
      admin.setPasswordHash(passwordEncoder.encode(adminPassword));
      admin.setRole(UserRole.ADMIN);
      admin.setCpf(adminCpf);
      admin.setPhone(adminPhone);
      admin.setActive(true);
      admin.setCreatedAt(Instant.now());

      clientRepository.save(admin);
    }
  }
}
