package com.example.tablecure.common.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Set;

/**
 * Strips CORS-related Vary headers (Vary: Origin, etc.) from /api/products responses.
 * Without this, Spring's CORS processor causes Cloudflare to store a separate cache
 * variant per Origin value. Purge-by-URL only clears the "no-origin" variant, leaving
 * the www.tablecure.com variant stale after admin updates.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class ProductVaryHeaderFilter extends OncePerRequestFilter {

    private static final Set<String> CORS_VARY_HEADERS = Set.of(
            "origin",
            "access-control-request-method",
            "access-control-request-headers"
    );

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        if (request.getRequestURI().startsWith("/api/products")) {
            chain.doFilter(request, new VaryStrippingResponseWrapper(response));
        } else {
            chain.doFilter(request, response);
        }
    }

    private static class VaryStrippingResponseWrapper extends HttpServletResponseWrapper {

        VaryStrippingResponseWrapper(HttpServletResponse response) {
            super(response);
        }

        @Override
        public void addHeader(String name, String value) {
            if ("vary".equalsIgnoreCase(name)
                    && CORS_VARY_HEADERS.contains(value.trim().toLowerCase())) {
                return; // block Vary: Origin / Access-Control-* from being written
            }
            super.addHeader(name, value);
        }

        @Override
        public void setHeader(String name, String value) {
            if ("vary".equalsIgnoreCase(name)) {
                // Keep only non-CORS vary values (e.g. Accept-Encoding)
                String filtered = java.util.Arrays.stream(value.split(","))
                        .map(String::trim)
                        .filter(v -> !CORS_VARY_HEADERS.contains(v.toLowerCase()))
                        .collect(java.util.stream.Collectors.joining(", "));
                if (!filtered.isEmpty()) {
                    super.setHeader(name, filtered);
                }
                return;
            }
            super.setHeader(name, value);
        }
    }
}
