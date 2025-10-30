package com.orama.e_commerce.service;

import com.orama.e_commerce.dtos.cart.AddItemToCartRequestDto;
import com.orama.e_commerce.dtos.cart.CartResponseDto;
import com.orama.e_commerce.dtos.cart.UpdateCartItemRequestDto;
import com.orama.e_commerce.exceptions.cart.CartNotFoundException;
import com.orama.e_commerce.exceptions.product.ProductNotFoundException;
import com.orama.e_commerce.mapper.CartMapper;
import com.orama.e_commerce.models.Cart;
import com.orama.e_commerce.models.CartItem;
import com.orama.e_commerce.models.Client;
import com.orama.e_commerce.models.Product;
import com.orama.e_commerce.repository.CartItemRepository;
import com.orama.e_commerce.repository.CartRepository;
import com.orama.e_commerce.repository.ProductRepository;
import jakarta.transaction.Transactional;
import java.util.ArrayList;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class CartService {

  private final CartRepository cartRepository;
  private final CartItemRepository cartItemRepository;
  private final ProductRepository productRepository;
  private final CartMapper cartMapper;
  private final ClientService clientService;

  public CartService(
      CartRepository cartRepository,
      CartItemRepository cartItemRepository,
      ProductRepository productRepository,
      CartMapper cartMapper,
      ClientService clientService) {
    this.cartRepository = cartRepository;
    this.cartItemRepository = cartItemRepository;
    this.productRepository = productRepository;
    this.cartMapper = cartMapper;
    this.clientService = clientService;
  }

  @Transactional
  public CartResponseDto getOrCreateActiveCart(Long clientId) {
    Client client = clientService.findById(clientId);

    Cart cart =
        cartRepository.findActiveCartByClientId(clientId).orElseGet(() -> createNewCart(client));

    return cartMapper.toResponseDto(cart);
  }

  public CartResponseDto getCartById(Long cartId) {
    Cart cart =
        cartRepository
            .findById(cartId)
            .orElseThrow(() -> new CartNotFoundException("Cart not found with id: " + cartId));

    return cartMapper.toResponseDto(cart);
  }

  @Transactional
  public CartResponseDto addItemToCart(Long clientId, AddItemToCartRequestDto dto) {
    Cart cart =
        cartRepository
            .findActiveCartByClientId(clientId)
            .orElseGet(() -> createNewCart(clientService.findById(clientId)));

    Product product =
        productRepository
            .findById(dto.productId())
            .orElseThrow(
                () ->
                    new ProductNotFoundException("Product not found with id: " + dto.productId()));

    if (!product.getActive()) {
      throw new IllegalArgumentException("Product is not active");
    }

    if (product.getStock() < dto.quantity()) {
      throw new IllegalArgumentException("Insufficient stock. Available: " + product.getStock());
    }

    CartItem existingItem =
        cartItemRepository.findByCartIdAndProductId(cart.getId(), product.getId()).orElse(null);

    if (existingItem != null) {
      int newQuantity = existingItem.getQuantity() + dto.quantity();
      if (product.getStock() < newQuantity) {
        throw new IllegalArgumentException("Insufficient stock. Available: " + product.getStock());
      }
      existingItem.setQuantity(newQuantity);
      cartItemRepository.save(existingItem);
    } else {
      CartItem newItem = new CartItem();
      newItem.setCart(cart);
      newItem.setProduct(product);
      newItem.setQuantity(dto.quantity());
      newItem.setUnitPrice(product.getPrice());

      if (cart.getItems() == null) {
        cart.setItems(new ArrayList<>());
      }
      cart.getItems().add(newItem);
      cartItemRepository.save(newItem);
    }

    Cart savedCart = cartRepository.save(cart);
    return cartMapper.toResponseDto(savedCart);
  }

  @Transactional
  public CartResponseDto updateCartItemQuantity(
      Long clientId, Long cartItemId, UpdateCartItemRequestDto dto) {
    CartItem cartItem = findAndValidateCartItem(clientId, cartItemId);

    Product product = cartItem.getProduct();
    if (product.getStock() < dto.quantity()) {
      throw new IllegalArgumentException("Insufficient stock. Available: " + product.getStock());
    }

    cartItem.setQuantity(dto.quantity());
    cartItemRepository.save(cartItem);

    Cart savedCart = cartRepository.save(cartItem.getCart());
    return cartMapper.toResponseDto(savedCart);
  }

  @Transactional
  public CartResponseDto removeItemFromCart(Long clientId, Long cartItemId) {
    CartItem cartItem = findAndValidateCartItem(clientId, cartItemId);
    Cart cart = cartItem.getCart();

    cart.getItems().remove(cartItem);
    cartItemRepository.delete(cartItem);

    Cart savedCart = cartRepository.save(cart);
    return cartMapper.toResponseDto(savedCart);
  }

  @Transactional
  public void clearCart(Long clientId) {
    Cart cart =
        cartRepository
            .findActiveCartByClientId(clientId)
            .orElseThrow(() -> new CartNotFoundException("No active cart found for client"));

    if (cart.getItems() != null) {
      cartItemRepository.deleteAll(cart.getItems());
      cart.getItems().clear();
    }

    cartRepository.save(cart);
  }

  private CartItem findAndValidateCartItem(Long clientId, Long cartItemId) {
    Cart cart =
        cartRepository
            .findActiveCartByClientId(clientId)
            .orElseThrow(() -> new CartNotFoundException("No active cart found for client"));

    CartItem cartItem =
        cartItemRepository
            .findById(cartItemId)
            .orElseThrow(
                () -> new IllegalArgumentException("Cart item not found with id: " + cartItemId));

    if (!cartItem.getCart().getId().equals(cart.getId())) {
      throw new IllegalArgumentException("Cart item does not belong to this cart");
    }

    return cartItem;
  }

  private Cart createNewCart(Client client) {
    Cart newCart = new Cart();
    newCart.setClient(client);
    newCart.setSessionId(UUID.randomUUID().toString());
    newCart.setItems(new ArrayList<>());
    return cartRepository.save(newCart);
  }
}
