package com.mealmesh.order.service;

import com.mealmesh.order.entity.OrderStatus;
import com.mealmesh.common.exception.BadRequestException;

import java.util.EnumMap;
import java.util.Map;
import java.util.Set;

public class OrderStateMachine {

    private static final Map<OrderStatus, Set<OrderStatus>> VALID_TRANSITIONS = new EnumMap<>(OrderStatus.class);

    static {
        VALID_TRANSITIONS.put(OrderStatus.CREATED, Set.of(OrderStatus.PAYMENT_PENDING, OrderStatus.CANCELLED));
        VALID_TRANSITIONS.put(OrderStatus.PAYMENT_PENDING, Set.of(OrderStatus.PAYMENT_CONFIRMED, OrderStatus.PAYMENT_FAILED, OrderStatus.CANCELLED));
        VALID_TRANSITIONS.put(OrderStatus.PAYMENT_CONFIRMED, Set.of(OrderStatus.RESTAURANT_PENDING, OrderStatus.CANCELLED));
        VALID_TRANSITIONS.put(OrderStatus.RESTAURANT_PENDING, Set.of(OrderStatus.RESTAURANT_ACCEPTED, OrderStatus.RESTAURANT_REJECTED));
        VALID_TRANSITIONS.put(OrderStatus.RESTAURANT_ACCEPTED, Set.of(OrderStatus.PREPARING, OrderStatus.CANCELLED));
        VALID_TRANSITIONS.put(OrderStatus.PREPARING, Set.of(OrderStatus.READY_FOR_PICKUP, OrderStatus.CANCELLED));
        VALID_TRANSITIONS.put(OrderStatus.READY_FOR_PICKUP, Set.of(OrderStatus.DELIVERY_PARTNER_ASSIGNED, OrderStatus.CANCELLED));
        VALID_TRANSITIONS.put(OrderStatus.DELIVERY_PARTNER_ASSIGNED, Set.of(OrderStatus.PICKED_UP, OrderStatus.CANCELLED));
        VALID_TRANSITIONS.put(OrderStatus.PICKED_UP, Set.of(OrderStatus.OUT_FOR_DELIVERY));
        VALID_TRANSITIONS.put(OrderStatus.OUT_FOR_DELIVERY, Set.of(OrderStatus.DELIVERED));
        VALID_TRANSITIONS.put(OrderStatus.DELIVERED, Set.of());
        VALID_TRANSITIONS.put(OrderStatus.CANCELLED, Set.of());
        VALID_TRANSITIONS.put(OrderStatus.PAYMENT_FAILED, Set.of(OrderStatus.PAYMENT_PENDING, OrderStatus.CANCELLED));
        VALID_TRANSITIONS.put(OrderStatus.RESTAURANT_REJECTED, Set.of());
    }

    public static void validateTransition(OrderStatus from, OrderStatus to) {
        if (from == to) {
            return;
        }
        Set<OrderStatus> allowed = VALID_TRANSITIONS.get(from);
        if (allowed == null || !allowed.contains(to)) {
            throw new BadRequestException(
                    String.format("Invalid order status transition: %s -> %s", from, to)
            );
        }
    }

    public static Set<OrderStatus> getValidTransitions(OrderStatus from) {
        return VALID_TRANSITIONS.getOrDefault(from, Set.of());
    }

    public static boolean isTerminal(OrderStatus status) {
        return status == OrderStatus.DELIVERED || status == OrderStatus.CANCELLED || status == OrderStatus.RESTAURANT_REJECTED;
    }

    public static boolean canCancel(OrderStatus status) {
        return Set.of(
                OrderStatus.CREATED,
                OrderStatus.PAYMENT_PENDING,
                OrderStatus.PAYMENT_CONFIRMED,
                OrderStatus.RESTAURANT_PENDING,
                OrderStatus.RESTAURANT_ACCEPTED,
                OrderStatus.PREPARING
        ).contains(status);
    }
}