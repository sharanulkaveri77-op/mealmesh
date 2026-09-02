package com.mealmesh.common.realtime;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
@Slf4j
public class RealtimeUpdateService {

    // Order tracking subscriptions: orderId -> list of emitters
    private final Map<UUID, CopyOnWriteArrayList<SseEmitter>> orderEmitters = new ConcurrentHashMap<>();

    // User notification subscriptions: userId -> list of emitters
    private final Map<UUID, CopyOnWriteArrayList<SseEmitter>> userEmitters = new ConcurrentHashMap<>();

    private static final Long EMITTER_TIMEOUT = 30 * 60 * 1000L; // 30 mins

    public SseEmitter subscribeToOrderUpdates(UUID orderId) {
        SseEmitter emitter = new SseEmitter(EMITTER_TIMEOUT);
        orderEmitters.computeIfAbsent(orderId, k -> new CopyOnWriteArrayList<>()).add(emitter);

        emitter.onCompletion(() -> removeOrderEmitter(orderId, emitter));
        emitter.onTimeout(() -> removeOrderEmitter(orderId, emitter));
        emitter.onError(e -> removeOrderEmitter(orderId, emitter));

        try {
            emitter.send(SseEmitter.event().name("INIT").data("Connected to live tracking for order: " + orderId));
        } catch (IOException e) {
            log.debug("Error sending init event: {}", e.getMessage());
        }

        return emitter;
    }

    public SseEmitter subscribeToUserNotifications(UUID userId) {
        SseEmitter emitter = new SseEmitter(EMITTER_TIMEOUT);
        userEmitters.computeIfAbsent(userId, k -> new CopyOnWriteArrayList<>()).add(emitter);

        emitter.onCompletion(() -> removeUserEmitter(userId, emitter));
        emitter.onTimeout(() -> removeUserEmitter(userId, emitter));
        emitter.onError(e -> removeUserEmitter(userId, emitter));

        try {
            emitter.send(SseEmitter.event().name("INIT").data("Connected to live notification stream for user: " + userId));
        } catch (IOException e) {
            log.debug("Error sending init event: {}", e.getMessage());
        }

        return emitter;
    }

    public void publishOrderUpdate(UUID orderId, String eventType, Object payload) {
        CopyOnWriteArrayList<SseEmitter> emitters = orderEmitters.get(orderId);
        if (emitters == null || emitters.isEmpty()) return;

        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event().name(eventType).data(payload));
            } catch (Exception e) {
                removeOrderEmitter(orderId, emitter);
            }
        }
    }

    public void publishUserNotification(UUID userId, Object notificationPayload) {
        CopyOnWriteArrayList<SseEmitter> emitters = userEmitters.get(userId);
        if (emitters == null || emitters.isEmpty()) return;

        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event().name("NOTIFICATION").data(notificationPayload));
            } catch (Exception e) {
                removeUserEmitter(userId, emitter);
            }
        }
    }

    private void removeOrderEmitter(UUID orderId, SseEmitter emitter) {
        CopyOnWriteArrayList<SseEmitter> list = orderEmitters.get(orderId);
        if (list != null) {
            list.remove(emitter);
            if (list.isEmpty()) {
                orderEmitters.remove(orderId);
            }
        }
    }

    private void removeUserEmitter(UUID userId, SseEmitter emitter) {
        CopyOnWriteArrayList<SseEmitter> list = userEmitters.get(userId);
        if (list != null) {
            list.remove(emitter);
            if (list.isEmpty()) {
                userEmitters.remove(userId);
            }
        }
    }
}
