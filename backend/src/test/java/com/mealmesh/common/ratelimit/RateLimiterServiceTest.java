package com.mealmesh.common.ratelimit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RateLimiterServiceTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private RateLimiterService rateLimiterService;

    @BeforeEach
    void setUp() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    @DisplayName("isAllowed should return true when request count is within limit")
    void isAllowed_withinLimit() {
        when(valueOperations.increment("rate_limit:test-key")).thenReturn(5L);

        boolean allowed = rateLimiterService.isAllowed("test-key", 10, 60);

        assertThat(allowed).isTrue();
    }

    @Test
    @DisplayName("isAllowed should set expiry on first request in window")
    void isAllowed_firstRequest_setsExpiry() {
        when(valueOperations.increment("rate_limit:test-key")).thenReturn(1L);

        boolean allowed = rateLimiterService.isAllowed("test-key", 10, 60);

        assertThat(allowed).isTrue();
        verify(redisTemplate).expire(eq("rate_limit:test-key"), any());
    }

    @Test
    @DisplayName("isAllowed should return false when limit is exceeded")
    void isAllowed_exceeded() {
        when(valueOperations.increment("rate_limit:test-key")).thenReturn(11L);

        boolean allowed = rateLimiterService.isAllowed("test-key", 10, 60);

        assertThat(allowed).isFalse();
    }
}
