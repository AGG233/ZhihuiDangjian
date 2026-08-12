package com.rauio.smartdangjian.server.auth.config;

import java.io.IOException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.web.filter.OncePerRequestFilter;

import cn.dev33.satoken.stp.SaLoginModel;
import cn.dev33.satoken.stp.StpUtil;

@Profile("dev")
@Order(0)
public class DevAutoLoginFilter extends OncePerRequestFilter {

    @Value("${app.dev.default-user-id:}")
    private String defaultDevUserId;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        if (!StpUtil.isLogin() && defaultDevUserId != null && !defaultDevUserId.isEmpty()) {
            StpUtil.login(defaultDevUserId, SaLoginModel.create().setTimeout(86400));
        }
        chain.doFilter(request, response);
    }
}
