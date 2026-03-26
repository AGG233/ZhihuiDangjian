package com.rauio.smartdangjian.server.auth.security;

import com.rauio.smartdangjian.server.auth.service.JwtService;
import com.rauio.smartdangjian.server.user.pojo.entity.User;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService tokenAuthenticationService;
    private final HandlerExceptionResolver resolver;

    public JwtAuthenticationFilter(
            @Qualifier("handlerExceptionResolver") HandlerExceptionResolver resolver,
            JwtService tokenAuthenticationService
    ) {
        this.resolver = resolver;
        this.tokenAuthenticationService = tokenAuthenticationService;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) {
        try {
            String authHeader = request.getHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                filterChain.doFilter(request, response);
                return;
            }

            String token = authHeader.substring(7);
            User user = tokenAuthenticationService.validateToken(token);

            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                    user,
                    null,
                    user.getAuthorities()
            );
            authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authToken);
            filterChain.doFilter(request, response);
        } catch (Exception e) {
            resolver.resolveException(request, response, null, e);
        }
    }
}
