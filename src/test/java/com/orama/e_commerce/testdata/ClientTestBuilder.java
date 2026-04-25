package com.orama.e_commerce.testdata;

import com.orama.e_commerce.models.Address;
import com.orama.e_commerce.models.Client;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ClientTestBuilder {

  private Long id = 100L;
  private String name = "Joao Silva";
  private String email = "joao@example.com";
  private String cpf = "123.456.789-00";
  private List<Address> addresses = new ArrayList<>();

  public static ClientTestBuilder aClient() {
    return new ClientTestBuilder();
  }

  public ClientTestBuilder withId(Long id) {
    this.id = id;
    return this;
  }

  public ClientTestBuilder withName(String name) {
    this.name = name;
    return this;
  }

  public ClientTestBuilder withEmail(String email) {
    this.email = email;
    return this;
  }

  public ClientTestBuilder withCpf(String cpf) {
    this.cpf = cpf;
    return this;
  }

  public ClientTestBuilder withAddresses(Address... addresses) {
    this.addresses = new ArrayList<>(Arrays.asList(addresses));
    return this;
  }

  public ClientTestBuilder withoutAddresses() {
    this.addresses = new ArrayList<>();
    return this;
  }

  public Client build() {
    Client client = new Client();
    client.setId(id);
    client.setName(name);
    client.setEmail(email);
    client.setCpf(cpf);
    client.setAddresses(addresses);
    return client;
  }
}
