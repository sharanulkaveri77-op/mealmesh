package com.mealmesh.delivery.service;

import com.mealmesh.common.exception.BadRequestException;
import com.mealmesh.delivery.dto.DeliveryAssignmentResponse;
import com.mealmesh.delivery.dto.DeliveryPartnerProfileResponse;
import com.mealmesh.delivery.dto.PartnerStatusUpdateRequest;
import com.mealmesh.delivery.entity.DeliveryAssignment;
import com.mealmesh.delivery.entity.DeliveryPartner;
import com.mealmesh.delivery.repository.DeliveryAssignmentRepository;
import com.mealmesh.delivery.repository.DeliveryPartnerRepository;
import com.mealmesh.order.entity.Order;
import com.mealmesh.order.entity.OrderStatus;
import com.mealmesh.order.repository.OrderRepository;
import com.mealmesh.outbox.service.OutboxService;
import com.mealmesh.restaurant.entity.Restaurant;
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
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeliveryPartnerServiceTest {

    @Mock
    private DeliveryPartnerRepository partnerRepository;

    @Mock
    private DeliveryAssignmentRepository assignmentRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private OutboxService outboxService;

    @InjectMocks
    private DeliveryPartnerService deliveryPartnerService;

    private UUID userId;
    private UUID partnerId;
    private UUID assignmentId;
    private UUID orderId;
    private User user;
    private DeliveryPartner partner;
    private Order order;
    private DeliveryAssignment assignment;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        partnerId = UUID.randomUUID();
        assignmentId = UUID.randomUUID();
        orderId = UUID.randomUUID();

        user = User.builder()
                .id(userId)
                .name("Rider Dave")
                .email("dave@delivery.com")
                .phone("9876543210")
                .build();

        partner = DeliveryPartner.builder()
                .id(partnerId)
                .user(user)
                .vehicleType("MOTORCYCLE")
                .vehicleNumber("DL01AB1234")
                .isOnline(true)
                .isAvailable(true)
                .currentActiveOrders(1)
                .maxConcurrentOrders(3)
                .totalDeliveries(10)
                .totalEarnings(new BigDecimal("500.00"))
                .build();

        Restaurant restaurant = Restaurant.builder()
                .id(UUID.randomUUID())
                .name("Burger Hub")
                .phone("1122334455")
                .build();

        order = Order.builder()
                .id(orderId)
                .orderNumber("ORD-DEL-101")
                .customer(user)
                .restaurant(restaurant)
                .status(OrderStatus.READY_FOR_PICKUP)
                .deliveryAddressSnapshot("42 Market Street")
                .build();

        assignment = DeliveryAssignment.builder()
                .id(assignmentId)
                .order(order)
                .deliveryPartner(partner)
                .status("ASSIGNED")
                .assignedAt(Instant.now())
                .build();
    }

    @Test
    @DisplayName("updatePartnerStatus should toggle isOnline and isAvailable")
    void updatePartnerStatus_success() {
        // Arrange
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(partnerRepository.findByUser(user)).thenReturn(Optional.of(partner));
        when(partnerRepository.save(any(DeliveryPartner.class))).thenAnswer(inv -> inv.getArgument(0));

        PartnerStatusUpdateRequest request = PartnerStatusUpdateRequest.builder()
                .isOnline(false)
                .isAvailable(false)
                .build();

        // Act
        DeliveryPartnerProfileResponse response = deliveryPartnerService.updatePartnerStatus(userId, request);

        // Assert
        assertThat(response.getIsOnline()).isFalse();
        assertThat(response.getIsAvailable()).isFalse();
    }

    @Test
    @DisplayName("acceptAssignment should transition status to ACCEPTED and emit outbox event")
    void acceptAssignment_success() {
        // Arrange
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(partnerRepository.findByUser(user)).thenReturn(Optional.of(partner));
        when(assignmentRepository.findById(assignmentId)).thenReturn(Optional.of(assignment));
        when(assignmentRepository.save(any(DeliveryAssignment.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        DeliveryAssignmentResponse response = deliveryPartnerService.acceptAssignment(userId, assignmentId);

        // Assert
        assertThat(response.getStatus()).isEqualTo("ACCEPTED");
        assertThat(response.getAcceptedAt()).isNotNull();
        verify(outboxService).saveEvent(eq("Delivery"), eq(assignmentId), eq("DeliveryAcceptedEvent"), any());
    }

    @Test
    @DisplayName("pickupOrder should update order to OUT_FOR_DELIVERY and emit outbox event")
    void pickupOrder_success() {
        // Arrange
        assignment.setStatus("ACCEPTED");
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(partnerRepository.findByUser(user)).thenReturn(Optional.of(partner));
        when(assignmentRepository.findById(assignmentId)).thenReturn(Optional.of(assignment));
        when(assignmentRepository.save(any(DeliveryAssignment.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        DeliveryAssignmentResponse response = deliveryPartnerService.pickupOrder(userId, assignmentId);

        // Assert
        assertThat(response.getStatus()).isEqualTo("PICKED_UP");
        assertThat(order.getStatus()).isEqualTo(OrderStatus.OUT_FOR_DELIVERY);
        verify(orderRepository).save(order);
        verify(outboxService).saveEvent(eq("Order"), eq(orderId), eq("OrderPickedUpEvent"), any());
    }

    @Test
    @DisplayName("completeDelivery should mark order DELIVERED, credit earnings, and update partner counters")
    void completeDelivery_success() {
        // Arrange
        assignment.setStatus("PICKED_UP");
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(partnerRepository.findByUser(user)).thenReturn(Optional.of(partner));
        when(assignmentRepository.findById(assignmentId)).thenReturn(Optional.of(assignment));
        when(assignmentRepository.save(any(DeliveryAssignment.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        DeliveryAssignmentResponse response = deliveryPartnerService.completeDelivery(userId, assignmentId);

        // Assert
        assertThat(response.getStatus()).isEqualTo("DELIVERED");
        assertThat(order.getStatus()).isEqualTo(OrderStatus.DELIVERED);
        assertThat(partner.getTotalDeliveries()).isEqualTo(11);
        assertThat(partner.getTotalEarnings()).isEqualTo(new BigDecimal("550.00"));
        assertThat(partner.getCurrentActiveOrders()).isEqualTo(0);
        assertThat(partner.getIsAvailable()).isTrue();

        verify(orderRepository).save(order);
        verify(partnerRepository).save(partner);
        verify(outboxService).saveEvent(eq("Order"), eq(orderId), eq("OrderDeliveredEvent"), any());
    }

    @Test
    @DisplayName("acceptAssignment should throw BadRequestException if partner is not assigned")
    void acceptAssignment_unauthorized() {
        // Arrange
        DeliveryPartner otherPartner = DeliveryPartner.builder().id(UUID.randomUUID()).build();
        assignment.setDeliveryPartner(otherPartner);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(partnerRepository.findByUser(user)).thenReturn(Optional.of(partner));
        when(assignmentRepository.findById(assignmentId)).thenReturn(Optional.of(assignment));

        // Act & Assert
        assertThatThrownBy(() -> deliveryPartnerService.acceptAssignment(userId, assignmentId))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("not authorized");
    }
}
