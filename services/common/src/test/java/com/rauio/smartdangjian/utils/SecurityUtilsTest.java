package com.rauio.smartdangjian.utils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import java.lang.reflect.Constructor;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import com.rauio.smartdangjian.security.CurrentUserPrincipal;
import com.rauio.smartdangjian.utils.spec.UserType;

import cn.dev33.satoken.exception.SaTokenContextException;
import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.stp.StpUtil;

class SecurityUtilsTest {

    private MockedStatic<StpUtil> stpUtilMock;

    @BeforeEach
    void setUp() {
        stpUtilMock = mockStatic(StpUtil.class);
    }

    @AfterEach
    void tearDown() {
        stpUtilMock.close();
    }

    // ================================================================
    // getCurrentUser
    // ================================================================

    @Test
    @DisplayName("getCurrentUser 未登录时返回 null")
    void getCurrentUserReturnsNullWhenNotLoggedIn() {
        stpUtilMock.when(StpUtil::isLogin).thenReturn(false);

        CurrentUserPrincipal result = SecurityUtils.getCurrentUser();

        assertThat(result).isNull();
    }

    @Test
    @DisplayName("getCurrentUser 已登录时返回 Session 中的 user")
    void getCurrentUserReturnsUserFromSession() {
        CurrentUserPrincipal mockPrincipal = mock(CurrentUserPrincipal.class);
        SaSession session = mock(SaSession.class);
        when(session.get("user")).thenReturn(mockPrincipal);
        stpUtilMock.when(StpUtil::isLogin).thenReturn(true);
        stpUtilMock.when(StpUtil::getSession).thenReturn(session);

        CurrentUserPrincipal result = SecurityUtils.getCurrentUser();

        assertThat(result).isEqualTo(mockPrincipal);
    }

    @Test
    @DisplayName("getCurrentUser Session 中没有 user 时返回 null")
    void getCurrentUserReturnsNullWhenUserNotInSession() {
        SaSession session = mock(SaSession.class);
        when(session.get("user")).thenReturn(null);
        stpUtilMock.when(StpUtil::isLogin).thenReturn(true);
        stpUtilMock.when(StpUtil::getSession).thenReturn(session);

        CurrentUserPrincipal result = SecurityUtils.getCurrentUser();

        assertThat(result).isNull();
    }

    // ================================================================
    // getCurrentUserId
    // ================================================================

    @Test
    @DisplayName("getCurrentUserId 已登录时返回用户 ID")
    void getCurrentUserIdReturnsIdWhenLoggedIn() {
        stpUtilMock.when(StpUtil::isLogin).thenReturn(true);
        stpUtilMock.when(StpUtil::getLoginIdAsString).thenReturn("user-id-123");

        String result = SecurityUtils.getCurrentUserId();

        assertThat(result).isEqualTo("user-id-123");
    }

    @Test
    @DisplayName("getCurrentUserId 未登录时返回 null")
    void getCurrentUserIdReturnsNullWhenNotLoggedIn() {
        stpUtilMock.when(StpUtil::isLogin).thenReturn(false);

        String result = SecurityUtils.getCurrentUserId();

        assertThat(result).isNull();
    }

    // ================================================================
    // getCurrentUserType
    // ================================================================

    @Test
    @DisplayName("getCurrentUserType 已登录时返回用户类型")
    void getCurrentUserTypeReturnsTypeWhenLoggedIn() {
        CurrentUserPrincipal mockPrincipal = mock(CurrentUserPrincipal.class);
        when(mockPrincipal.getUserType()).thenReturn(UserType.MANAGER);
        SaSession session = mock(SaSession.class);
        when(session.get("user")).thenReturn(mockPrincipal);
        stpUtilMock.when(StpUtil::isLogin).thenReturn(true);
        stpUtilMock.when(StpUtil::getSession).thenReturn(session);

        UserType result = SecurityUtils.getCurrentUserType();

        assertThat(result).isEqualTo(UserType.MANAGER);
    }

    @Test
    @DisplayName("getCurrentUserType 未登录时返回 null")
    void getCurrentUserTypeReturnsNullWhenNotLoggedIn() {
        stpUtilMock.when(StpUtil::isLogin).thenReturn(false);

        UserType result = SecurityUtils.getCurrentUserType();

        assertThat(result).isNull();
    }

    @Test
    @DisplayName("getCurrentUser SaTokenContextException 时返回 null")
    void getCurrentUserReturnsNullOnSaTokenContextException() {
        stpUtilMock.when(StpUtil::isLogin).thenThrow(SaTokenContextException.class);

        CurrentUserPrincipal result = SecurityUtils.getCurrentUser();

        assertThat(result).isNull();
    }

    @Test
    @DisplayName("getCurrentUserId SaTokenContextException 时返回 null")
    void getCurrentUserIdReturnsNullOnSaTokenContextException() {
        stpUtilMock.when(StpUtil::isLogin).thenThrow(SaTokenContextException.class);

        String result = SecurityUtils.getCurrentUserId();

        assertThat(result).isNull();
    }

}
