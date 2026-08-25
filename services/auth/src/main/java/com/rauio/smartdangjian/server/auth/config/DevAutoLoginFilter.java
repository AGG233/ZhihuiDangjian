package com.rauio.smartdangjian.server.auth.config;

import java.io.IOException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.web.filter.OncePerRequestFilter;

import com.rauio.smartdangjian.server.auth.constants.JwtClaims;
import com.rauio.smartdangjian.server.user.pojo.entity.User;
import com.rauio.smartdangjian.server.user.service.UserService;

import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.stp.parameter.SaLoginParameter;

/**
 * 开发环境自动登录过滤器（仅 dev profile）。
 *
 * <p>Stateless 模式下 RBAC 角色与数据范围隔离均取自 JWT claims，因此自动登录
 * 时按配置的用户 ID 回查用户并写入 {@code role}/{@code uni} claims；用户不存在
 * 或查询不可用时退化为无角色令牌（与历史行为兼容，仅可访问公开与登录态接口）。
 */
@Profile("dev")
@Order(0)
public class DevAutoLoginFilter extends OncePerRequestFilter {

    @Value("${app.dev.default-user-id:}")
    private String defaultDevUserId;

    @Autowired(required = false)
    private UserService userService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        if (!StpUtil.isLogin() && defaultDevUserId != null && !defaultDevUserId.isEmpty()) {
            SaLoginParameter loginParameter = SaLoginParameter.create().setTimeout(86400);
            User user = resolveDevUser();
            if (user != null && user.getId() != null) {
                loginParameter
                        .setExtra(
                                JwtClaims.ROLE,
                                user.getUserType() == null
                                        ? null
                                        : user.getUserType().name())
                        .setExtra(JwtClaims.UNIVERSITY_ID, user.getUniversityId())
                        .setExtra(JwtClaims.TOKEN_VERSION, 0L);
                StpUtil.login(user.getId(), loginParameter);
            } else {
                // 无法解析出用户实体（含非数字 ID 或查询不可用）时退化为字符串 loginId，
                // 不携带角色 claims，仅可访问公开与登录态接口
                StpUtil.login(defaultDevUserId, loginParameter);
            }
        }
        chain.doFilter(request, response);
    }

    private User resolveDevUser() {
        if (userService == null) {
            return null;
        }
        try {
            return userService.getById(Long.valueOf(defaultDevUserId));
        } catch (RuntimeException e) {
            return null;
        }
    }
}
