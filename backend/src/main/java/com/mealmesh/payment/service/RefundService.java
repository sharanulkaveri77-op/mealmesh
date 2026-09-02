package com.mealmesh.payment.service;

import com.mealmesh.common.exception.BadRequestException;
import com.mealmesh.common.exception.ResourceNotFoundException;
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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class RefundService {

    private final RefundRepository refundRepository;
    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;
    private final OutboxService outboxService;

    @Transactional
    public RefundResponse processRefund(UUID userId, RefundRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + request.getOrderId()));

        if (!order.getCustomer().getId().equals(userId)) {
            throw new BadRequestException("You are not authorized to request a refund for this order");
        }

        if (order.getStatus() != OrderStatus.CANCELLED && order.getStatus() != OrderStatus.RESTAURANT_REJECTED) {
            throw new BadRequestException("Refund can only be requested for CANCELLED or REJECTED orders. Current status: " + order.getStatus());
        }

        if (refundRepository.findByOrderId(order.getId()).isPresent()) {
            throw new BadRequestException("A refund request already exists for this order");
        }

        Payment payment = paymentRepository.findByOrderId(order.getId())
                .orElseThrow(() -> new BadRequestException("No payment record found for order: " + order.getId()));

        String refundTxId = "RFD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        Refund refund = Refund.builder()
                .order(order)
                .payment(payment)
                .user(user)
                .amount(payment.getAmount())
                .reason(request.getReason())
                .status(RefundStatus.COMPLETED)
                .refundTransactionId(refundTxId)
                .build();

        refund = refundRepository.save(refund);

        payment.setStatus(PaymentStatus.REFUNDED);
        paymentRepository.save(payment);

        outboxService.saveEvent("Payment", payment.getId(), "RefundCompletedEvent", Map.of(
                "refundId", refund.getId(),
                "orderId", order.getId(),
                "orderNumber", order.getOrderNumber(),
                "amount", refund.getAmount(),
                "refundTransactionId", refundTxId,
                "userId", user.getId()
        ));

        log.info("Refund processed successfully: {} for order: {}", refundTxId, order.getOrderNumber());
        return RefundResponse.from(refund);
    }

    @Transactional(readOnly = true)
    public List<RefundResponse> getUserRefunds(UUID userId) {
        return refundRepository.findByUserId(userId).stream()
                .map(RefundResponse::from)
                .toList();
    }
}
