package com.rauio.smartdangjian.server.auth.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import com.rauio.smartdangjian.security.LoginUser;
import com.rauio.smartdangjian.server.user.pojo.entity.User;
import com.rauio.smartdangjian.utils.spec.UserType;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.SaTokenContextException;
import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.stp.StpUtil;

@ExtendWith(MockitoExtension.class)
class SaTokenCurrentUserProviderTest {

    private final SaTokenCurrentUserProvider provider = new SaTokenCurrentUserProvider();

    @Mock
    private SaSession session;

    @Test
    @DisplayName("getCurrentUserId 用户已登录返回用户ID")
    void getCurrentUserIdWhenLoggedIn() {
        try (MockedStatic<StpUtil> stpUtil = mockStatic(StpUtil.class)) {
            stpUtil.when(StpUtil::isLogin).thenReturn(true);
            stpUtil.when(StpUtil::getLoginIdAsString).thenReturn("1");

            String userId = provider.getCurrentUserId();

            assertThat(userId).isEqualTo("1");
        }
    }

    @Test
    @DisplayName("getCurrentUserId 未登录时抛出 NotLoginException")
    void getCurrentUserIdWhenNotLoggedIn() {
        try (MockedStatic<StpUtil> stpUtil = mockStatic(StpUtil.class)) {
            stpUtil.when(StpUtil::isLogin).thenReturn(false);

            assertThatThrownBy(() -> provider.getCurrentUserId()).isInstanceOf(NotLoginException.class);
        }
    }

    @Test
    @DisplayName("getCurrentUserId SaTokenContextException 时抛出 NotLoginException")
    void getCurrentUserIdWhenContextException() {
        try (MockedStatic<StpUtil> stpUtil = mockStatic(StpUtil.class)) {
            stpUtil.when(StpUtil::isLogin).thenThrow(SaTokenContextException.class);

            assertThatThrownBy(() -> provider.getCurrentUserId()).isInstanceOf(NotLoginException.class);
        }
    }

    @Test
    @DisplayName("getCurrentUserRole 有效用户类型返回角色名称")
    void getCurrentUserRoleWithValidUserType() {
        User user = User.builder().id(1L).userType(UserType.SCHOOL).build();

        try (MockedStatic<StpUtil> stpUtil = mockStatic(StpUtil.class)) {
            stpUtil.when(StpUtil::isLogin).thenReturn(true);
            stpUtil.when(StpUtil::getSession).thenReturn(session);
            when(session.get(SessionPrincipalFactory.SESSION_USER_KEY)).thenReturn(user);

            String role = provider.getCurrentUserRole();

            assertThat(role).isEqualTo(UserType.SCHOOL.name());
        }
    }

    @Test
    @DisplayName("getCurrentUserRole 用户类型为 null 时抛出 NotLoginException")
    void getCurrentUserRoleWithNullUserType() {
        User user = User.builder().id(1L).userType(null).build();

        try (MockedStatic<StpUtil> stpUtil = mockStatic(StpUtil.class)) {
            stpUtil.when(StpUtil::isLogin).thenReturn(true);
            stpUtil.when(StpUtil::getSession).thenReturn(session);
            when(session.get(SessionPrincipalFactory.SESSION_USER_KEY)).thenReturn(user);

            assertThatThrownBy(() -> provider.getCurrentUserRole()).isInstanceOf(NotLoginException.class);
        }
    }

    @Test
    @DisplayName("hasRole 用户拥有角色时返回 true")
    void hasRoleTrue() {
        try (MockedStatic<StpUtil> stpUtil = mockStatic(StpUtil.class)) {
            stpUtil.when(() -> StpUtil.hasRole("MANAGER")).thenReturn(true);

            boolean result = provider.hasRole("MANAGER");

            assertThat(result).isTrue();
        }
    }

    @Test
    @DisplayName("hasRole 用户无角色时返回 false")
    void hasRoleFalse() {
        try (MockedStatic<StpUtil> stpUtil = mockStatic(StpUtil.class)) {
            stpUtil.when(() -> StpUtil.hasRole("STUDENT")).thenReturn(false);

            boolean result = provider.hasRole("STUDENT");

            assertThat(result).isFalse();
        }
    }

    @Test
    @DisplayName("hasRole SaTokenContextException 时返回 false")
    void hasRoleOnContextException() {
        try (MockedStatic<StpUtil> stpUtil = mockStatic(StpUtil.class)) {
            stpUtil.when(() -> StpUtil.hasRole(any())).thenThrow(SaTokenContextException.class);

            boolean result = provider.hasRole("ANY_ROLE");

            assertThat(result).isFalse();
        }
    }

    @Test
    @DisplayName("getCurrentUser 返回当前登录用户信息")
    void getCurrentUser() {
        User user = User.builder()
                .id(1L)
                .userType(UserType.STUDENT)
                .universityId("uni-1")
                .build();

        try (MockedStatic<StpUtil> stpUtil = mockStatic(StpUtil.class)) {
            stpUtil.when(StpUtil::isLogin).thenReturn(true);
            stpUtil.when(StpUtil::getSession).thenReturn(session);
            when(session.get(SessionPrincipalFactory.SESSION_USER_KEY)).thenReturn(user);

            LoginUser result = provider.getCurrentUser();

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo("1");
            assertThat(result.getUserType()).isEqualTo(UserType.STUDENT);
            assertThat(result.getRole()).isEqualTo(UserType.STUDENT.name());
            assertThat(result.getUniversityId()).isEqualTo("uni-1");
        }
    }

    @Test
    @DisplayName("getCurrentUser 用户类型 null 时 role 为 null")
    void getCurrentUserWithNullUserType() {
        User user = User.builder().id(1L).userType(null).build();

        try (MockedStatic<StpUtil> stpUtil = mockStatic(StpUtil.class)) {
            stpUtil.when(StpUtil::isLogin).thenReturn(true);
            stpUtil.when(StpUtil::getSession).thenReturn(session);
            when(session.get(SessionPrincipalFactory.SESSION_USER_KEY)).thenReturn(user);

            LoginUser result = provider.getCurrentUser();

            assertThat(result.getRole()).isNull();
        }
    }

    @Test
    @DisplayName("getCurrentUser 未登录时抛出 NotLoginException")
    void getCurrentUserNotLoggedIn() {
        try (MockedStatic<StpUtil> stpUtil = mockStatic(StpUtil.class)) {
            stpUtil.when(StpUtil::isLogin).thenReturn(false);

            assertThatThrownBy(() -> provider.getCurrentUser()).isInstanceOf(NotLoginException.class);
        }
    }

    @Test
    @DisplayName("getCurrentUser Session 中不存在用户时抛出 NotLoginException")
    void getCurrentUserNoUserInSession() {
        try (MockedStatic<StpUtil> stpUtil = mockStatic(StpUtil.class)) {
            stpUtil.when(StpUtil::isLogin).thenReturn(true);
            stpUtil.when(StpUtil::getSession).thenReturn(session);
            when(session.get(SessionPrincipalFactory.SESSION_USER_KEY)).thenReturn(null);

            assertThatThrownBy(() -> provider.getCurrentUser()).isInstanceOf(NotLoginException.class);
        }
    }
}
