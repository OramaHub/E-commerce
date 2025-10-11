package com.orama.e_commerce.models;

import jakarta.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "ProductImage")
public class ProductImage {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "idProductImage")
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "Product_idProduct", nullable = false)
  private Product product;

  @Column(name = "url", nullable = false, length = 1000)
  private String url;

  public ProductImage() {}

  public ProductImage(Long id) {
    this.id = id;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Product getProduct() {
    return product;
  }

  public void setProduct(Product product) {
    this.product = product;
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  @Override
  public boolean equals(Object o) {
    return this == o || (o instanceof ProductImage pi && Objects.equals(id, pi.id));
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(id);
  }

  @Override
  public String toString() {
    return "ProductImage{id=" + id + ", url='" + url + "'}";
  }
}
