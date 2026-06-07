package com.rauio.smartdangjian.server.auth.security;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;

import com.rauio.smartdangjian.security.CurrentUserPrincipal;
import com.rauio.smartdangjian.security.CurrentUserProvider;
import com.rauio.smartdangjian.security.LoginUser;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.SaTokenContextException;
import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.stp.StpUtil;

@Component
@ConditionalOnMissingBean(StpUtil.class)
public class SaTokenCurrentUserProvider implements CurrentUserProvider {

    @Override
    public String getCurrentUserId() {
        try {
            if (!StpUtil.isLogin()) {
                throw notLogin();
            }
            return StpUtil.getLoginIdAsString();
        } catch (SaTokenContextException e) {
            throw notLogin();
        }
    }

    @Override
    public String getCurrentUserRole() {
        CurrentUserPrincipal user = getSessionUser();
        if (user.getUserType() == null) {
            throw notLogin();
        }
        return user.getUserType().name();
    }

    @Override
    public boolean hasRole(String role) {
        try {
            return StpUtil.hasRole(role);
        } catch (SaTokenContextException e) {
            return false;
        }
    }

    @Override
    public LoginUser getCurrentUser() {
        CurrentUserPrincipal principal = getSessionUser();
        return LoginUser.builder()
                .id(String.valueOf(principal.getId()))
                .userType(principal.getUserType())
                .role(principal.getUserType() != null ? principal.getUserType().name() : null)
                .universityId(principal.getUniversityId())
                .build();
    }

    private CurrentUserPrincipal getSessionUser() {
        try {
            if (!StpUtil.isLogin()) {
                throw notLogin();
            }
            SaSession session = StpUtil.getSession();
            if (session == null) {
                throw notLogin();
            }
            Object user = session.get(SessionPrincipalFactory.SESSION_USER_KEY);
            if (user instanceof CurrentUserPrincipal) {
                return (CurrentUserPrincipal) user;
            }
            throw notLogin();
        } catch (SaTokenContextException e) {
            throw notLogin();
        }
    }

    private NotLoginException notLogin() {
        return new NotLoginException(NotLoginException.DEFAULT_MESSAGE, StpUtil.TYPE, NotLoginException.NOT_TOKEN);
    }
}
