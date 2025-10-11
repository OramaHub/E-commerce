package com.orama.e_commerce.models;

import jakarta.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "Address")
public class Address {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "idAddress")
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "client_id", nullable = false)
  private Client client;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "City_idCity", nullable = false)
  private City city;

  @Column(name = "street", nullable = false, length = 255)
  private String street;

  @Column(name = "number", nullable = true, length = 20)
  private String number;

  @Column(name = "complement", length = 100)
  private String complement;

  @Column(name = "district", nullable = false, length = 100)
  private String district;

  @Column(name = "zip_code", nullable = false, length = 20)
  private String zipCode;

  public Address() {}

  public Address(Long id) {
    this.id = id;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Client getClient() {
    return client;
  }

  public void setClient(Client client) {
    this.client = client;
  }

  public City getCity() {
    return city;
  }

  public void setCity(City city) {
    this.city = city;
  }

  public String getStreet() {
    return street;
  }

  public void setStreet(String street) {
    this.street = street;
  }

  public String getNumber() {
    return number;
  }

  public void setNumber(String number) {
    this.number = number;
  }

  public String getComplement() {
    return complement;
  }

  public void setComplement(String complement) {
    this.complement = complement;
  }

  public String getDistrict() {
    return district;
  }

  public void setDistrict(String district) {
    this.district = district;
  }

  public String getZipCode() {
    return zipCode;
  }

  public void setZipCode(String zipCode) {
    this.zipCode = zipCode;
  }

  @Override
  public boolean equals(Object o) {
    return this == o || (o instanceof Address a && Objects.equals(id, a.id));
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(id);
  }

  @Override
  public String toString() {
    return "Address{id=" + id + ", street='" + street + "'}";
  }
}
