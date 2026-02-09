package com.orama.e_commerce.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.orama.e_commerce.dtos.auth.AuthRegisterResponseDto;
import com.orama.e_commerce.dtos.auth.AuthResponseDto;
import com.orama.e_commerce.dtos.auth.LoginRequestDto;
import com.orama.e_commerce.dtos.client.ClientRequestDto;
import com.orama.e_commerce.dtos.client.ClientResponseDto;
import com.orama.e_commerce.enums.UserRole;
import com.orama.e_commerce.models.Client;
import com.orama.e_commerce.repository.ClientRepository;
import com.orama.e_commerce.security.JwtService;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

  @Mock private AuthenticationManager authenticationManager;
  @Mock private UserDetailsService userDetailsService;
  @Mock private JwtService jwtService;
  @Mock private ClientService clientService;
  @Mock private ClientRepository clientRepository;

  @InjectMocks private AuthService authService;

  private Client client;
  private UserDetails userDetails;
  private ClientResponseDto clientResponseDto;

  @BeforeEach
  void setUp() {
    client = new Client();
    client.setId(1L);
    client.setName("João Silva");
    client.setEmail("joao@email.com");
    client.setPasswordHash("hashedPassword");
    client.setRole(UserRole.USER);

    userDetails =
        User.builder().username("joao@email.com").password("hashedPassword").roles("USER").build();

    clientResponseDto = new ClientResponseDto(1L, "João Silva", "joao@email.com", UserRole.USER);
  }

  @Test
  void shouldLoginSuccessfully() {
    LoginRequestDto loginRequest = new LoginRequestDto("joao@email.com", "password123");

    when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
        .thenReturn(null);
    when(clientRepository.findByEmail("joao@email.com")).thenReturn(Optional.of(client));
    when(userDetailsService.loadUserByUsername("joao@email.com")).thenReturn(userDetails);
    when(jwtService.generateToken(userDetails, 1L)).thenReturn("jwt-token");
    when(jwtService.getAccessExpirationTime()).thenReturn(3600000L);

    AuthResponseDto result = authService.login(loginRequest);

    assertNotNull(result);
    assertEquals("jwt-token", result.token());
    assertEquals("Bearer", result.type());
    assertEquals(3600000L, result.expiresIn());
    verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
  }

  @Test
  void shouldThrowExceptionWhenLoginWithInvalidCredentials() {
    LoginRequestDto loginRequest = new LoginRequestDto("joao@email.com", "wrongPassword");

    when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
        .thenThrow(new BadCredentialsException("Invalid credentials"));

    assertThrows(BadCredentialsException.class, () -> authService.login(loginRequest));
  }

  @Test
  void shouldThrowExceptionWhenClientNotFoundOnLogin() {
    LoginRequestDto loginRequest = new LoginRequestDto("notfound@email.com", "password123");

    when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
        .thenReturn(null);
    when(clientRepository.findByEmail("notfound@email.com")).thenReturn(Optional.empty());

    assertThrows(RuntimeException.class, () -> authService.login(loginRequest));
  }

  @Test
  void shouldRegisterSuccessfully() {
    ClientRequestDto requestDto =
        new ClientRequestDto(
            "João Silva", "joao@email.com", "password123", "12345678901", "11999999999");

    when(clientService.createClient(requestDto)).thenReturn(clientResponseDto);

    AuthRegisterResponseDto result = authService.register(requestDto);

    assertNotNull(result);
    assertEquals("joao@email.com", result.email());
    assertEquals("USER", result.role());
    verify(clientService).createClient(requestDto);
  }
}
