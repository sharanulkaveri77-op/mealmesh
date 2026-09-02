package com.mealmesh.loyalty.service;

import com.mealmesh.common.exception.BadRequestException;
import com.mealmesh.loyalty.dto.LoyaltyResponse;
import com.mealmesh.loyalty.dto.RedeemPointsRequest;
import com.mealmesh.loyalty.entity.LoyaltyAccount;
import com.mealmesh.loyalty.entity.LoyaltyTier;
import com.mealmesh.loyalty.repository.LoyaltyAccountRepository;
import com.mealmesh.outbox.service.OutboxService;
import com.mealmesh.user.entity.User;
import com.mealmesh.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoyaltyServiceTest {

    @Mock
    private LoyaltyAccountRepository loyaltyRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private OutboxService outboxService;

    @InjectMocks
    private LoyaltyService loyaltyService;

    private UUID userId;
    private User user;
    private LoyaltyAccount account;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        user = User.builder()
                .id(userId)
                .name("Alice")
                .email("alice@mealmesh.com")
                .build();

        account = LoyaltyAccount.builder()
                .id(UUID.randomUUID())
                .user(user)
                .pointsBalance(200)
                .lifetimePoints(450)
                .tier(LoyaltyTier.BRONZE)
                .build();
    }

    @Test
    @DisplayName("earnPointsOnOrder should credit points and upgrade tier when threshold passed")
    void earnPoints_tierUpgrade() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(loyaltyRepository.findByUser(user)).thenReturn(Optional.of(account));
        when(loyaltyRepository.save(any(LoyaltyAccount.class))).thenAnswer(inv -> inv.getArgument(0));

        // ₹1000 spent -> 100 base points * 1.0 = 100 points added -> lifetime reaches 550 (> 500) -> SILVER!
        LoyaltyResponse response = loyaltyService.earnPointsOnOrder(userId, new BigDecimal("1000.00"));

        assertThat(response.getPointsBalance()).isEqualTo(300);
        assertThat(response.getLifetimePoints()).isEqualTo(550);
        assertThat(response.getTier()).isEqualTo(LoyaltyTier.SILVER);
        verify(outboxService).saveEvent(eq("Loyalty"), any(), eq("LoyaltyPointsEarnedEvent"), any());
    }

    @Test
    @DisplayName("redeemPoints should deduct points balance and calculate rupee discount")
    void redeemPoints_success() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(loyaltyRepository.findByUser(user)).thenReturn(Optional.of(account));
        when(loyaltyRepository.save(any(LoyaltyAccount.class))).thenAnswer(inv -> inv.getArgument(0));

        RedeemPointsRequest request = RedeemPointsRequest.builder().points(100).build();
        LoyaltyResponse response = loyaltyService.redeemPoints(userId, request);

        assertThat(response.getPointsBalance()).isEqualTo(100);
        verify(outboxService).saveEvent(eq("Loyalty"), any(), eq("LoyaltyPointsRedeemedEvent"), any());
    }

    @Test
    @DisplayName("redeemPoints should throw BadRequestException if balance insufficient")
    void redeemPoints_insufficient() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(loyaltyRepository.findByUser(user)).thenReturn(Optional.of(account));

        RedeemPointsRequest request = RedeemPointsRequest.builder().points(500).build();

        assertThatThrownBy(() -> loyaltyService.redeemPoints(userId, request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Insufficient points");
    }
}
