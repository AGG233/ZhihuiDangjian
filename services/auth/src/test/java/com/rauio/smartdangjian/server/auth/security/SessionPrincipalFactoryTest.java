package com.rauio.smartdangjian.server.auth.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import com.rauio.smartdangjian.security.SessionUserPrincipal;
import com.rauio.smartdangjian.server.user.pojo.entity.User;
import com.rauio.smartdangjian.utils.spec.UserType;

import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.stp.StpUtil;

class SessionPrincipalFactoryTest {

    private final SessionPrincipalFactory factory = new SessionPrincipalFactory();

    @Test
    @DisplayName("toSessionPrincipal 默认使用用户自身类型")
    void toSessionPrincipalUsesUserType() {
        User user = User.builder()
                .id(1L)
                .universityId("uni-1")
                .userType(UserType.STUDENT)
                .build();

        SessionUserPrincipal principal = factory.toSessionPrincipal(user);

        assertThat(principal.getId()).isEqualTo(1L);
        assertThat(principal.getUniversityId()).isEqualTo("uni-1");
        assertThat(principal.getUserType()).isEqualTo(UserType.STUDENT);
    }

    @Test
    @DisplayName("toSessionPrincipal 支持覆盖用户类型以模拟 dev 默认管理员")
    void toSessionPrincipalSupportsUserTypeOverride() {
        User user = User.builder()
                .id(1L)
                .universityId("uni-1")
                .userType(UserType.STUDENT)
                .build();

        SessionUserPrincipal principal = factory.toSessionPrincipal(user, UserType.MANAGER);

        assertThat(principal.getId()).isEqualTo(1L);
        assertThat(principal.getUniversityId()).isEqualTo("uni-1");
        assertThat(principal.getUserType()).isEqualTo(UserType.MANAGER);
    }

    @Test
    @DisplayName("bindCurrentSession 支持用用户 ID 创建临时 dev 管理员上下文")
    void bindCurrentSessionSupportsTemporaryDevPrincipal() {
        SaSession session = org.mockito.Mockito.mock(SaSession.class);

        try (MockedStatic<StpUtil> stpUtil = org.mockito.Mockito.mockStatic(StpUtil.class)) {
            stpUtil.when(StpUtil::getSession).thenReturn(session);

            factory.bindCurrentSession("1", UserType.MANAGER);

            org.mockito.ArgumentCaptor<Object> captor = org.mockito.ArgumentCaptor.forClass(Object.class);
            org.mockito.Mockito.verify(session)
                    .set(org.mockito.Mockito.eq(SessionPrincipalFactory.SESSION_USER_KEY), captor.capture());
            assertThat(captor.getValue()).isInstanceOf(SessionUserPrincipal.class);
            SessionUserPrincipal principal = (SessionUserPrincipal) captor.getValue();
            assertThat(principal.getId()).isEqualTo(1L);
            assertThat(principal.getUserType()).isEqualTo(UserType.MANAGER);
            assertThat(principal.getUniversityId()).isNull();
        }
    }

    @Test
    @DisplayName("bindCurrentSession 在临时 dev 用户 ID 非数字时抛出明确配置错误")
    void bindCurrentSessionRejectsNonNumericTemporaryDevPrincipalId() {
        assertThatThrownBy(() -> factory.bindCurrentSession("admin", UserType.MANAGER))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Dev default user id must be numeric");
    }
}
