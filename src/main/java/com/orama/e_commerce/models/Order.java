package com.orama.e_commerce.models;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "Order_cart")
public class Order {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "idOrder")
  private Long id;

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "Cart_idCart", nullable = false, unique = true)
  private Cart cart;

  @Column(name = "orderNumber", length = 50, unique = true)
  private String orderNumber;

  @Column(name = "orderDate")
  private LocalDate orderDate;

  //  @Enumerated(EnumType.STRING)
  //  @Column(name = "status_order", length = 20)
  /// /  private OrderStatus status;

  @Column(name = "subtotal", precision = 15, scale = 2)
  private BigDecimal subtotal;

  @Column(name = "discount", precision = 15, scale = 2)
  private BigDecimal discount;

  @Column(name = "total", precision = 15, scale = 2)
  private BigDecimal total;

  @OneToMany(
      mappedBy = "order",
      fetch = FetchType.LAZY,
      cascade = CascadeType.ALL,
      orphanRemoval = true)
  private List<OrderItem> items;

  public Order() {}

  public Order(Long id) {
    this.id = id;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Cart getCart() {
    return cart;
  }

  public void setCart(Cart cart) {
    this.cart = cart;
  }

  public String getOrderNumber() {
    return orderNumber;
  }

  public void setOrderNumber(String orderNumber) {
    this.orderNumber = orderNumber;
  }

  public LocalDate getOrderDate() {
    return orderDate;
  }

  public void setOrderDate(LocalDate orderDate) {
    this.orderDate = orderDate;
  }

  //  public OrderStatus getStatus() {
  //    return status;
  //  }
  //
  //  public void setStatus(OrderStatus status) {
  //    this.status = status;
  //  }

  public BigDecimal getSubtotal() {
    return subtotal;
  }

  public void setSubtotal(BigDecimal subtotal) {
    this.subtotal = subtotal;
  }

  public BigDecimal getDiscount() {
    return discount;
  }

  public void setDiscount(BigDecimal discount) {
    this.discount = discount;
  }

  public BigDecimal getTotal() {
    return total;
  }

  public void setTotal(BigDecimal total) {
    this.total = total;
  }

  public List<OrderItem> getItems() {
    return items;
  }

  public void setItems(List<OrderItem> items) {
    this.items = items;
  }

  @Override
  public boolean equals(Object o) {
    return this == o || (o instanceof Order ord && Objects.equals(id, ord.id));
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(id);
  }

  @Override
  public String toString() {
    return "Order{id=" + id + ", number='" + orderNumber + "'}";
  }
}
