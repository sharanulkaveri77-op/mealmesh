package com.mealmesh.cart.dto;

import com.mealmesh.cart.entity.CartItem;
import com.mealmesh.menu.entity.MenuItem;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartItemResponse {
    private UUID id;
    private UUID menuItemId;
    private String menuItemName;
    private String menuItemDescription;
    private BigDecimal price;
    private String imageUrl;
    private Boolean isVegetarian;
    private Boolean isAvailable;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal totalPrice;
    private String specialInstructions;

    public static CartItemResponse from(CartItem cartItem) {
        MenuItem menuItem = cartItem.getMenuItem();
        return CartItemResponse.builder()
                .id(cartItem.getId())
                .menuItemId(menuItem.getId())
                .menuItemName(menuItem.getName())
                .menuItemDescription(menuItem.getDescription())
                .price(menuItem.getPrice())
                .imageUrl(menuItem.getImageUrl())
                .isVegetarian(menuItem.getIsVegetarian())
                .isAvailable(menuItem.getIsAvailable())
                .quantity(cartItem.getQuantity())
                .unitPrice(cartItem.getUnitPrice())
                .totalPrice(cartItem.getUnitPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity())))
                .specialInstructions(cartItem.getSpecialInstructions())
                .build();
    }
}