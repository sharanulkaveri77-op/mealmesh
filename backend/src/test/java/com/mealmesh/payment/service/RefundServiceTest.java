package com.mealmesh.payment.service;

import com.mealmesh.common.exception.BadRequestException;
import com.mealmesh.order.entity.Order;
import com.mealmesh.order.entity.OrderStatus;
import com.mealmesh.order.repository.OrderRepository;
import com.mealmesh.outbox.service.OutboxService;
import com.mealmesh.payment.dto.RefundRequest;
import com.mealmesh.payment.dto.RefundResponse;
import com.mealmesh.payment.entity.Payment;
import com.mealmesh.payment.entity.PaymentStatus;
import com.mealmesh.payment.entity.Refund;
import com.mealmesh.payment.entity.RefundStatus;
import com.mealmesh.payment.repository.PaymentRepository;
import com.mealmesh.payment.repository.RefundRepository;
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
class RefundServiceTest {

    @Mock
    private RefundRepository refundRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private OutboxService outboxService;

    @InjectMocks
    private RefundService refundService;

    private UUID userId;
    private UUID orderId;
    private User user;
    private Order order;
    private Payment payment;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        orderId = UUID.randomUUID();

        user = User.builder().id(userId).email("user@test.com").build();

        order = Order.builder()
                .id(orderId)
                .orderNumber("ORD-REF-100")
                .customer(user)
                .status(OrderStatus.CANCELLED)
                .build();

        payment = Payment.builder()
                .id(UUID.randomUUID())
                .order(order)
                .amount(new BigDecimal("450.00"))
                .status(PaymentStatus.COMPLETED)
                .build();
    }

    @Test
    @DisplayName("processRefund should create refund record and update payment status")
    void processRefund_success() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(refundRepository.findByOrderId(orderId)).thenReturn(Optional.empty());
        when(paymentRepository.findByOrderId(orderId)).thenReturn(Optional.of(payment));
        when(refundRepository.save(any(Refund.class))).thenAnswer(inv -> inv.getArgument(0));

        RefundRequest request = RefundRequest.builder()
                .orderId(orderId)
                .reason("Restaurant closed")
                .build();

        RefundResponse response = refundService.processRefund(userId, request);

        assertThat(response.getStatus()).isEqualTo(RefundStatus.COMPLETED);
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.REFUNDED);
        verify(paymentRepository).save(payment);
        verify(outboxService).saveEvent(eq("Payment"), any(), eq("RefundCompletedEvent"), any());
    }

    @Test
    @DisplayName("processRefund should throw exception if order is not cancelled or rejected")
    void processRefund_invalidStatus() {
        order.setStatus(OrderStatus.DELIVERED);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        RefundRequest request = RefundRequest.builder()
                .orderId(orderId)
                .reason("Did not like food")
                .build();

        assertThatThrownBy(() -> refundService.processRefund(userId, request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Current status: DELIVERED");
    }
}
