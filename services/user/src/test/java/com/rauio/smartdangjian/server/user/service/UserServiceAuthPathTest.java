package com.rauio.smartdangjian.server.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;
import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.annotation.Cacheable;

import com.rauio.smartdangjian.exception.BusinessException;
import com.rauio.smartdangjian.server.user.pojo.convertor.UserConvertor;
import com.rauio.smartdangjian.server.user.pojo.entity.User;
import com.rauio.smartdangjian.server.user.pojo.response.UserResponse;

import cn.dev33.satoken.stp.StpUtil;

/**
 * 用户认证路径 Service 行为：凭据查询返回的实体携带密码哈希，必须实时查库；
 * 缓存会因 {@code @JsonProperty(WRITE_ONLY)} 丢失密码，导致二次登录失败。
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("用户认证路径 Service 行为")
class UserServiceAuthPathTest {

    private static final Set<String> CREDENTIAL_QUERY_METHODS =
            Set.of("getByPassport", "getByUsername", "getByEmail", "getByPhone");

    @Mock
    private UserConvertor convertor;

    @Spy
    @InjectMocks
    private UserService userService;

    @Test
    @DisplayName("凭据查询方法不得标注 @Cacheable，否则缓存命中时密码哈希丢失")
    void credentialQueriesAreNotCacheable() {
        for (Method method : UserService.class.getDeclaredMethods()) {
            if (CREDENTIAL_QUERY_METHODS.contains(method.getName())) {
                assertThat(method.isAnnotationPresent(Cacheable.class))
                        .as("%s 返回带密码哈希的 User 实体，缓存会丢失 password 导致登录失败", method.getName())
                        .isFalse();
            }
        }
    }

    @Test
    @DisplayName("getCurrentUserProfile 未登录时抛出用户不存在异常")
    void profileThrowsWhenNotLoggedIn() {
        try (MockedStatic<StpUtil> stpUtil = mockStatic(StpUtil.class)) {
            stpUtil.when(StpUtil::isLogin).thenReturn(false);

            assertThatThrownBy(() -> userService.getCurrentUserProfile())
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("用户不存在或登录状态已失效");
        }
    }

    @Test
    @DisplayName("getCurrentUserProfile 返回当前登录用户的视图对象")
    void profileReturnsCurrentUserView() {
        User stored = User.builder().id(7L).username("zhangsan").build();
        UserResponse expected = new UserResponse();
        expected.setId(7L);
        expected.setUsername("zhangsan");

        try (MockedStatic<StpUtil> stpUtil = mockStatic(StpUtil.class)) {
            stpUtil.when(StpUtil::isLogin).thenReturn(true);
            stpUtil.when(StpUtil::getLoginIdAsString).thenReturn("7");
            doReturn(stored).when(userService).getById(7L);
            when(convertor.toResponse(stored)).thenReturn(expected);

            assertThat(userService.getCurrentUserProfile()).isSameAs(expected);
        }
    }
}
