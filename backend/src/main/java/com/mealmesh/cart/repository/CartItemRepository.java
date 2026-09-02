package com.mealmesh.cart.repository;

import com.mealmesh.cart.entity.Cart;
import com.mealmesh.cart.entity.CartItem;
import com.mealmesh.menu.entity.MenuItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, UUID> {
    Optional<CartItem> findByCartAndMenuItem(Cart cart, MenuItem menuItem);
}