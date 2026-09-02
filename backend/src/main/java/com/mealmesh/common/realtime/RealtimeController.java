package com.mealmesh.common.realtime;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.UUID;

@RestController
@RequestMapping("/api/realtime")
@RequiredArgsConstructor
public class RealtimeController {

    private final RealtimeUpdateService realtimeUpdateService;

    @GetMapping(value = "/orders/{orderId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamOrderUpdates(@PathVariable UUID orderId) {
        return realtimeUpdateService.subscribeToOrderUpdates(orderId);
    }

    @GetMapping(value = "/notifications", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamUserNotifications(@AuthenticationPrincipal UUID userId) {
        return realtimeUpdateService.subscribeToUserNotifications(userId);
    }
}
