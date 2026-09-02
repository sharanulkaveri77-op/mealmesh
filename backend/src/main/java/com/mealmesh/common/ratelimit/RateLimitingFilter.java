package com.mealmesh.common.ratelimit;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class RateLimitingFilter extends OncePerRequestFilter {

    private final RateLimiterService rateLimiterService;

    // Rate limits per minute
    private static final int AUTH_LIMIT = 20; // 20 attempts per min for login/register
    private static final int CHECKOUT_LIMIT = 30; // 30 checkout attempts per min
    private static final int GENERAL_LIMIT = 200; // 200 requests per min for other APIs

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        String path = request.getRequestURI();
        String clientIp = resolveClientIp(request);

        int maxLimit = GENERAL_LIMIT;
        String limitKey = "ip:" + clientIp;

        if (path.startsWith("/api/auth")) {
            maxLimit = AUTH_LIMIT;
            limitKey = "auth:" + clientIp;
        } else if (path.startsWith("/api/checkout")) {
            maxLimit = CHECKOUT_LIMIT;
            limitKey = "checkout:" + clientIp;
        }

        boolean allowed = rateLimiterService.isAllowed(limitKey, maxLimit, 60);

        if (!allowed) {
            log.warn("Rate limit exceeded for client: {}, path: {}", clientIp, path);
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType("application/json");
            response.setHeader("Retry-After", "60");
            response.getWriter().write("{\"error\":\"Too Many Requests\",\"message\":\"Rate limit exceeded. Please try again later.\"}");
            return;
        }

        response.setHeader("X-RateLimit-Limit", String.valueOf(maxLimit));
        response.setHeader("X-RateLimit-Remaining", String.valueOf(rateLimiterService.getRemainingLimit(limitKey, maxLimit)));

        filterChain.doFilter(request, response);
    }

    private String resolveClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isBlank()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
