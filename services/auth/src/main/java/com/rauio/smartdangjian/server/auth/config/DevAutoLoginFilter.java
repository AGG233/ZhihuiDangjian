package com.rauio.smartdangjian.server.auth.config;

import java.io.IOException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.rauio.smartdangjian.server.auth.security.SessionPrincipalFactory;
import com.rauio.smartdangjian.server.user.pojo.entity.User;
import com.rauio.smartdangjian.server.user.service.UserService;

import cn.dev33.satoken.stp.SaLoginModel;
import cn.dev33.satoken.stp.StpUtil;

@Component
@Profile("dev")
@Order(0)
public class DevAutoLoginFilter extends OncePerRequestFilter {

    private final DevProperties devProperties;
    private final UserService userService;
    private final SessionPrincipalFactory sessionPrincipalFactory;

    public DevAutoLoginFilter(
            DevProperties devProperties, UserService userService, SessionPrincipalFactory sessionPrincipalFactory) {
        this.devProperties = devProperties;
        this.userService = userService;
        this.sessionPrincipalFactory = sessionPrincipalFactory;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String defaultDevUserId = devProperties.getDefaultUserId();
        if (defaultDevUserId != null && !defaultDevUserId.isEmpty()) {
            ensureDevLogin(defaultDevUserId);
        }
        chain.doFilter(request, response);
    }

    private void ensureDevLogin(String defaultDevUserId) {
        boolean login = StpUtil.isLogin();
        if (!login) {
            StpUtil.login(defaultDevUserId, SaLoginModel.create().setTimeout(86400));
        }
        if (!login || !sessionPrincipalFactory.hasCurrentSessionPrincipal()) {
            String principalUserId = login ? StpUtil.getLoginIdAsString() : defaultDevUserId;
            User user = userService.getById(principalUserId);
            if (user != null) {
                sessionPrincipalFactory.bindCurrentSession(user, devProperties.getDefaultUserType());
            } else {
                sessionPrincipalFactory.bindCurrentSession(principalUserId, devProperties.getDefaultUserType());
            }
        }
    }
}
