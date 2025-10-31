package com.orama.e_commerce.models;

import com.orama.e_commerce.enums.OrderStatus;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "tb_order")
public class Order {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "order_number", length = 50, unique = true)
  private String orderNumber;

  @CreationTimestamp
  @Column(name = "order_date")
  private Instant orderDate;

  @Enumerated(EnumType.STRING)
  @Column(name = "status_order", length = 20, nullable = false)
  private OrderStatus status;

  @Column(precision = 15, scale = 2)
  private BigDecimal subtotal;

  @Column(precision = 15, scale = 2)
  private BigDecimal discount;

  @Column(precision = 15, scale = 2)
  private BigDecimal total;

  @OneToMany(
      mappedBy = "order",
      fetch = FetchType.LAZY,
      cascade = CascadeType.ALL,
      orphanRemoval = true)
  private List<OrderItem> items;

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "cart_id", nullable = false, unique = true)
  private Cart cart;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "client_id", nullable = false)
  private Client client;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "delivery_adress_id", nullable = false)
  private Address deliveryAddress;

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

  public Instant getOrderDate() {
    return orderDate;
  }

  public void setOrderDate(Instant orderDate) {
    this.orderDate = orderDate;
  }

  public OrderStatus getStatus() {
    return status;
  }

  public void setStatus(OrderStatus status) {
    this.status = status;
  }

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

  public Client getClient() {
    return client;
  }

  public void setClient(Client client) {
    this.client = client;
  }

  public Address getDeliveryAddress() {
    return deliveryAddress;
  }

  public void setDeliveryAddress(Address deliveryAddress) {
    this.deliveryAddress = deliveryAddress;
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
