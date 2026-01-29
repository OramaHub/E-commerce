package com.orama.e_commerce.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import com.orama.e_commerce.dtos.client.ChangePasswordRequestDto;
import com.orama.e_commerce.dtos.client.ClientRequestDto;
import com.orama.e_commerce.dtos.client.ClientResponseDto;
import com.orama.e_commerce.dtos.client.ClientUpdateRequestDto;
import com.orama.e_commerce.enums.UserRole;
import com.orama.e_commerce.exceptions.client.*;
import com.orama.e_commerce.mapper.ClientMapper;
import com.orama.e_commerce.models.Client;
import com.orama.e_commerce.repository.ClientRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class ClientServiceTest {

  @Mock private ClientRepository clientRepository;
  @Mock private ClientMapper clientMapper;
  @Mock private PasswordEncoder passwordEncoder;

  @InjectMocks private ClientService clientService;

  private Client client;
  private ClientResponseDto clientResponseDto;
  private Pageable pageable;

  @BeforeEach
  void setUp() {
    client = new Client();
    client.setId(1L);
    client.setName("João Silva");
    client.setEmail("joao@email.com");
    client.setCpf("12345678901");
    client.setPhone("11999999999");
    client.setPasswordHash("hashedPassword");
    client.setActive(true);
    client.setRole(UserRole.USER);

    clientResponseDto = new ClientResponseDto(1L, "João Silva", "joao@email.com", UserRole.USER);

    pageable = PageRequest.of(0, 10);
  }

  @Test
  void shouldReturnClientById() {
    when(clientRepository.findById(1L)).thenReturn(Optional.of(client));
    when(clientMapper.toResponseDto(client)).thenReturn(clientResponseDto);

    ClientResponseDto result = clientService.getById(1L);

    assertNotNull(result);
    assertEquals(1L, result.id());
    assertEquals("João Silva", result.name());
    verify(clientRepository).findById(1L);
  }

  @Test
  void shouldReturnPageOfActiveClients() {
    Page<Client> clientPage = new PageImpl<>(List.of(client));
    when(clientRepository.findByActiveTrue(pageable)).thenReturn(clientPage);
    when(clientMapper.toResponseDto(client)).thenReturn(clientResponseDto);

    Page<ClientResponseDto> result = clientService.getAllActiveClients(pageable);

    assertNotNull(result);
    assertEquals(1, result.getTotalElements());
    verify(clientRepository).findByActiveTrue(pageable);
  }

  @Test
  void shouldReturnClientByEmail() {
    when(clientRepository.findByEmail("joao@email.com")).thenReturn(Optional.of(client));
    when(clientMapper.toResponseDto(client)).thenReturn(clientResponseDto);

    ClientResponseDto result = clientService.getByEmail("joao@email.com");

    assertNotNull(result);
    assertEquals("joao@email.com", result.email());
    verify(clientRepository).findByEmail("joao@email.com");
  }

  @Test
  void shouldThrowClientNotFoundExceptionWhenEmailNotFound() {
    when(clientRepository.findByEmail("notfound@email.com")).thenReturn(Optional.empty());

    assertThrows(
        ClientNotFoundException.class, () -> clientService.getByEmail("notfound@email.com"));
  }

  @Test
  void shouldReturnClientByCpf() {
    when(clientRepository.findByCpf("12345678901")).thenReturn(Optional.of(client));
    when(clientMapper.toResponseDto(client)).thenReturn(clientResponseDto);

    ClientResponseDto result = clientService.getByCpf("12345678901");

    assertNotNull(result);
    verify(clientRepository).findByCpf("12345678901");
  }

  @Test
  void shouldThrowClientNotFoundExceptionWhenCpfNotFound() {
    when(clientRepository.findByCpf("00000000000")).thenReturn(Optional.empty());

    assertThrows(ClientNotFoundException.class, () -> clientService.getByCpf("00000000000"));
  }

  @Test
  void shouldReturnClientsByRole() {
    Page<Client> clientPage = new PageImpl<>(List.of(client));
    when(clientRepository.findByRole(UserRole.USER, pageable)).thenReturn(clientPage);
    when(clientMapper.toResponseDto(client)).thenReturn(clientResponseDto);

    Page<ClientResponseDto> result = clientService.getAllByRole("USER", pageable);

    assertNotNull(result);
    assertEquals(1, result.getTotalElements());
    verify(clientRepository).findByRole(UserRole.USER, pageable);
  }

  @Test
  void shouldThrowIllegalArgumentExceptionForInvalidRole() {
    assertThrows(
        IllegalArgumentException.class, () -> clientService.getAllByRole("INVALID_ROLE", pageable));
  }

  @Test
  void shouldCreateClient() {
    ClientRequestDto requestDto =
        new ClientRequestDto(
            "João Silva", "joao@email.com", "password123", "12345678901", "11999999999");

    when(clientRepository.findByEmail("joao@email.com")).thenReturn(Optional.empty());
    when(clientMapper.toEntity(requestDto)).thenReturn(client);
    when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
    when(clientRepository.save(any(Client.class))).thenReturn(client);
    when(clientMapper.toResponseDto(client)).thenReturn(clientResponseDto);

    ClientResponseDto result = clientService.createClient(requestDto);

    assertNotNull(result);
    assertEquals("João Silva", result.name());
    verify(clientRepository).save(any(Client.class));
  }

  @Test
  void shouldThrowClientAlreadyExistsExceptionWhenEmailExists() {
    ClientRequestDto requestDto =
        new ClientRequestDto(
            "João Silva", "joao@email.com", "password123", "12345678901", "11999999999");

    when(clientRepository.findByEmail("joao@email.com")).thenReturn(Optional.of(client));

    assertThrows(ClientAlreadyExistsException.class, () -> clientService.createClient(requestDto));
  }

  @Test
  void shouldUpdateClient() {
    ClientUpdateRequestDto updateRequestDto =
        new ClientUpdateRequestDto("João Updated", "joao.updated@email.com", "11888888888");

    when(clientRepository.findById(1L)).thenReturn(Optional.of(client));
    when(clientRepository.existsByEmailAndIdNot("joao.updated@email.com", 1L)).thenReturn(false);
    when(clientRepository.save(any(Client.class))).thenReturn(client);
    when(clientMapper.toResponseDto(client)).thenReturn(clientResponseDto);

    ClientResponseDto result = clientService.updateClient(1L, updateRequestDto);

    assertNotNull(result);
    verify(clientMapper).updateDto(updateRequestDto, client);
    verify(clientRepository).save(client);
  }

  @Test
  void shouldThrowEmailAlreadyExistsExceptionWhenEmailInUse() {
    ClientUpdateRequestDto updateRequestDto =
        new ClientUpdateRequestDto("João Updated", "existing@email.com", "11888888888");

    when(clientRepository.findById(1L)).thenReturn(Optional.of(client));
    when(clientRepository.existsByEmailAndIdNot("existing@email.com", 1L)).thenReturn(true);

    assertThrows(
        EmailAlreadyExistsException.class, () -> clientService.updateClient(1L, updateRequestDto));
  }

  @Test
  void shouldThrowClientNotFoundExceptionWhenUpdatingNonExistentClient() {
    ClientUpdateRequestDto updateRequestDto =
        new ClientUpdateRequestDto("João Updated", "joao@email.com", "11888888888");

    when(clientRepository.findById(99L)).thenReturn(Optional.empty());

    assertThrows(
        ClientNotFoundException.class, () -> clientService.updateClient(99L, updateRequestDto));
  }

  @Test
  void shouldUpdatePassword() {
    ChangePasswordRequestDto passwordDto =
        new ChangePasswordRequestDto("currentPassword", "newPassword");

    when(clientRepository.findById(1L)).thenReturn(Optional.of(client));
    when(passwordEncoder.matches("currentPassword", "hashedPassword")).thenReturn(true);
    when(passwordEncoder.encode("newPassword")).thenReturn("newHashedPassword");

    clientService.updatePassword(1L, passwordDto);

    verify(clientRepository).save(client);
    assertEquals("newHashedPassword", client.getPasswordHash());
  }

  @Test
  void shouldThrowInvalidPasswordExceptionWhenCurrentPasswordIncorrect() {
    ChangePasswordRequestDto passwordDto =
        new ChangePasswordRequestDto("wrongPassword", "newPassword");

    when(clientRepository.findById(1L)).thenReturn(Optional.of(client));
    when(passwordEncoder.matches("wrongPassword", "hashedPassword")).thenReturn(false);

    assertThrows(
        InvalidPasswordException.class, () -> clientService.updatePassword(1L, passwordDto));
  }

  @Test
  void shouldDeactivateClient() {
    when(clientRepository.findById(1L)).thenReturn(Optional.of(client));

    clientService.deactivateClient(1L);

    assertFalse(client.getActive());
    verify(clientRepository).save(client);
  }

  @Test
  void shouldThrowClientAlreadyInactiveExceptionWhenDeactivatingInactiveClient() {
    client.setActive(false);
    when(clientRepository.findById(1L)).thenReturn(Optional.of(client));

    assertThrows(ClientAlreadyInactiveException.class, () -> clientService.deactivateClient(1L));
  }

  @Test
  void shouldActivateClient() {
    client.setActive(false);
    when(clientRepository.findById(1L)).thenReturn(Optional.of(client));

    clientService.activateClient(1L);

    assertTrue(client.getActive());
    verify(clientRepository).save(client);
  }

  @Test
  void shouldThrowClientAlreadyActiveExceptionWhenActivatingActiveClient() {
    when(clientRepository.findById(1L)).thenReturn(Optional.of(client));

    assertThrows(ClientAlreadyActiveException.class, () -> clientService.activateClient(1L));
  }

  @Test
  void shouldFindClientById() {
    when(clientRepository.findById(1L)).thenReturn(Optional.of(client));

    Client result = clientService.findById(1L);

    assertNotNull(result);
    assertEquals(1L, result.getId());
  }

  @Test
  void shouldThrowClientNotFoundExceptionWhenFindByIdNotFound() {
    when(clientRepository.findById(99L)).thenReturn(Optional.empty());

    assertThrows(ClientNotFoundException.class, () -> clientService.findById(99L));
  }
}
