package com.mealmesh.payment.dto;

import com.mealmesh.payment.entity.Refund;
import com.mealmesh.payment.entity.RefundStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefundResponse {

    private UUID id;
    private UUID orderId;
    private String orderNumber;
    private BigDecimal amount;
    private String reason;
    private RefundStatus status;
    private String refundTransactionId;
    private Instant createdAt;

    public static RefundResponse from(Refund refund) {
        return RefundResponse.builder()
                .id(refund.getId())
                .orderId(refund.getOrder().getId())
                .orderNumber(refund.getOrder().getOrderNumber())
                .amount(refund.getAmount())
                .reason(refund.getReason())
                .status(refund.getStatus())
                .refundTransactionId(refund.getRefundTransactionId())
                .createdAt(refund.getCreatedAt())
                .build();
    }
}
