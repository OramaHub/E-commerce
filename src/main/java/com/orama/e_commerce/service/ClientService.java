package com.orama.e_commerce.service;

import com.orama.e_commerce.dtos.client.ChangePasswordRequestDto;
import com.orama.e_commerce.dtos.client.ClientRequestDto;
import com.orama.e_commerce.dtos.client.ClientResponseDto;
import com.orama.e_commerce.dtos.client.ClientUpdateRequestDto;
import com.orama.e_commerce.enums.UserRole;
import com.orama.e_commerce.mapper.ClientMapper;
import com.orama.e_commerce.models.Client;
import com.orama.e_commerce.repository.ClientRepository;
import jakarta.transaction.Transactional;
import java.time.Instant;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class ClientService {

  private final ClientRepository clientRepository;
  private final ClientMapper clientMapper;
  private final PasswordEncoder passwordEncoder;

  public ClientService(
      ClientRepository clientRepository,
      ClientMapper clientMapper,
      PasswordEncoder passwordEncoder) {
    this.clientRepository = clientRepository;
    this.clientMapper = clientMapper;
    this.passwordEncoder = passwordEncoder;
  }

  @Transactional
  public ClientResponseDto createClient(ClientRequestDto clientRequestDto) {
    findByEmail(clientRequestDto.email());

    Client client = clientMapper.toEntity(clientRequestDto);

    client.setActive(true);
    client.setRole(UserRole.USER);
    client.setCreatedAt(Instant.now());
    client.setPasswordHash(passwordEncoder.encode(client.getPasswordHash()));

    clientRepository.save(client);

    return clientMapper.toResponseDto(client);
  }

  @Transactional
  public ClientResponseDto updateClient(Long id, ClientUpdateRequestDto updateRequestDto) {
    Client client = findById(id);

    if (clientRepository.existsByEmailAndIdNot(updateRequestDto.email(), id)) {
      throw new RuntimeException("Email already in use by another client.");
    }

    clientMapper.updateDto(updateRequestDto, client);

    Client updatedClient = clientRepository.save(client);

    return clientMapper.toResponseDto(updatedClient);
  }

  @Transactional
  public void updatePassword(Long id, ChangePasswordRequestDto dto) {
    Client client =
        clientRepository.findById(id).orElseThrow(() -> new RuntimeException("Client not found"));

    if (!client.getPasswordHash().equals(dto.currentPassword())) {
      throw new RuntimeException("Current password is incorrect.");
    }

    client.setPasswordHash(dto.newPassword());
    clientRepository.save(client);
  }

  @Transactional
  public void deactivateClient(Long id) {
    Client client = findById(id);

    if (!client.getActive()) {
      throw new RuntimeException("Client is already inactive.");
    }

    client.setActive(false);
    clientRepository.save(client);
  }

  private void findByEmail(String email) {
    if (clientRepository.findByEmail(email).isPresent()) {
      throw new RuntimeException("User with email '" + email + "' already exists");
    }
  }

  private Client findById(Long id) {
    return clientRepository
        .findById(id)
        .orElseThrow(() -> new RuntimeException("User not found with id " + id));
  }
}
