package com.rauio.smartdangjian.security;

import com.rauio.smartdangjian.utils.spec.UserType;

/**
 * 无状态 JWT 场景下的轻量当前用户载体。
 *
 * <p>身份要素直接来自访问令牌 claims（role/uni），不再依赖服务端会话；
 * 由 {@code SecurityUtils.getCurrentUser()} 在请求上下文中组装。
 */
public record JwtUserPrincipal(Long id, UserType userType, String universityId) implements CurrentUserPrincipal {

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public UserType getUserType() {
        return userType;
    }

    @Override
    public String getUniversityId() {
        return universityId;
    }
}
