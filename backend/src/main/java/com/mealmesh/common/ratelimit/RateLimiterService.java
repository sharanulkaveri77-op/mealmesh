package com.mealmesh.common.ratelimit;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
@Slf4j
public class RateLimiterService {

    private final StringRedisTemplate redisTemplate;

    /**
     * Checks whether the request from a specific key (IP or user ID) is allowed.
     * Uses sliding window token rate limiting backed by Redis.
     *
     * @param key unique identifier (e.g. IP address or userId)
     * @param maxRequests maximum allowed requests within the window
     * @param windowSeconds time window duration in seconds
     * @return true if allowed, false if rate limit exceeded
     */
    public boolean isAllowed(String key, int maxRequests, int windowSeconds) {
        String redisKey = "rate_limit:" + key;
        try {
            Long currentCount = redisTemplate.opsForValue().increment(redisKey);
            if (currentCount != null && currentCount == 1) {
                redisTemplate.expire(redisKey, Duration.ofSeconds(windowSeconds));
            }
            return currentCount != null && currentCount <= maxRequests;
        } catch (Exception e) {
            log.debug("Redis rate limiter unavailable, failing open: {}", e.getMessage());
            return true; // Fail-open resilience when Redis is temporarily unreachable
        }
    }

    public long getRemainingLimit(String key, int maxRequests) {
        String redisKey = "rate_limit:" + key;
        try {
            String val = redisTemplate.opsForValue().get(redisKey);
            if (val != null) {
                long current = Long.parseLong(val);
                return Math.max(0, maxRequests - current);
            }
        } catch (Exception e) {
            log.debug("Error getting remaining rate limit: {}", e.getMessage());
        }
        return maxRequests;
    }
}
