package com.orama.e_commerce.service;

import com.orama.e_commerce.dtos.auth.AuthRegisterResponseDto;
import com.orama.e_commerce.dtos.auth.AuthResponseDto;
import com.orama.e_commerce.dtos.auth.LoginRequestDto;
import com.orama.e_commerce.dtos.client.ClientRequestDto;
import com.orama.e_commerce.dtos.client.ClientResponseDto;
import com.orama.e_commerce.models.Client;
import com.orama.e_commerce.repository.ClientRepository;
import com.orama.e_commerce.security.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

  private final AuthenticationManager authenticationManager;
  private final UserDetailsService userDetailsService;
  private final JwtService jwtService;
  private final ClientService clientService;
  private final ClientRepository clientRepository;

  public AuthService(
      AuthenticationManager authenticationManager,
      UserDetailsService userDetailsService,
      JwtService jwtService,
      ClientService clientService,
      ClientRepository clientRepository) {
    this.authenticationManager = authenticationManager;
    this.userDetailsService = userDetailsService;
    this.jwtService = jwtService;
    this.clientService = clientService;
    this.clientRepository = clientRepository;
  }

  public AuthResponseDto login(LoginRequestDto loginRequest) {
    authenticationManager.authenticate(
        new UsernamePasswordAuthenticationToken(loginRequest.email(), loginRequest.password()));

    Client client =
        clientRepository
            .findByEmail(loginRequest.email())
            .orElseThrow(() -> new RuntimeException("Client not found"));

    UserDetails userDetails = userDetailsService.loadUserByUsername(loginRequest.email());

    String token = jwtService.generateToken(userDetails, client.getId());

    return new AuthResponseDto(token, jwtService.getExpirationTime());
  }

  public AuthRegisterResponseDto register(ClientRequestDto dto) {
    ClientResponseDto createdClientDto = clientService.createClient(dto);

    return new AuthRegisterResponseDto(
            createdClientDto.email(),
            createdClientDto.role().name()
    );
  }
}