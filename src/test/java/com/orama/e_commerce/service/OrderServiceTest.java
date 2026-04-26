package com.orama.e_commerce.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import com.orama.e_commerce.dtos.order.CreateOrderRequestDto;
import com.orama.e_commerce.dtos.order.OrderResponseDto;
import com.orama.e_commerce.enums.OrderStatus;
import com.orama.e_commerce.exceptions.cart.CartNotFoundException;
import com.orama.e_commerce.exceptions.order.InvalidDiscountException;
import com.orama.e_commerce.exceptions.order.OrderNotFoundException;
import com.orama.e_commerce.mapper.OrderMapper;
import com.orama.e_commerce.models.*;
import com.orama.e_commerce.repository.AddressRepository;
import com.orama.e_commerce.repository.CartRepository;
import com.orama.e_commerce.repository.OrderRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

  @Mock private OrderRepository orderRepository;
  @Mock private CartRepository cartRepository;
  @Mock private AddressRepository addressRepository;
  @Mock private OrderMapper orderMapper;
  @Mock private ShippingService shippingService;
  @Mock private PaymentApplicationService paymentApplicationService;

  @InjectMocks private OrderService orderService;

  private Cart cart;
  private Client client;
  private Product product;
  private CartItem cartItem;
  private Order order;
  private Address address;
  private OrderResponseDto orderResponseDto;

  @BeforeEach
  void setUp() {
    client = new Client();
    client.setId(1L);
    client.setName("João Silva");

    product = new Product();
    product.setId(1L);
    product.setName("Produto Teste");
    product.setPrice(new BigDecimal("100.00"));

    cartItem = new CartItem();
    cartItem.setId(1L);
    cartItem.setProduct(product);
    cartItem.setQuantity(2);
    cartItem.setUnitPrice(new BigDecimal("100.00"));

    cart = new Cart();
    cart.setId(1L);
    cart.setClient(client);
    cart.setItems(List.of(cartItem));

    address = new Address();
    address.setId(1L);
    address.setStreet("Rua das Flores");
    address.setNumber("123");
    address.setDistrict("Centro");
    address.setZipCode("01310-100");
    address.setCityName("Sao Paulo");
    address.setStateUf("SP");
    address.setCountryCode("BR");

    order = new Order();
    order.setId(1L);
    order.setOrderNumber("ORD-123");
    order.setClient(client);
    order.setCart(cart);
    order.setSubtotal(new BigDecimal("200.00"));
    order.setDiscount(BigDecimal.ZERO);
    order.setTotal(new BigDecimal("200.00"));
    order.setStatus(OrderStatus.PENDING);

    orderResponseDto =
        new OrderResponseDto(
            1L,
            "ORD-123",
            Instant.now(),
            OrderStatus.PENDING,
            new BigDecimal("200.00"),
            BigDecimal.ZERO,
            new BigDecimal("60.00"),
            new BigDecimal("260.00"),
            "01310-100",
            1L,
            "João Silva",
            null,
            null,
            Collections.emptyList(),
            null,
            null,
            null,
            null,
            null,
            null);
  }

  @Test
  void shouldCreateOrder() {
    CreateOrderRequestDto requestDto =
        new CreateOrderRequestDto(1L, BigDecimal.ZERO, "01310-100", 1L);

    when(cartRepository.findById(1L)).thenReturn(Optional.of(cart));
    when(orderMapper.toEntity(requestDto)).thenReturn(order);
    when(orderRepository.existsByOrderNumber(anyString())).thenReturn(false);
    when(orderRepository.save(any(Order.class))).thenReturn(order);
    when(orderMapper.toResponseDto(order)).thenReturn(orderResponseDto);
    when(shippingService.getShippingCost("01310-100")).thenReturn(new BigDecimal("60.00"));
    when(addressRepository.findById(1L)).thenReturn(Optional.of(address));
    when(addressRepository.existsByIdAndClientId(1L, 1L)).thenReturn(true);

    OrderResponseDto result = orderService.createOrder(requestDto);

    assertNotNull(result);
    assertEquals("ORD-123", result.orderNumber());
    verify(orderRepository).save(any(Order.class));
  }

  @Test
  void shouldCreateOrderWithDiscount() {
    CreateOrderRequestDto requestDto =
        new CreateOrderRequestDto(1L, new BigDecimal("50.00"), "01310-100", 1L);

    when(cartRepository.findById(1L)).thenReturn(Optional.of(cart));
    when(orderMapper.toEntity(requestDto)).thenReturn(order);
    when(orderRepository.existsByOrderNumber(anyString())).thenReturn(false);
    when(orderRepository.save(any(Order.class))).thenReturn(order);
    when(orderMapper.toResponseDto(order)).thenReturn(orderResponseDto);
    when(shippingService.getShippingCost("01310-100")).thenReturn(new BigDecimal("60.00"));
    when(addressRepository.findById(1L)).thenReturn(Optional.of(address));
    when(addressRepository.existsByIdAndClientId(1L, 1L)).thenReturn(true);

    OrderResponseDto result = orderService.createOrder(requestDto);

    assertNotNull(result);
    verify(orderRepository).save(any(Order.class));
  }

  @Test
  void shouldThrowCartNotFoundExceptionWhenCartNotFound() {
    CreateOrderRequestDto requestDto =
        new CreateOrderRequestDto(99L, BigDecimal.ZERO, "01310-100", 1L);

    when(cartRepository.findById(99L)).thenReturn(Optional.empty());

    assertThrows(CartNotFoundException.class, () -> orderService.createOrder(requestDto));
  }

  @Test
  void shouldThrowIllegalArgumentExceptionWhenCartIsEmpty() {
    cart.setItems(new ArrayList<>());
    CreateOrderRequestDto requestDto =
        new CreateOrderRequestDto(1L, BigDecimal.ZERO, "01310-100", 1L);

    when(cartRepository.findById(1L)).thenReturn(Optional.of(cart));

    assertThrows(IllegalArgumentException.class, () -> orderService.createOrder(requestDto));
  }

  @Test
  void shouldThrowIllegalArgumentExceptionWhenCartItemsIsNull() {
    cart.setItems(null);
    CreateOrderRequestDto requestDto =
        new CreateOrderRequestDto(1L, BigDecimal.ZERO, "01310-100", 1L);

    when(cartRepository.findById(1L)).thenReturn(Optional.of(cart));

    assertThrows(IllegalArgumentException.class, () -> orderService.createOrder(requestDto));
  }

  @Test
  void shouldThrowInvalidDiscountExceptionWhenDiscountExceedsSubtotal() {
    CreateOrderRequestDto requestDto =
        new CreateOrderRequestDto(1L, new BigDecimal("300.00"), "01310-100", 1L);

    when(cartRepository.findById(1L)).thenReturn(Optional.of(cart));
    when(orderMapper.toEntity(requestDto)).thenReturn(order);

    assertThrows(InvalidDiscountException.class, () -> orderService.createOrder(requestDto));
  }

  @Test
  void shouldThrowIllegalArgumentExceptionWhenAddressNotFound() {
    CreateOrderRequestDto requestDto =
        new CreateOrderRequestDto(1L, BigDecimal.ZERO, "01310-100", 99L);

    when(cartRepository.findById(1L)).thenReturn(Optional.of(cart));
    when(orderMapper.toEntity(requestDto)).thenReturn(order);
    when(orderRepository.existsByOrderNumber(anyString())).thenReturn(false);
    when(shippingService.getShippingCost("01310-100")).thenReturn(new BigDecimal("60.00"));
    when(addressRepository.findById(99L)).thenReturn(Optional.empty());

    assertThrows(IllegalArgumentException.class, () -> orderService.createOrder(requestDto));
  }

  @Test
  void shouldThrowAccessDeniedWhenAddressDoesNotBelongToClient() {
    CreateOrderRequestDto requestDto =
        new CreateOrderRequestDto(1L, BigDecimal.ZERO, "01310-100", 1L);

    when(cartRepository.findById(1L)).thenReturn(Optional.of(cart));
    when(orderMapper.toEntity(requestDto)).thenReturn(order);
    when(orderRepository.existsByOrderNumber(anyString())).thenReturn(false);
    when(shippingService.getShippingCost("01310-100")).thenReturn(new BigDecimal("60.00"));
    when(addressRepository.findById(1L)).thenReturn(Optional.of(address));
    when(addressRepository.existsByIdAndClientId(1L, 1L)).thenReturn(false);

    assertThrows(
        org.springframework.security.access.AccessDeniedException.class,
        () -> orderService.createOrder(requestDto));
  }

  @Test
  void shouldGetOrderById() {
    when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
    when(orderMapper.toResponseDto(order)).thenReturn(orderResponseDto);

    OrderResponseDto result = orderService.getOrderById(1L, null);

    assertNotNull(result);
    assertEquals(1L, result.id());
    verify(orderRepository).findById(1L);
  }

  @Test
  void shouldThrowOrderNotFoundExceptionWhenOrderNotFoundById() {
    when(orderRepository.findById(99L)).thenReturn(Optional.empty());

    assertThrows(OrderNotFoundException.class, () -> orderService.getOrderById(99L, null));
  }

  @Test
  void shouldGetAllOrders() {
    when(orderRepository.findAll()).thenReturn(List.of(order));
    when(orderMapper.toResponseDto(order)).thenReturn(orderResponseDto);

    List<OrderResponseDto> result = orderService.getAllOrders();

    assertNotNull(result);
    assertEquals(1, result.size());
    verify(orderRepository).findAll();
  }

  @Test
  void shouldGetOrdersByClient() {
    when(orderRepository.findByClientId(1L)).thenReturn(List.of(order));
    when(orderMapper.toResponseDto(order)).thenReturn(orderResponseDto);

    List<OrderResponseDto> result = orderService.getOrdersByClient(1L);

    assertNotNull(result);
    assertEquals(1, result.size());
    verify(orderRepository).findByClientId(1L);
  }

  @Test
  void shouldGetOrderByOrderNumber() {
    when(orderRepository.findByOrderNumber("ORD-123")).thenReturn(Optional.of(order));
    when(orderMapper.toResponseDto(order)).thenReturn(orderResponseDto);

    OrderResponseDto result = orderService.getOrderByOrderNumber("ORD-123");

    assertNotNull(result);
    assertEquals("ORD-123", result.orderNumber());
    verify(orderRepository).findByOrderNumber("ORD-123");
  }

  @Test
  void shouldThrowOrderNotFoundExceptionWhenOrderNumberNotFound() {
    when(orderRepository.findByOrderNumber("ORD-INVALID")).thenReturn(Optional.empty());

    assertThrows(
        OrderNotFoundException.class, () -> orderService.getOrderByOrderNumber("ORD-INVALID"));
  }

  @Test
  void shouldUpdateOrderStatus() {
    when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
    when(orderRepository.save(any(Order.class))).thenReturn(order);
    when(orderMapper.toResponseDto(order)).thenReturn(orderResponseDto);

    OrderResponseDto result = orderService.updateOrderStatus(1L, OrderStatus.PROCESSING);

    assertNotNull(result);
    assertEquals(OrderStatus.PROCESSING, order.getStatus());
    verify(orderRepository).save(order);
  }

  @Test
  void shouldThrowOrderNotFoundExceptionWhenUpdatingStatusOfNonExistentOrder() {
    when(orderRepository.findById(99L)).thenReturn(Optional.empty());

    assertThrows(
        OrderNotFoundException.class,
        () -> orderService.updateOrderStatus(99L, OrderStatus.PROCESSING));
  }

  @Test
  void shouldCancelOrder() {
    when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
    when(orderRepository.save(any(Order.class))).thenReturn(order);
    when(orderMapper.toResponseDto(order)).thenReturn(orderResponseDto);

    OrderResponseDto result = orderService.cancelOrder(1L, 1L);

    assertNotNull(result);
    assertEquals(OrderStatus.CANCELLED, order.getStatus());
    verify(orderRepository).save(order);
    verifyNoInteractions(paymentApplicationService);
  }

  @Test
  void shouldCancelOrderWithNullAuthenticatedClientId() {
    when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
    when(orderRepository.save(any(Order.class))).thenReturn(order);
    when(orderMapper.toResponseDto(order)).thenReturn(orderResponseDto);

    OrderResponseDto result = orderService.cancelOrder(1L, null);

    assertNotNull(result);
    assertEquals(OrderStatus.CANCELLED, order.getStatus());
    verify(orderRepository).save(order);
    verifyNoInteractions(paymentApplicationService);
  }

  @Test
  void shouldCancelRemotePaymentBeforeLocalCancel() {
    order.setPaymentId("MP-ORDER-123");
    when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
    when(paymentApplicationService.cancelOrRefundRemotePayment(order))
        .thenReturn(OrderStatus.CANCELLED);
    when(orderRepository.save(any(Order.class))).thenReturn(order);
    when(orderMapper.toResponseDto(order)).thenReturn(orderResponseDto);

    OrderResponseDto result = orderService.cancelOrder(1L, 1L);

    assertNotNull(result);
    assertEquals(OrderStatus.CANCELLED, order.getStatus());
    verify(paymentApplicationService).cancelOrRefundRemotePayment(order);
    verify(orderRepository).save(order);
  }

  @Test
  void shouldRefundRemotePaymentBeforeLocalRefund() {
    order.setPaymentId("MP-ORDER-123");
    order.setStatus(OrderStatus.PAYMENT_CONFIRMED);
    when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
    when(paymentApplicationService.cancelOrRefundRemotePayment(order))
        .thenReturn(OrderStatus.REFUNDED);
    when(orderRepository.save(any(Order.class))).thenReturn(order);
    when(orderMapper.toResponseDto(order)).thenReturn(orderResponseDto);

    OrderResponseDto result = orderService.cancelOrder(1L, 1L);

    assertNotNull(result);
    assertEquals(OrderStatus.REFUNDED, order.getStatus());
    verify(paymentApplicationService).cancelOrRefundRemotePayment(order);
    verify(orderRepository).save(order);
  }

  @Test
  void shouldNotCallRemoteAgainWhenOrderAlreadyRefunded() {
    order.setPaymentId("MP-ORDER-123");
    order.setStatus(OrderStatus.REFUNDED);
    when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
    when(orderMapper.toResponseDto(order)).thenReturn(orderResponseDto);

    OrderResponseDto result = orderService.cancelOrder(1L, 1L);

    assertNotNull(result);
    verifyNoInteractions(paymentApplicationService);
    verify(orderRepository, never()).save(any(Order.class));
  }

  @Test
  void shouldThrowAccessDeniedWhenCancellingOrderOfAnotherClient() {
    when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

    assertThrows(
        org.springframework.security.access.AccessDeniedException.class,
        () -> orderService.cancelOrder(1L, 999L));
  }

  @Test
  void shouldThrowOrderNotFoundExceptionWhenCancellingNonExistentOrder() {
    when(orderRepository.findById(99L)).thenReturn(Optional.empty());

    assertThrows(OrderNotFoundException.class, () -> orderService.cancelOrder(99L, 1L));
  }
}
