package com.example.tablecure.auth.config;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Order(1)
public class RateLimitFilter extends OncePerRequestFilter {

    private static final int MAX_REQUESTS = 5;
    private static final long WINDOW_MS = 60_000L;
    private static final List<String> RATE_LIMITED_PATHS = List.of("/api/auth/login", "/api/auth/register", "/api/auth/forgot-password");

    private final ConcurrentHashMap<String, List<Long>> requestTimestamps = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        if (!RATE_LIMITED_PATHS.contains(request.getRequestURI())) {
            filterChain.doFilter(request, response);
            return;
        }

        String ip = request.getRemoteAddr();
        long now = System.currentTimeMillis();

        requestTimestamps.putIfAbsent(ip, new ArrayList<>());
        List<Long> timestamps = requestTimestamps.get(ip);

        synchronized (timestamps) {
            timestamps.removeIf(t -> now - t > WINDOW_MS);
            if (timestamps.size() >= MAX_REQUESTS) {
                response.setStatus(429);
                response.setContentType("text/plain");
                response.getWriter().write("Too many requests. Please try again later.");
                return;
            }
            timestamps.add(now);
        }

        filterChain.doFilter(request, response);
    }
}
