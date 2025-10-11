package com.orama.e_commerce;

import jakarta.persistence.*;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "state")
public class State {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "idstate")
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "country_idcountry", nullable = false)
  private Country country;

  @Column(name = "name", nullable = false, length = 150)
  private String name;

  @Column(name = "sgl", length = 10)
  private String abbreviation;

  @OneToMany(mappedBy = "state", fetch = FetchType.LAZY)
  private List<City> cities;

  public State() {}

  public State(Long id) {
    this.id = id;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Country getCountry() {
    return country;
  }

  public void setCountry(Country country) {
    this.country = country;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getAbbreviation() {
    return abbreviation;
  }

  public void setAbbreviation(String abbreviation) {
    this.abbreviation = abbreviation;
  }

  public List<City> getCities() {
    return cities;
  }

  public void setCities(List<City> cities) {
    this.cities = cities;
  }

  @Override
  public boolean equals(Object o) {
    return this == o || (o instanceof State s && Objects.equals(id, s.id));
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(id);
  }

  @Override
  public String toString() {
    return "State{id=" + id + ", name='" + name + "'}";
  }
}
