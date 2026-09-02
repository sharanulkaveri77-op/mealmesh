package com.mealmesh.notification.service;

import com.mealmesh.notification.dto.NotificationResponse;
import com.mealmesh.notification.dto.UnreadCountResponse;
import com.mealmesh.notification.entity.Notification;
import com.mealmesh.notification.repository.NotificationRepository;
import com.mealmesh.order.entity.Order;
import com.mealmesh.order.repository.OrderRepository;
import com.mealmesh.outbox.service.OutboxService;
import com.mealmesh.user.entity.User;
import com.mealmesh.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OutboxService outboxService;

    @InjectMocks
    private NotificationService notificationService;

    private UUID userId;
    private User testUser;
    private UUID orderId;
    private Order testOrder;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        orderId = UUID.randomUUID();

        testUser = User.builder()
                .id(userId)
                .name("Alice Customer")
                .email("alice@example.com")
                .build();

        testOrder = Order.builder()
                .id(orderId)
                .orderNumber("ORD-2026-999")
                .customer(testUser)
                .build();
    }

    @Test
    @DisplayName("createNotification should persist notification and publish outbox event")
    void createNotification_success() {
        // Arrange
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(notificationRepository.save(any(Notification.class))).thenAnswer(inv -> {
            Notification n = inv.getArgument(0);
            n.setId(UUID.randomUUID());
            return n;
        });

        // Act
        Notification notification = notificationService.createNotification(
                userId, "ORDER_CREATED", "Order Placed", "Your order has been placed", "{}"
        );

        // Assert
        assertThat(notification).isNotNull();
        assertThat(notification.getTitle()).isEqualTo("Order Placed");
        assertThat(notification.getType()).isEqualTo("ORDER_CREATED");
        verify(outboxService).saveEvent(eq("Notification"), any(UUID.class), eq("NotificationEvent"), any());
    }

    @Test
    @DisplayName("sendOrderAcceptedNotification should lookup order customer and dispatch notification")
    void sendOrderAcceptedNotification_success() {
        // Arrange
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(testOrder));
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(notificationRepository.save(any(Notification.class))).thenAnswer(inv -> {
            Notification n = inv.getArgument(0);
            n.setId(UUID.randomUUID());
            return n;
        });

        // Act
        notificationService.sendOrderAcceptedNotification(orderId, 25);

        // Assert
        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(captor.capture());
        Notification captured = captor.getValue();

        assertThat(captured.getType()).isEqualTo("ORDER_ACCEPTED");
        assertThat(captured.getMessage()).contains("25 mins");
    }

    @Test
    @DisplayName("getUserNotifications should return paginated list")
    void getUserNotifications_success() {
        // Arrange
        Notification n = Notification.builder()
                .id(UUID.randomUUID())
                .user(testUser)
                .type("ORDER_DELIVERED")
                .title("Delivered")
                .message("Enjoy your meal")
                .isRead(false)
                .createdAt(Instant.now())
                .build();

        Pageable pageable = PageRequest.of(0, 10);
        when(notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable))
                .thenReturn(new PageImpl<>(List.of(n)));

        // Act
        Page<NotificationResponse> result = notificationService.getUserNotifications(userId, false, pageable);

        // Assert
        assertThat(result).isNotEmpty();
        assertThat(result.getContent().get(0).getType()).isEqualTo("ORDER_DELIVERED");
    }

    @Test
    @DisplayName("markAsRead should update isRead status to true")
    void markAsRead_success() {
        // Arrange
        UUID notificationId = UUID.randomUUID();
        Notification notification = Notification.builder()
                .id(notificationId)
                .user(testUser)
                .isRead(false)
                .build();

        when(notificationRepository.findById(notificationId)).thenReturn(Optional.of(notification));

        // Act
        notificationService.markAsRead(notificationId, userId);

        // Assert
        assertThat(notification.getIsRead()).isTrue();
        assertThat(notification.getReadAt()).isNotNull();
        verify(notificationRepository).save(notification);
    }

    @Test
    @DisplayName("markAsRead should throw exception if user is unauthorized")
    void markAsRead_unauthorized() {
        // Arrange
        UUID notificationId = UUID.randomUUID();
        UUID otherUserId = UUID.randomUUID();
        Notification notification = Notification.builder()
                .id(notificationId)
                .user(testUser)
                .isRead(false)
                .build();

        when(notificationRepository.findById(notificationId)).thenReturn(Optional.of(notification));

        // Act & Assert
        assertThatThrownBy(() -> notificationService.markAsRead(notificationId, otherUserId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Not authorized");
    }

    @Test
    @DisplayName("getUnreadCount should return correct count")
    void getUnreadCount_success() {
        // Arrange
        when(notificationRepository.countByUserIdAndIsReadFalse(userId)).thenReturn(7L);

        // Act
        UnreadCountResponse response = notificationService.getUnreadCount(userId);

        // Assert
        assertThat(response.getUnreadCount()).isEqualTo(7L);
    }
}
