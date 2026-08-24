package com.rauio.smartdangjian.server.auth.service;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import com.rauio.smartdangjian.exception.BusinessException;

import cn.dev33.satoken.stp.StpUtil;

@ExtendWith(MockitoExtension.class)
class StatelessTokenGuardTest {

    @Mock
    private TokenVersionService tokenVersionService;

    @InjectMocks
    private StatelessTokenGuard guard;

    private MockedStatic<StpUtil> stpUtilMock;

    @org.junit.jupiter.api.AfterEach
    void tearDown() {
        if (stpUtilMock != null) {
            stpUtilMock.close();
        }
    }

    private void stubLogin(Long userId, Object versionClaim) {
        stpUtilMock = Mockito.mockStatic(StpUtil.class);
        stpUtilMock.when(StpUtil::getLoginIdDefaultNull).thenReturn(userId);
        if (userId != null) {
            stpUtilMock.when(StpUtil::getLoginIdAsString).thenReturn(String.valueOf(userId));
        }
        stpUtilMock.when(() -> StpUtil.getExtra("ver")).thenReturn(versionClaim);
    }

    @Test
    @DisplayName("版本号与 Redis 当前值一致时放行")
    void passesWhenClaimMatchesCurrentVersion() {
        stubLogin(7L, 2L);
        when(tokenVersionService.current(7L)).thenReturn(2L);

        assertThatCode(() -> guard.verify()).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("claim 版本号落后于 Redis 当前值时拒绝（强制下线生效）")
    void rejectsWhenClaimBehindCurrentVersion() {
        stubLogin(7L, 1L);
        when(tokenVersionService.current(7L)).thenReturn(2L);

        assertThatThrownBy(() -> guard.verify())
                .isInstanceOf(BusinessException.class)
                .extracting("code")
                .isEqualTo(com.rauio.smartdangjian.server.auth.constants.AuthErrorConstants.UNAUTHORIZED);
    }

    @Test
    @DisplayName("令牌未携带版本号 claim 时按 0 处理，Redis 未 bump 则放行")
    void missingClaimTreatedAsZero() {
        stubLogin(7L, null);
        when(tokenVersionService.current(7L)).thenReturn(0L);

        assertThatCode(() -> guard.verify()).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("未登录上下文直接放行（由 checkLogin 拦截）")
    void skipsWhenNotLoggedIn() {
        stubLogin(null, null);
        assertThatCode(() -> guard.verify()).doesNotThrowAnyException();
    }
}
