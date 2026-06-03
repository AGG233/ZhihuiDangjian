package com.rauio.smartdangjian.config;

import java.io.IOException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.web.filter.OncePerRequestFilter;

public class SecurityHeadersFilter extends OncePerRequestFilter {

    static final String CONTENT_SECURITY_POLICY = "default-src 'self'; "
            + "base-uri 'self'; "
            + "object-src 'none'; "
            + "frame-ancestors 'none'; "
            + "img-src 'self' data: https:; "
            + "style-src 'self' 'unsafe-inline'; "
            + "script-src 'self'";
    static final String PERMISSIONS_POLICY = "geolocation=(), microphone=(), camera=()";
    static final String REFERRER_POLICY = "strict-origin-when-cross-origin";
    static final String STRICT_TRANSPORT_SECURITY = "max-age=31536000; includeSubDomains";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        response.setHeader("Content-Security-Policy", CONTENT_SECURITY_POLICY);
        response.setHeader("X-Content-Type-Options", "nosniff");
        response.setHeader("X-Frame-Options", "DENY");
        response.setHeader("Referrer-Policy", REFERRER_POLICY);
        response.setHeader("Permissions-Policy", PERMISSIONS_POLICY);
        if (request.isSecure()) {
            response.setHeader("Strict-Transport-Security", STRICT_TRANSPORT_SECURITY);
        }
        filterChain.doFilter(request, response);
    }
}
