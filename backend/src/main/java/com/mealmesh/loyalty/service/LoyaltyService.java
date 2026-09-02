package com.mealmesh.loyalty.service;

import com.mealmesh.common.exception.BadRequestException;
import com.mealmesh.common.exception.ResourceNotFoundException;
import com.mealmesh.loyalty.dto.LoyaltyResponse;
import com.mealmesh.loyalty.dto.RedeemPointsRequest;
import com.mealmesh.loyalty.entity.LoyaltyAccount;
import com.mealmesh.loyalty.entity.LoyaltyTier;
import com.mealmesh.loyalty.repository.LoyaltyAccountRepository;
import com.mealmesh.outbox.service.OutboxService;
import com.mealmesh.user.entity.User;
import com.mealmesh.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class LoyaltyService {

    private final LoyaltyAccountRepository loyaltyRepository;
    private final UserRepository userRepository;
    private final OutboxService outboxService;

    @Transactional
    public LoyaltyAccount getOrCreateAccount(User user) {
        return loyaltyRepository.findByUser(user).orElseGet(() -> {
            LoyaltyAccount account = LoyaltyAccount.builder()
                    .user(user)
                    .pointsBalance(100) // 100 welcome bonus points
                    .lifetimePoints(100)
                    .tier(LoyaltyTier.BRONZE)
                    .build();
            return loyaltyRepository.save(account);
        });
    }

    @Transactional
    public LoyaltyResponse earnPointsOnOrder(UUID userId, BigDecimal orderAmount) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

        LoyaltyAccount account = getOrCreateAccount(user);

        // 1 point per ₹10 spent, multiplied by tier multiplier
        double basePoints = orderAmount.divide(BigDecimal.valueOf(10), 0, RoundingMode.DOWN).doubleValue();
        int earnedPoints = (int) Math.round(basePoints * account.getTier().getMultiplier());

        account.setPointsBalance(account.getPointsBalance() + earnedPoints);
        account.setLifetimePoints(account.getLifetimePoints() + earnedPoints);

        // Recalculate tier
        LoyaltyTier newTier = LoyaltyTier.calculateTier(account.getLifetimePoints());
        if (newTier != account.getTier()) {
            log.info("User {} upgraded to loyalty tier {}", user.getEmail(), newTier);
            account.setTier(newTier);
        }

        account = loyaltyRepository.save(account);

        outboxService.saveEvent("Loyalty", account.getId(), "LoyaltyPointsEarnedEvent", Map.of(
                "userId", user.getId(),
                "earnedPoints", earnedPoints,
                "currentBalance", account.getPointsBalance(),
                "tier", account.getTier().name()
        ));

        return LoyaltyResponse.from(account);
    }

    @Transactional
    public LoyaltyResponse redeemPoints(UUID userId, RedeemPointsRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

        LoyaltyAccount account = getOrCreateAccount(user);

        if (account.getPointsBalance() < request.getPoints()) {
            throw new BadRequestException("Insufficient points balance. Available: " + account.getPointsBalance());
        }

        account.setPointsBalance(account.getPointsBalance() - request.getPoints());
        account = loyaltyRepository.save(account);

        BigDecimal discountRupees = BigDecimal.valueOf(request.getPoints())
                .divide(BigDecimal.valueOf(10), 2, RoundingMode.HALF_UP);

        outboxService.saveEvent("Loyalty", account.getId(), "LoyaltyPointsRedeemedEvent", Map.of(
                "userId", user.getId(),
                "redeemedPoints", request.getPoints(),
                "discountRupees", discountRupees,
                "remainingBalance", account.getPointsBalance()
        ));

        return LoyaltyResponse.from(account);
    }

    @Transactional(readOnly = true)
    public LoyaltyResponse getMyLoyaltyAccount(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

        LoyaltyAccount account = getOrCreateAccount(user);
        return LoyaltyResponse.from(account);
    }
}
