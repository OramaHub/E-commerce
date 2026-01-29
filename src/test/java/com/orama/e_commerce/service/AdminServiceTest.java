package com.orama.e_commerce.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.orama.e_commerce.dtos.client.AdminPasswordResetDto;
import com.orama.e_commerce.dtos.client.ClientRequestDto;
import com.orama.e_commerce.dtos.client.ClientResponseDto;
import com.orama.e_commerce.enums.UserRole;
import com.orama.e_commerce.mapper.ClientMapper;
import com.orama.e_commerce.models.Client;
import com.orama.e_commerce.repository.ClientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AdminServiceTest {

  @Mock private ClientService clientService;
  @Mock private ClientRepository clientRepository;
  @Mock private ClientMapper clientMapper;
  @Mock private PasswordEncoder passwordEncoder;

  @InjectMocks private AdminService adminService;

  private Client client;
  private ClientResponseDto clientResponseDto;

  @BeforeEach
  void setUp() {
    client = new Client();
    client.setId(1L);
    client.setName("Admin User");
    client.setEmail("admin@email.com");
    client.setCpf("12345678901");
    client.setPhone("11999999999");
    client.setPasswordHash("hashedPassword");
    client.setActive(true);
    client.setRole(UserRole.ADMIN);

    clientResponseDto = new ClientResponseDto(1L, "Admin User", "admin@email.com", UserRole.ADMIN);
  }

  @Test
  void shouldCreateAdmin() {
    ClientRequestDto requestDto =
        new ClientRequestDto(
            "Admin User", "admin@email.com", "password123", "12345678901", "11999999999");

    when(clientMapper.toEntity(requestDto)).thenReturn(client);
    when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
    when(clientRepository.save(any(Client.class))).thenReturn(client);
    when(clientMapper.toResponseDto(client)).thenReturn(clientResponseDto);

    ClientResponseDto result = adminService.createAdmin(requestDto);

    assertNotNull(result);
    assertEquals("Admin User", result.name());
    assertEquals(UserRole.ADMIN, result.role());
    assertTrue(client.getActive());
    assertEquals(UserRole.ADMIN, client.getRole());
    verify(clientRepository).save(any(Client.class));
  }

  @Test
  void shouldResetClientPassword() {
    AdminPasswordResetDto passwordDto = new AdminPasswordResetDto("newPassword123");

    when(clientService.findById(1L)).thenReturn(client);
    when(passwordEncoder.encode("newPassword123")).thenReturn("newEncodedPassword");

    adminService.resetClientPassword(1L, passwordDto);

    assertEquals("newEncodedPassword", client.getPasswordHash());
    verify(clientRepository).save(client);
  }

  @Test
  void shouldResetPasswordForDifferentClient() {
    Client targetClient = new Client();
    targetClient.setId(2L);
    targetClient.setName("João Silva");
    targetClient.setEmail("joao@email.com");
    targetClient.setPasswordHash("oldPassword");
    targetClient.setRole(UserRole.USER);

    AdminPasswordResetDto passwordDto = new AdminPasswordResetDto("newSecurePassword");

    when(clientService.findById(2L)).thenReturn(targetClient);
    when(passwordEncoder.encode("newSecurePassword")).thenReturn("newEncodedPassword");

    adminService.resetClientPassword(2L, passwordDto);

    assertEquals("newEncodedPassword", targetClient.getPasswordHash());
    verify(clientRepository).save(targetClient);
  }
}
