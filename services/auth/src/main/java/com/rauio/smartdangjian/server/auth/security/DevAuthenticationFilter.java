package com.rauio.smartdangjian.server.auth.security;

import java.io.IOException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.rauio.smartdangjian.server.user.pojo.entity.User;
import com.rauio.smartdangjian.server.user.service.UserService;

import lombok.RequiredArgsConstructor;

@Component
@ConditionalOnProperty(name = "app.security.enabled", havingValue = "false")
@RequiredArgsConstructor
public class DevAuthenticationFilter extends OncePerRequestFilter {

    private final UserService userService;

    @Value("${app.dev.default-user-id:}")
    private String defaultUserId;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        Authentication existingAuth = SecurityContextHolder.getContext().getAuthentication();

        boolean needsDefaultUser = existingAuth == null
                || !existingAuth.isAuthenticated()
                || existingAuth instanceof AnonymousAuthenticationToken;

        if (needsDefaultUser && defaultUserId != null && !defaultUserId.isBlank()) {
            User user = userService.getById(defaultUserId);
            if (user != null) {
                UsernamePasswordAuthenticationToken auth =
                        new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
                auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        }

        filterChain.doFilter(request, response);
    }
}
