package com.mealmesh.cart.dto;

import com.mealmesh.cart.entity.Cart;
import com.mealmesh.restaurant.entity.Restaurant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartResponse {
    private UUID id;
    private UUID restaurantId;
    private String restaurantName;
    private List<CartItemResponse> items;
    private Integer itemCount;
    private BigDecimal subtotal;
    private BigDecimal deliveryFee;
    private BigDecimal taxAmount;
    private BigDecimal discountAmount;
    private BigDecimal platformFee;
    private BigDecimal totalAmount;
    private String couponCode;
    private BigDecimal couponDiscount;
    private BigDecimal minimumOrderAmount;
    private Boolean meetsMinimumOrder;

    public static CartResponse from(Cart cart) {
        List<CartItemResponse> items = cart.getCartItems() != null 
                ? cart.getCartItems().stream().map(CartItemResponse::from).toList() 
                : List.of();

        BigDecimal subtotal = items.stream()
                .map(CartItemResponse::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal deliveryFee = cart.getRestaurant() != null ? cart.getRestaurant().getDeliveryFee() : BigDecimal.ZERO;
        BigDecimal minimumOrder = cart.getRestaurant() != null ? cart.getRestaurant().getMinimumOrderAmount() : BigDecimal.ZERO;
        BigDecimal discount = cart.getCouponDiscount() != null ? cart.getCouponDiscount() : BigDecimal.ZERO;
        BigDecimal total = subtotal.add(deliveryFee).subtract(discount);

        return CartResponse.builder()
                .id(cart.getId())
                .restaurantId(cart.getRestaurant() != null ? cart.getRestaurant().getId() : null)
                .restaurantName(cart.getRestaurant() != null ? cart.getRestaurant().getName() : null)
                .items(items)
                .itemCount(items.stream().mapToInt(CartItemResponse::getQuantity).sum())
                .subtotal(subtotal)
                .deliveryFee(deliveryFee)
                .taxAmount(BigDecimal.ZERO)
                .discountAmount(discount)
                .platformFee(BigDecimal.ZERO)
                .totalAmount(total)
                .couponCode(cart.getCouponCode())
                .couponDiscount(discount)
                .minimumOrderAmount(minimumOrder)
                .meetsMinimumOrder(subtotal.compareTo(minimumOrder) >= 0)
                .build();
    }
}