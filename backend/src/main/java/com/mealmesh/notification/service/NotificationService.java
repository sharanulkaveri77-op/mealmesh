package com.mealmesh.notification.service;

import com.mealmesh.common.exception.ResourceNotFoundException;
import com.mealmesh.notification.dto.NotificationResponse;
import com.mealmesh.notification.dto.UnreadCountResponse;
import com.mealmesh.notification.entity.Notification;
import com.mealmesh.notification.repository.NotificationRepository;
import com.mealmesh.order.entity.Order;
import com.mealmesh.order.repository.OrderRepository;
import com.mealmesh.outbox.service.OutboxService;
import com.mealmesh.user.entity.User;
import com.mealmesh.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final OutboxService outboxService;

    @Transactional
    public Notification createNotification(
            UUID userId, String type, String title, String message, String data,
            String relatedEntityType, UUID relatedEntityId, String priority, String sentVia) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        Notification notification = Notification.builder()
                .user(user)
                .type(type)
                .title(title)
                .message(message)
                .data(data != null ? data : "{}")
                .isRead(false)
                .relatedEntityType(relatedEntityType)
                .relatedEntityId(relatedEntityId)
                .priority(priority != null ? priority : "NORMAL")
                .sentVia(sentVia != null ? sentVia : "IN_APP")
                .sentAt(Instant.now())
                .createdAt(Instant.now())
                .build();

        notification = notificationRepository.save(notification);
        log.info("Notification created: id={}, userId={}, type={}, title={}",
                notification.getId(), userId, type, title);

        // Optionally dispatch to outbox for email/SMS/push downstream workers
        outboxService.saveEvent("Notification", notification.getId(), "NotificationEvent",
                Map.of(
                        "notificationId", notification.getId(),
                        "userId", userId,
                        "type", type,
                        "title", title,
                        "message", message,
                        "sentVia", notification.getSentVia(),
                        "createdAt", Instant.now().toString()
                ));

        return notification;
    }

    @Transactional
    public Notification createNotification(UUID userId, String type, String title, String message, String data) {
        return createNotification(userId, type, title, message, data, null, null, "NORMAL", "IN_APP");
    }

    @Transactional(readOnly = true)
    public Page<NotificationResponse> getUserNotifications(UUID userId, boolean unreadOnly, Pageable pageable) {
        if (unreadOnly) {
            return notificationRepository.findByUserIdAndIsReadFalseOrderByCreatedAtDesc(userId, pageable)
                    .map(NotificationResponse::fromEntity);
        }
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)
                .map(NotificationResponse::fromEntity);
    }

    @Transactional(readOnly = true)
    public List<NotificationResponse> getUserNotificationsList(UUID userId, boolean unreadOnly) {
        if (unreadOnly) {
            return notificationRepository.findByUserIdAndIsReadFalse(userId).stream()
                    .map(NotificationResponse::fromEntity)
                    .collect(Collectors.toList());
        }
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(NotificationResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public void markAsRead(UUID notificationId, UUID userId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found with id: " + notificationId));

        if (!notification.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("You are not authorized to modify this notification");
        }

        notification.setIsRead(true);
        notification.setReadAt(Instant.now());
        notificationRepository.save(notification);
        log.info("Notification marked as read: id={}, userId={}", notificationId, userId);
    }

    @Transactional
    public void markAllAsRead(UUID userId) {
        notificationRepository.markAllAsRead(userId, Instant.now());
        log.info("All notifications marked as read for user: {}", userId);
    }

    @Transactional(readOnly = true)
    public UnreadCountResponse getUnreadCount(UUID userId) {
        long count = notificationRepository.countByUserIdAndIsReadFalse(userId);
        return UnreadCountResponse.builder().unreadCount(count).build();
    }

    // ==========================================
    // Lifecycle Event Notification Triggers
    // ==========================================

    public void sendOrderCreatedNotification(UUID userId, String orderNumber) {
        createNotification(userId, "ORDER_CREATED",
                "Order Placed Successfully",
                "Your order " + orderNumber + " has been placed and is being processed.",
                "{\"orderNumber\": \"" + orderNumber + "\"}");
    }

    public void sendOrderAcceptedNotification(UUID orderId, Integer preparationTimeMinutes) {
        orderRepository.findById(orderId).ifPresent(order -> {
            int prep = preparationTimeMinutes != null ? preparationTimeMinutes : 30;
            createNotification(order.getCustomer().getId(), "ORDER_ACCEPTED",
                    "Order Accepted by Restaurant",
                    "Restaurant accepted your order " + order.getOrderNumber() + ". Estimated prep time: " + prep + " mins.",
                    "{\"orderId\": \"" + orderId + "\", \"orderNumber\": \"" + order.getOrderNumber() + "\", \"prepTime\": " + prep + "}",
                    "Order", orderId, "NORMAL", "IN_APP");
        });
    }

    public void sendOrderPreparingNotification(UUID orderId) {
        orderRepository.findById(orderId).ifPresent(order -> {
            createNotification(order.getCustomer().getId(), "ORDER_PREPARING",
                    "Order Is Being Prepared",
                    "The kitchen has started preparing your order " + order.getOrderNumber() + ".",
                    "{\"orderId\": \"" + orderId + "\", \"orderNumber\": \"" + order.getOrderNumber() + "\"}",
                    "Order", orderId, "NORMAL", "IN_APP");
        });
    }

    public void sendOrderReadyNotification(UUID orderId) {
        orderRepository.findById(orderId).ifPresent(order -> {
            createNotification(order.getCustomer().getId(), "ORDER_READY",
                    "Order Ready for Pickup",
                    "Your order " + order.getOrderNumber() + " is packed and ready for delivery pickup.",
                    "{\"orderId\": \"" + orderId + "\", \"orderNumber\": \"" + order.getOrderNumber() + "\"}",
                    "Order", orderId, "HIGH", "IN_APP");
        });
    }

    public void sendOrderPickedUpNotification(UUID orderId) {
        orderRepository.findById(orderId).ifPresent(order -> {
            createNotification(order.getCustomer().getId(), "ORDER_PICKED_UP",
                    "Order Picked Up",
                    "Delivery partner has picked up your order " + order.getOrderNumber() + ".",
                    "{\"orderId\": \"" + orderId + "\", \"orderNumber\": \"" + order.getOrderNumber() + "\"}",
                    "Order", orderId, "HIGH", "IN_APP");
        });
    }

    public void sendOrderOutForDeliveryNotification(UUID orderId) {
        orderRepository.findById(orderId).ifPresent(order -> {
            createNotification(order.getCustomer().getId(), "ORDER_OUT_FOR_DELIVERY",
                    "Order Out for Delivery",
                    "Your order " + order.getOrderNumber() + " is on its way to you!",
                    "{\"orderId\": \"" + orderId + "\", \"orderNumber\": \"" + order.getOrderNumber() + "\"}",
                    "Order", orderId, "URGENT", "IN_APP");
        });
    }

    public void sendOrderDeliveredNotification(UUID userId, String orderNumber) {
        createNotification(userId, "ORDER_DELIVERED",
                "Order Delivered",
                "Your order " + orderNumber + " has been delivered. Enjoy your meal!",
                "{\"orderNumber\": \"" + orderNumber + "\"}");
    }

    public void sendOrderCancelledNotification(UUID orderId, String reason) {
        orderRepository.findById(orderId).ifPresent(order -> {
            createNotification(order.getCustomer().getId(), "ORDER_CANCELLED",
                    "Order Cancelled",
                    "Your order " + order.getOrderNumber() + " was cancelled. Reason: " + (reason != null ? reason : "N/A"),
                    "{\"orderId\": \"" + orderId + "\", \"orderNumber\": \"" + order.getOrderNumber() + "\", \"reason\": \"" + reason + "\"}",
                    "Order", orderId, "HIGH", "IN_APP");
        });
    }

    public void sendPaymentSuccessNotification(UUID userId, String orderNumber) {
        createNotification(userId, "PAYMENT_SUCCESS",
                "Payment Successful",
                "Your payment for order " + orderNumber + " was successful.",
                "{\"orderNumber\": \"" + orderNumber + "\"}");
    }

    public void sendPaymentFailedNotification(UUID userId, String orderNumber, String reason) {
        createNotification(userId, "PAYMENT_FAILED",
                "Payment Failed",
                "Your payment for order " + orderNumber + " failed: " + reason,
                "{\"orderNumber\": \"" + orderNumber + "\", \"reason\": \"" + reason + "\"}",
                null, null, "HIGH", "IN_APP");
    }

    public void sendDeliveryPartnerAssignedNotification(UUID orderId, String partnerName) {
        orderRepository.findById(orderId).ifPresent(order -> {
            createNotification(order.getCustomer().getId(), "DELIVERY_ASSIGNED",
                    "Delivery Partner Assigned",
                    partnerName + " has been assigned to deliver your order " + order.getOrderNumber() + ".",
                    "{\"orderId\": \"" + orderId + "\", \"partnerName\": \"" + partnerName + "\"}",
                    "Order", orderId, "NORMAL", "IN_APP");
        });
    }

    public void sendOrderRejectedNotification(UUID orderId, String reason) {
        orderRepository.findById(orderId).ifPresent(order -> {
            createNotification(order.getCustomer().getId(), "ORDER_CANCELLED",
                    "Order Rejected by Restaurant",
                    "Restaurant was unable to accept your order " + order.getOrderNumber() + ". Reason: " + (reason != null ? reason : "Unavailable"),
                    "{\"orderId\": \"" + orderId + "\", \"orderNumber\": \"" + order.getOrderNumber() + "\", \"reason\": \"" + reason + "\"}",
                    "Order", orderId, "HIGH", "IN_APP");
        });
    }

    public void sendOrderStatusNotification(UUID orderId, String previousStatus, String newStatus) {
        orderRepository.findById(orderId).ifPresent(order -> {
            createNotification(order.getCustomer().getId(), "ORDER_STATUS_CHANGED",
                    "Order Status Update",
                    "Your order " + order.getOrderNumber() + " status is now " + newStatus + ".",
                    "{\"orderId\": \"" + orderId + "\", \"orderNumber\": \"" + order.getOrderNumber() + "\", \"previousStatus\": \"" + previousStatus + "\", \"newStatus\": \"" + newStatus + "\"}",
                    "Order", orderId, "NORMAL", "IN_APP");
        });
    }
}