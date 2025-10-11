package com.orama.e_commerce.models;

import com.orama.e_commerce.enums.UserRole;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "Client")
public class Client {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "idClient")
  private Long id;

  @Column(name = "Name", nullable = false, length = 150)
  private String name;

  @Column(name = "email", length = 180, unique = true)
  private String email;

  @Column(name = "password_hash", nullable = false, length = 255)
  private String passwordHash;

  @Column(name = "cpf", length = 20)
  private String cpf;

  @Column(name = "phone", length = 30)
  private String phone;

  @Column(name = "active")
  private Boolean active;

  @Column(name = "created_at")
  private LocalDate createdAt;

  @Enumerated(EnumType.STRING)
  private UserRole role;

  @OneToMany(mappedBy = "client", fetch = FetchType.LAZY)
  private List<Address> addresses;

  @OneToMany(mappedBy = "client", fetch = FetchType.LAZY)
  private List<Cart> carts;

  public Client() {}

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

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getPasswordHash() {
    return passwordHash;
  }

  public void setPasswordHash(String passwordHash) {
    this.passwordHash = passwordHash;
  }

  public String getCpf() {
    return cpf;
  }

  public void setCpf(String cpf) {
    this.cpf = cpf;
  }

  public String getPhone() {
    return phone;
  }

  public void setPhone(String phone) {
    this.phone = phone;
  }

  public Boolean getActive() {
    return active;
  }

  public void setActive(Boolean active) {
    this.active = active;
  }

  public LocalDate getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(LocalDate createdAt) {
    this.createdAt = createdAt;
  }

  public UserRole getRole() {
    return role;
  }

  public void setRole(UserRole role) {
    this.role = role;
  }

  public List<Address> getAddresses() {
    return addresses;
  }

  public void setAddresses(List<Address> addresses) {
    this.addresses = addresses;
  }

  public List<Cart> getCarts() {
    return carts;
  }

  public void setCarts(List<Cart> carts) {
    this.carts = carts;
  }

  @Override
  public boolean equals(Object o) {
    return this == o || (o instanceof Client c && Objects.equals(id, c.id));
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(id);
  }

  @Override
  public String toString() {
    return "Client{id=" + id + ", name='" + name + "'}";
  }
}
