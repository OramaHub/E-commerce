package com.orama.e_commerce.service;

import com.orama.e_commerce.dtos.client.ChangePasswordRequestDto;
import com.orama.e_commerce.dtos.client.ClientRequestDto;
import com.orama.e_commerce.dtos.client.ClientResponseDto;
import com.orama.e_commerce.dtos.client.ClientUpdateRequestDto;
import com.orama.e_commerce.enums.UserRole;
import com.orama.e_commerce.exceptions.client.*;
import com.orama.e_commerce.mapper.ClientMapper;
import com.orama.e_commerce.mapper.ProductMapper;
import com.orama.e_commerce.models.Client;
import com.orama.e_commerce.repository.ClientRepository;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
      PasswordEncoder passwordEncoder,
      ProductMapper productMapper) {
    this.clientRepository = clientRepository;
    this.clientMapper = clientMapper;
    this.passwordEncoder = passwordEncoder;
  }

  public ClientResponseDto getById(Long id) {
    Client client = findById(id);
    return clientMapper.toResponseDto(client);
  }

  public Page<ClientResponseDto> getAllActiveClients(Pageable pageable) {
    return clientRepository.findByActiveTrue(pageable).map(clientMapper::toResponseDto);
  }

  public ClientResponseDto getByEmail(String email) {
    Client client =
        clientRepository
            .findByEmail(email)
            .orElseThrow(
                () -> new ClientNotFoundException("Client not found with email: " + email));
    return clientMapper.toResponseDto(client);
  }

  public ClientResponseDto getByCpf(String cpf) {
    Client client =
        clientRepository
            .findByCpf(cpf)
            .orElseThrow(() -> new ClientNotFoundException("Client not found with CPF: " + cpf));
    return clientMapper.toResponseDto(client);
  }

  public Page<ClientResponseDto> getAllByRole(String role, Pageable pageable) {
    UserRole roleEnum;
    try {
      roleEnum = UserRole.valueOf(role.toUpperCase());
    } catch (IllegalArgumentException e) {
      throw new IllegalArgumentException("Invalid role: " + role);
    }

    Page<Client> clients = clientRepository.findByRole(roleEnum, pageable);

    return clients.map(clientMapper::toResponseDto);
  }

  @Transactional
  public ClientResponseDto createClient(ClientRequestDto clientRequestDto) {
    findByEmail(clientRequestDto.email());

    if (clientRepository.existsByCpf(clientRequestDto.cpf())) {
      throw new ClientAlreadyExistsException(
          "Client with CPF '" + clientRequestDto.cpf() + "' already exists");
    }

    Client client = clientMapper.toEntity(clientRequestDto);

    client.setActive(true);
    client.setRole(UserRole.USER);
    client.setPasswordHash(passwordEncoder.encode(client.getPasswordHash()));

    clientRepository.save(client);

    return clientMapper.toResponseDto(client);
  }

  @Transactional
  public ClientResponseDto updateClient(Long id, ClientUpdateRequestDto updateRequestDto) {
    Client client = findById(id);

    if (clientRepository.existsByEmailAndIdNot(updateRequestDto.email(), id)) {
      throw new EmailAlreadyExistsException("Email already in use by another client.");
    }

    clientMapper.updateDto(updateRequestDto, client);

    Client updatedClient = clientRepository.save(client);

    return clientMapper.toResponseDto(updatedClient);
  }

  @Transactional
  public void updatePassword(Long id, ChangePasswordRequestDto dto) {
    Client client = findById(id);

    if (!passwordEncoder.matches(dto.currentPassword(), client.getPasswordHash())) {
      throw new InvalidPasswordException("Current password is incorrect.");
    }

    client.setPasswordHash(passwordEncoder.encode(dto.newPassword()));
    clientRepository.save(client);
  }

  @Transactional
  public void deactivateClient(Long id) {
    Client client = findById(id);

    if (!client.getActive()) {
      throw new ClientAlreadyInactiveException("Client is already inactive.");
    }

    client.setActive(false);
    clientRepository.save(client);
  }

  @Transactional
  public void activateClient(Long id) {
    Client client = findById(id);

    if (client.getActive()) {
      throw new ClientAlreadyActiveException("Client is already active.");
    }

    client.setActive(true);
    clientRepository.save(client);
  }

  private void findByEmail(String email) {
    if (clientRepository.findByEmail(email).isPresent()) {
      throw new ClientAlreadyExistsException("Client with email '" + email + "' already exists");
    }
  }

  public Client findById(Long id) {
    return clientRepository
        .findById(id)
        .orElseThrow(() -> new ClientNotFoundException("Client not found with id " + id));
  }
}
