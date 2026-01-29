package com.orama.e_commerce.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.orama.e_commerce.dtos.cart.CartResponseDto;
import com.orama.e_commerce.mapper.CartMapper;
import com.orama.e_commerce.models.Cart;
import com.orama.e_commerce.models.Client;
import com.orama.e_commerce.repository.CartItemRepository;
import com.orama.e_commerce.repository.CartRepository;
import com.orama.e_commerce.repository.ProductRepository;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

  @Mock private CartRepository cartRepository;
  @Mock private CartItemRepository cartItemRepository;
  @Mock private ProductRepository productRepository;
  @Mock private CartMapper cartMapper;
  @Mock private ClientService clientService;

  @InjectMocks private CartService cartService;

  private Client client;
  private Cart cart;
  private CartResponseDto cartResponseDto;

  @BeforeEach
  void setUp() {
    client = new Client();
    client.setId(1L);
    client.setName("João");
    client.setEmail("joao@email.com");

    cart = new Cart();
    cart.setId(1L);
    cart.setClient(client);
    cart.setSessionId("session-123");
    cart.setItems(new ArrayList<>());

    cartResponseDto =
        new CartResponseDto(
            1L, "session-123", 1L, "João", Collections.emptyList(), BigDecimal.ZERO, null, null);
  }

  @Test
  void shouldReturnExistingCartWhenActiveCartExists() {
    when(clientService.findById(1L)).thenReturn(client);
    when(cartRepository.findActiveCartByClientId(1L)).thenReturn(Optional.of(cart));
    when(cartMapper.toResponseDto(cart)).thenReturn(cartResponseDto);

    CartResponseDto result = cartService.getOrCreateActiveCart(1L);

    assertNotNull(result);
    assertEquals(1L, result.id());
    verify(cartRepository, never()).save(any());
  }

  @Test
  void shouldCreateNewCartWhenNoActiveCartExists() {
    when(clientService.findById(1L)).thenReturn(client);
    when(cartRepository.findActiveCartByClientId(1L)).thenReturn(Optional.empty());
    when(cartRepository.save(any(Cart.class))).thenReturn(cart);
    when(cartMapper.toResponseDto(any(Cart.class))).thenReturn(cartResponseDto);

    CartResponseDto result = cartService.getOrCreateActiveCart(1L);

    assertNotNull(result);
    verify(cartRepository).save(any(Cart.class));
  }
}
