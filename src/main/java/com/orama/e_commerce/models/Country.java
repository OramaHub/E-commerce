package com.orama.e_commerce.models;

import jakarta.persistence.*;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "country")
public class Country {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "idcountry")
  private Long id;

  @Column(name = "name", nullable = false, length = 150)
  private String name;

  @Column(name = "sgl", length = 10)
  private String abbreviation;

  @OneToMany(mappedBy = "country", fetch = FetchType.LAZY)
  private List<State> states;

  public Country() {}

  public Country(Long id) {
    this.id = id;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
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

  public List<State> getStates() {
    return states;
  }

  public void setStates(List<State> states) {
    this.states = states;
  }

  @Override
  public boolean equals(Object o) {
    return this == o || (o instanceof Country c && Objects.equals(id, c.id));
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(id);
  }

  @Override
  public String toString() {
    return "Country{id=" + id + ", name='" + name + "'}";
  }
}
