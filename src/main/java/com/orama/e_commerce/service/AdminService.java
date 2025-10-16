package com.orama.e_commerce.service;

import com.orama.e_commerce.dtos.client.ClientRequestDto;
import com.orama.e_commerce.dtos.client.ClientResponseDto;
import com.orama.e_commerce.enums.UserRole;
import com.orama.e_commerce.mapper.ClientMapper;
import com.orama.e_commerce.models.Client;
import com.orama.e_commerce.repository.ClientRepository;
import jakarta.transaction.Transactional;
import java.time.Instant;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AdminService {

  private final ClientRepository clientRepository;
  private final ClientMapper clientMapper;
  private final PasswordEncoder passwordEncoder;

  public AdminService(
      ClientRepository clientRepository,
      ClientMapper clientMapper,
      PasswordEncoder passwordEncoder) {
    this.clientRepository = clientRepository;
    this.clientMapper = clientMapper;
    this.passwordEncoder = passwordEncoder;
  }

  @Transactional
  public ClientResponseDto createAdmin(ClientRequestDto dto) {
    Client client = clientMapper.toEntity(dto);

    client.setActive(true);
    client.setRole(UserRole.ADMIN);
    client.setCreatedAt(Instant.now());
    client.setPasswordHash(passwordEncoder.encode(dto.password()));

    Client savedAdmin = clientRepository.save(client);

    return clientMapper.toResponseDto(savedAdmin);
  }
}
