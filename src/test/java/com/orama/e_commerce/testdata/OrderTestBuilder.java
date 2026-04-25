package com.orama.e_commerce.testdata;

import com.orama.e_commerce.enums.OrderStatus;
import com.orama.e_commerce.models.Address;
import com.orama.e_commerce.models.Client;
import com.orama.e_commerce.models.Order;
import com.orama.e_commerce.models.OrderItem;
import com.orama.e_commerce.models.Product;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class OrderTestBuilder {

  private Long id = 1L;
  private String orderNumber = "ORD-2026-0001";
  private OrderStatus status = OrderStatus.PENDING;
  private BigDecimal total = new BigDecimal("100.00");
  private Client client;
  private Address deliveryAddress;
  private boolean deliveryAddressExplicitlySet = false;
  private List<OrderItem> items = new ArrayList<>();

  public static OrderTestBuilder anOrder() {
    return new OrderTestBuilder();
  }

  public OrderTestBuilder withId(Long id) {
    this.id = id;
    return this;
  }

  public OrderTestBuilder withOrderNumber(String orderNumber) {
    this.orderNumber = orderNumber;
    return this;
  }

  public OrderTestBuilder withStatus(OrderStatus status) {
    this.status = status;
    return this;
  }

  public OrderTestBuilder withTotal(BigDecimal total) {
    this.total = total;
    return this;
  }

  public OrderTestBuilder withClient(Client client) {
    this.client = client;
    return this;
  }

  public OrderTestBuilder withDeliveryAddress(Address address) {
    this.deliveryAddress = address;
    this.deliveryAddressExplicitlySet = true;
    return this;
  }

  public OrderTestBuilder withoutDeliveryAddress() {
    this.deliveryAddress = null;
    this.deliveryAddressExplicitlySet = true;
    return this;
  }

  public OrderTestBuilder withItem(String productName, BigDecimal unitPrice, int quantity) {
    Product product = new Product((long) (items.size() + 1));
    product.setName(productName);
    product.setPrice(unitPrice);

    OrderItem item = new OrderItem();
    item.setProduct(product);
    item.setUnitPrice(unitPrice);
    item.setQuantity(quantity);
    items.add(item);
    return this;
  }

  public Order build() {
    if (client == null) {
      client = ClientTestBuilder.aClient().build();
    }
    if (items.isEmpty()) {
      withItem("Produto Padrao", new BigDecimal("100.00"), 1);
    }
    if (!deliveryAddressExplicitlySet) {
      deliveryAddress = AddressTestBuilder.anAddress().build();
    }
    Order order = new Order(id);
    order.setOrderNumber(orderNumber);
    order.setStatus(status);
    order.setTotal(total);
    order.setClient(client);
    order.setDeliveryAddress(deliveryAddress);
    order.setItems(items);
    return order;
  }
}
