package com.mealmesh.loyalty.repository;

import com.mealmesh.loyalty.entity.LoyaltyAccount;
import com.mealmesh.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface LoyaltyAccountRepository extends JpaRepository<LoyaltyAccount, UUID> {

    Optional<LoyaltyAccount> findByUser(User user);

    Optional<LoyaltyAccount> findByUserId(UUID userId);
}
