package com.orama.e_commerce;

import jakarta.persistence.*;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "City")
public class City {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "idCity")
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "state_idstate", nullable = false)
  private State state;

  @Column(name = "name", nullable = false, length = 150)
  private String name;

  @OneToMany(mappedBy = "city", fetch = FetchType.LAZY)
  private List<Client> clients;

  public City() {}

  public City(Long id) {
    this.id = id;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public State getState() {
    return state;
  }

  public void setState(State state) {
    this.state = state;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public List<Client> getClients() {
    return clients;
  }

  public void setClients(List<Client> clients) {
    this.clients = clients;
  }

  @Override
  public boolean equals(Object o) {
    return this == o || (o instanceof City c && Objects.equals(id, c.id));
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(id);
  }

  @Override
  public String toString() {
    return "City{id=" + id + ", name='" + name + "'}";
  }
}
