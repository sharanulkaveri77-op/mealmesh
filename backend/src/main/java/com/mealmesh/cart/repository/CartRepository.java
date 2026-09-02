package com.mealmesh.cart.repository;

import com.mealmesh.cart.entity.Cart;
import com.mealmesh.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CartRepository extends JpaRepository<Cart, UUID> {
    Optional<Cart> findByUser(User user);
    void deleteByUser(User user);
}