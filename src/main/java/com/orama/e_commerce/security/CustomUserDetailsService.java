package com.orama.e_commerce.security;

import com.orama.e_commerce.models.Client;
import com.orama.e_commerce.repository.ClientRepository;
import java.util.ArrayList;
import java.util.List;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

  private final ClientRepository clientRepository;

  public CustomUserDetailsService(ClientRepository clientRepository) {
    this.clientRepository = clientRepository;
  }

  @Override
  public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
    Client client =
        clientRepository
            .findByEmail(email)
            .orElseThrow(
                () -> new UsernameNotFoundException("Usuário não encontrado com email: " + email));

    if (!client.getActive()) {
      throw new UsernameNotFoundException("Conta do usuário está inativa");
    }

    List<GrantedAuthority> authorities = new ArrayList<>();
    authorities.add(new SimpleGrantedAuthority("ROLE_" + client.getRole().name()));

    return new User(client.getEmail(), client.getPasswordHash(), authorities);
  }
}
