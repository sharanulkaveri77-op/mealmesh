package com.mealmesh.cart.service;

import com.mealmesh.cart.dto.CartItemResponse;
import com.mealmesh.cart.dto.CartRequest;
import com.mealmesh.cart.dto.CartResponse;
import com.mealmesh.cart.entity.Cart;
import com.mealmesh.cart.entity.CartItem;
import com.mealmesh.cart.repository.CartItemRepository;
import com.mealmesh.cart.repository.CartRepository;
import com.mealmesh.common.exception.BadRequestException;
import com.mealmesh.common.exception.ResourceNotFoundException;
import com.mealmesh.menu.entity.MenuItem;
import com.mealmesh.menu.repository.MenuItemRepository;
import com.mealmesh.restaurant.entity.Restaurant;
import com.mealmesh.restaurant.repository.RestaurantRepository;
import com.mealmesh.user.entity.User;
import com.mealmesh.user.repository.UserRepository;
import com.mealmesh.coupon.service.CouponService;
import com.mealmesh.coupon.dto.CouponValidateResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final MenuItemRepository menuItemRepository;
    private final RestaurantRepository restaurantRepository;
    private final UserRepository userRepository;
    private final CouponService couponService;

    @Transactional(readOnly = true)
    public CartResponse getCart(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Cart cart = cartRepository.findByUser(user).orElse(null);
        if (cart == null) {
            return CartResponse.builder()
                    .items(List.of())
                    .itemCount(0)
                    .subtotal(BigDecimal.ZERO)
                    .deliveryFee(BigDecimal.ZERO)
                    .totalAmount(BigDecimal.ZERO)
                    .meetsMinimumOrder(true)
                    .build();
        }
        return CartResponse.from(cart);
    }

    @Transactional
    public CartItemResponse addItem(UUID userId, CartRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        MenuItem menuItem = menuItemRepository.findById(request.getMenuItemId())
                .orElseThrow(() -> new ResourceNotFoundException("Menu item not found"));

        if (!menuItem.getIsAvailable()) {
            throw new BadRequestException("Menu item is not available");
        }

        Restaurant menuItemRestaurant = menuItem.getRestaurant();
        if (!menuItemRestaurant.getIsActive() || !menuItemRestaurant.getIsAcceptingOrders()) {
            throw new BadRequestException("Restaurant is not accepting orders");
        }

        Cart cart = getOrCreateCart(user);

        if (cart.getRestaurant() != null && !cart.getRestaurant().getId().equals(menuItemRestaurant.getId())) {
            throw new BadRequestException("Cart contains items from another restaurant. Please clear cart first.");
        }

        cart.setRestaurant(menuItemRestaurant);

        CartItem existingItem = cartItemRepository.findByCartAndMenuItem(cart, menuItem).orElse(null);

        CartItem cartItem;
        if (existingItem != null) {
            existingItem.setQuantity(existingItem.getQuantity() + request.getQuantity());
            existingItem.setSpecialInstructions(request.getSpecialInstructions());
            cartItem = cartItemRepository.save(existingItem);
        } else {
            cartItem = CartItem.builder()
                    .cart(cart)
                    .menuItem(menuItem)
                    .quantity(request.getQuantity())
                    .unitPrice(menuItem.getPrice())
                    .specialInstructions(request.getSpecialInstructions())
                    .build();
            cartItem = cartItemRepository.save(cartItem);
        }

        cartRepository.save(cart);
        return CartItemResponse.from(cartItem);
    }

    @Transactional
    public CartItemResponse updateQuantity(UUID userId, UUID itemId, Integer quantity) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Cart cart = cartRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found"));

        CartItem cartItem = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found"));

        if (!cartItem.getCart().getId().equals(cart.getId())) {
            throw new ResourceNotFoundException("Cart item not found");
        }

        if (quantity <= 0) {
            cartItemRepository.delete(cartItem);
            if (cart.getCartItems().isEmpty()) {
                cart.setRestaurant(null);
                cart.setCouponCode(null);
                cart.setCouponDiscount(BigDecimal.ZERO);
            }
            cartRepository.save(cart);
            return null;
        }

        cartItem.setQuantity(quantity);
        cartItem = cartItemRepository.save(cartItem);
        return CartItemResponse.from(cartItem);
    }

    @Transactional
    public void removeItem(UUID userId, UUID itemId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Cart cart = cartRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found"));

        CartItem cartItem = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found"));

        if (!cartItem.getCart().getId().equals(cart.getId())) {
            throw new ResourceNotFoundException("Cart item not found");
        }

        cartItemRepository.delete(cartItem);

        if (cart.getCartItems().isEmpty()) {
            cart.setRestaurant(null);
            cart.setCouponCode(null);
            cart.setCouponDiscount(BigDecimal.ZERO);
        }
        cartRepository.save(cart);
    }

    @Transactional
    public void clearCart(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Cart cart = cartRepository.findByUser(user).orElse(null);
        if (cart != null) {
            cartItemRepository.deleteAll(cart.getCartItems());
            cart.setRestaurant(null);
            cart.setCouponCode(null);
            cart.setCouponDiscount(BigDecimal.ZERO);
            cartRepository.save(cart);
        }
    }

    @Transactional
    public CartResponse applyCoupon(UUID userId, String couponCode) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Cart cart = cartRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found"));

        if (cart.getCartItems() == null || cart.getCartItems().isEmpty()) {
            throw new BadRequestException("Cannot apply coupon to an empty cart");
        }

        BigDecimal subtotal = cart.getCartItems().stream()
                .map(item -> item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        UUID restaurantId = cart.getRestaurant() != null ? cart.getRestaurant().getId() : null;

        CouponValidateResponse validation = couponService.validateAndCalculateDiscount(
                userId, couponCode, restaurantId, subtotal);

        if (!validation.isValid()) {
            throw new BadRequestException(validation.getMessage());
        }

        cart.setCouponCode(validation.getCoupon().getCode());
        cart.setCouponDiscount(validation.getDiscountAmount());
        cartRepository.save(cart);
        log.info("Coupon {} applied to cart for user {}, discount: {}",
                validation.getCoupon().getCode(), userId, validation.getDiscountAmount());
        return CartResponse.from(cart);
    }

    @Transactional
    public CartResponse removeCoupon(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Cart cart = cartRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found"));

        cart.setCouponCode(null);
        cart.setCouponDiscount(BigDecimal.ZERO);
        cartRepository.save(cart);
        return CartResponse.from(cart);
    }

    private Cart getOrCreateCart(User user) {
        return cartRepository.findByUser(user)
                .orElseGet(() -> {
                    Cart newCart = Cart.builder()
                            .user(user)
                            .couponDiscount(BigDecimal.ZERO)
                            .build();
                    return cartRepository.save(newCart);
                });
    }
}