package com.rauio.smartdangjian.server.auth.security;

import org.springframework.stereotype.Component;

import com.rauio.smartdangjian.security.SessionUserPrincipal;
import com.rauio.smartdangjian.server.user.pojo.entity.User;
import com.rauio.smartdangjian.utils.spec.UserType;

import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.stp.StpUtil;

@Component
public class SessionPrincipalFactory {

    public static final String SESSION_USER_KEY = "user";

    public SessionUserPrincipal toSessionPrincipal(User user) {
        return toSessionPrincipal(user, user.getUserType());
    }

    public SessionUserPrincipal toSessionPrincipal(User user, UserType userType) {
        return SessionUserPrincipal.builder()
                .id(user.getId())
                .userType(userType)
                .universityId(user.getUniversityId())
                .build();
    }

    public void bindCurrentSession(User user) {
        StpUtil.getSession().set(SESSION_USER_KEY, toSessionPrincipal(user));
    }

    public void bindCurrentSession(User user, UserType userType) {
        StpUtil.getSession().set(SESSION_USER_KEY, toSessionPrincipal(user, userType));
    }

    public void bindCurrentSession(String userId, UserType userType) {
        StpUtil.getSession()
                .set(
                        SESSION_USER_KEY,
                        SessionUserPrincipal.builder()
                                .id(Long.valueOf(userId))
                                .userType(userType)
                                .build());
    }

    public boolean hasCurrentSessionPrincipal() {
        SaSession session = StpUtil.getSession();
        return session != null && session.get(SESSION_USER_KEY) instanceof SessionUserPrincipal;
    }
}
