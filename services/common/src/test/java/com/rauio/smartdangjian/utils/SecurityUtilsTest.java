package com.rauio.smartdangjian.utils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mockStatic;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import com.rauio.smartdangjian.security.CurrentUserPrincipal;
import com.rauio.smartdangjian.utils.spec.UserType;

import cn.dev33.satoken.exception.SaTokenContextException;
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
    @DisplayName("getCurrentUser 已登录时从 JWT claims 组装 principal")
    void getCurrentUserAssemblesPrincipalFromClaims() {
        stpUtilMock.when(StpUtil::isLogin).thenReturn(true);
        stpUtilMock.when(StpUtil::getLoginIdDefaultNull).thenReturn(7L);
        stpUtilMock.when(() -> StpUtil.getExtra("role")).thenReturn("SCHOOL");
        stpUtilMock.when(() -> StpUtil.getExtra("uni")).thenReturn("uni-9");

        CurrentUserPrincipal result = SecurityUtils.getCurrentUser();

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(7L);
        assertThat(result.getUserType()).isEqualTo(UserType.SCHOOL);
        assertThat(result.getUniversityId()).isEqualTo("uni-9");
    }

    @Test
    @DisplayName("getCurrentUser 令牌未携带角色 claim 时返回 null")
    void getCurrentUserReturnsNullWhenRoleClaimMissing() {
        stpUtilMock.when(StpUtil::isLogin).thenReturn(true);
        stpUtilMock.when(StpUtil::getLoginIdDefaultNull).thenReturn(7L);

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
        stpUtilMock.when(StpUtil::isLogin).thenReturn(true);
        stpUtilMock.when(StpUtil::getLoginIdDefaultNull).thenReturn(7L);
        stpUtilMock.when(() -> StpUtil.getExtra("role")).thenReturn("MANAGER");
        stpUtilMock.when(() -> StpUtil.getExtra("uni")).thenReturn("uni-1");

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
