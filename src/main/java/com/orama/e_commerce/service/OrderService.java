package com.orama.e_commerce.service;

import com.orama.e_commerce.dtos.order.CreateOrderRequestDto;
import com.orama.e_commerce.dtos.order.OrderResponseDto;
import com.orama.e_commerce.enums.OrderStatus;
import com.orama.e_commerce.exceptions.cart.CartNotFoundException;
import com.orama.e_commerce.exceptions.order.InvalidDiscountException;
import com.orama.e_commerce.exceptions.order.OrderNotFoundException;
import com.orama.e_commerce.mapper.OrderMapper;
import com.orama.e_commerce.models.Cart;
import com.orama.e_commerce.models.CartItem;
import com.orama.e_commerce.models.Order;
import com.orama.e_commerce.models.OrderItem;
import com.orama.e_commerce.repository.CartRepository;
import com.orama.e_commerce.repository.OrderRepository;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class OrderService {

  private final OrderRepository orderRepository;
  private final CartRepository cartRepository;
  private final OrderMapper orderMapper;
  private final ShippingService shippingService;

  public OrderService(
      OrderRepository orderRepository,
      CartRepository cartRepository,
      OrderMapper orderMapper,
      ShippingService shippingService) {
    this.orderRepository = orderRepository;
    this.cartRepository = cartRepository;
    this.orderMapper = orderMapper;
    this.shippingService = shippingService;
  }

  @Transactional
  public OrderResponseDto createOrder(CreateOrderRequestDto dto) {
    Cart cart =
        cartRepository
            .findById(dto.cartId())
            .orElseThrow(
                () -> new CartNotFoundException("Carrinho não encontrado com id: " + dto.cartId()));

    if (cart.getItems() == null || cart.getItems().isEmpty()) {
      throw new IllegalArgumentException(
          "Não é possível criar pedido a partir de um carrinho vazio");
    }

    Order order = orderMapper.toEntity(dto);

    order.setCart(cart);
    order.setClient(cart.getClient());
    order.setOrderNumber(generateOrderNumber());
    order.setStatus(OrderStatus.PENDING);

    BigDecimal subtotal = calculateSubtotal(cart.getItems());
    order.setSubtotal(subtotal);

    BigDecimal discount = dto.discount() != null ? dto.discount() : BigDecimal.ZERO;
    if (discount.compareTo(subtotal) > 0) {
      throw new InvalidDiscountException("Desconto não pode exceder o subtotal");
    }
    order.setDiscount(discount);

    BigDecimal shippingCost = shippingService.getShippingCost(dto.zipCode());
    order.setShippingCost(shippingCost);
    order.setZipCode(dto.zipCode());

    order.setTotal(subtotal.subtract(discount).add(shippingCost));

    List<OrderItem> orderItems = createOrderItems(cart.getItems(), order);
    order.setItems(orderItems);

    Order savedOrder = orderRepository.save(order);

    return orderMapper.toResponseDto(savedOrder);
  }

  public OrderResponseDto getOrderById(Long id, Long requestingClientId) {
    Order order =
        orderRepository
            .findById(id)
            .orElseThrow(() -> new OrderNotFoundException("Pedido não encontrado com id: " + id));

    if (requestingClientId != null && !order.getClient().getId().equals(requestingClientId)) {
      throw new org.springframework.security.access.AccessDeniedException(
          "Acesso negado ao pedido.");
    }

    return orderMapper.toResponseDto(order);
  }

  public List<OrderResponseDto> getAllOrders() {
    List<Order> orders = orderRepository.findAll();
    return orders.stream().map(orderMapper::toResponseDto).toList();
  }

  public List<OrderResponseDto> getOrdersByClient(Long clientId) {
    List<Order> orders = orderRepository.findByClientId(clientId);
    return orders.stream().map(orderMapper::toResponseDto).toList();
  }

  public OrderResponseDto getOrderByOrderNumber(String orderNumber) {
    Order order =
        orderRepository
            .findByOrderNumber(orderNumber)
            .orElseThrow(
                () ->
                    new OrderNotFoundException("Pedido não encontrado com número: " + orderNumber));

    return orderMapper.toResponseDto(order);
  }

  @Transactional
  public OrderResponseDto updateOrderStatus(Long id, OrderStatus newStatus) {
    Order order =
        orderRepository
            .findById(id)
            .orElseThrow(() -> new OrderNotFoundException("Pedido não encontrado com id: " + id));

    order.setStatus(newStatus);
    Order updatedOrder = orderRepository.save(order);

    return orderMapper.toResponseDto(updatedOrder);
  }

  @Transactional
  public OrderResponseDto cancelOrder(Long id, Long authenticatedClientId) {
    Order order =
        orderRepository
            .findById(id)
            .orElseThrow(() -> new OrderNotFoundException("Pedido não encontrado com id: " + id));

    if (authenticatedClientId != null && !order.getClient().getId().equals(authenticatedClientId)) {
      throw new org.springframework.security.access.AccessDeniedException(
          "Você não tem permissão para cancelar este pedido");
    }

    order.setStatus(OrderStatus.CANCELLED);
    Order cancelledOrder = orderRepository.save(order);

    return orderMapper.toResponseDto(cancelledOrder);
  }

  private String generateOrderNumber() {
    String prefix = "ORD-" + Instant.now() + "-";
    String uniqueId = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    String orderNumber = prefix + uniqueId;

    while (orderRepository.existsByOrderNumber(orderNumber)) {
      uniqueId = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
      orderNumber = prefix + uniqueId;
    }

    return orderNumber;
  }

  private BigDecimal calculateSubtotal(List<CartItem> cartItems) {
    return cartItems.stream()
        .map(item -> item.getProduct().getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
        .reduce(BigDecimal.ZERO, BigDecimal::add);
  }

  private List<OrderItem> createOrderItems(List<CartItem> cartItems, Order order) {
    return cartItems.stream()
        .map(
            cartItem -> {
              OrderItem orderItem = new OrderItem();
              orderItem.setOrder(order);
              orderItem.setProduct(cartItem.getProduct());
              orderItem.setQuantity(cartItem.getQuantity());
              orderItem.setUnitPrice(cartItem.getProduct().getPrice());
              return orderItem;
            })
        .toList();
  }
}
