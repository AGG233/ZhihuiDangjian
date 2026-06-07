package com.rauio.smartdangjian.server.auth.config;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import com.rauio.smartdangjian.server.auth.security.SessionPrincipalFactory;
import com.rauio.smartdangjian.server.user.pojo.entity.User;
import com.rauio.smartdangjian.server.user.service.UserService;
import com.rauio.smartdangjian.utils.spec.UserType;

import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.stp.parameter.SaLoginParameter;

@ExtendWith(MockitoExtension.class)
class DevAutoLoginFilterTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain chain;

    @Mock
    private UserService userService;

    @Mock
    private SessionPrincipalFactory sessionPrincipalFactory;

    @Test
    @DisplayName("未登录且配置了默认用户 ID 时自动登录、绑定用户上下文并放行")
    void autoLoginWhenNotLoggedIn() throws Exception {
        DevProperties props = new DevProperties();
        props.setDefaultUserId("1");
        props.setDefaultUserType(UserType.MANAGER);
        User devUser = User.builder().id(1L).build();
        DevAutoLoginFilter filter = new DevAutoLoginFilter(props, userService, sessionPrincipalFactory);

        try (MockedStatic<StpUtil> stpUtil = mockStatic(StpUtil.class)) {
            stpUtil.when(StpUtil::isLogin).thenReturn(false);
            when(userService.getById("1")).thenReturn(devUser);

            filter.doFilterInternal(request, response, chain);

            stpUtil.verify(() -> StpUtil.login(eq("1"), any(SaLoginParameter.class)));
            verify(sessionPrincipalFactory).bindCurrentSession(devUser, UserType.MANAGER);
            verify(chain).doFilter(request, response);
        }
    }

    @Test
    @DisplayName("已登录且已存在 session 用户上下文时跳过自动登录并放行")
    void skipLoginWhenAlreadyLoggedIn() throws Exception {
        DevProperties props = new DevProperties();
        props.setDefaultUserId("admin");
        DevAutoLoginFilter filter = new DevAutoLoginFilter(props, userService, sessionPrincipalFactory);

        try (MockedStatic<StpUtil> stpUtil = mockStatic(StpUtil.class)) {
            stpUtil.when(StpUtil::isLogin).thenReturn(true);
            when(sessionPrincipalFactory.hasCurrentSessionPrincipal()).thenReturn(true);

            filter.doFilterInternal(request, response, chain);

            stpUtil.verify(StpUtil::isLogin);
            stpUtil.verify(() -> StpUtil.login(any(), any(SaLoginParameter.class)), never());
            verify(userService, never()).getById(any());
            verify(sessionPrincipalFactory, never()).bindCurrentSession(any());
            verify(chain).doFilter(request, response);
        }
    }

    @Test
    @DisplayName("已登录但缺少 session 用户上下文时补齐上下文")
    void bindSessionPrincipalWhenAlreadyLoggedInButMissingUserContext() throws Exception {
        DevProperties props = new DevProperties();
        props.setDefaultUserId("1");
        props.setDefaultUserType(UserType.MANAGER);
        User loginUser = User.builder().id(2L).build();
        DevAutoLoginFilter filter = new DevAutoLoginFilter(props, userService, sessionPrincipalFactory);

        try (MockedStatic<StpUtil> stpUtil = mockStatic(StpUtil.class)) {
            stpUtil.when(StpUtil::isLogin).thenReturn(true);
            stpUtil.when(StpUtil::getLoginIdAsString).thenReturn("2");
            when(sessionPrincipalFactory.hasCurrentSessionPrincipal()).thenReturn(false);
            when(userService.getById("2")).thenReturn(loginUser);

            filter.doFilterInternal(request, response, chain);

            stpUtil.verify(() -> StpUtil.login(any(), any(SaLoginParameter.class)), never());
            verify(sessionPrincipalFactory).bindCurrentSession(loginUser, UserType.MANAGER);
            verify(chain).doFilter(request, response);
        }
    }

    @Test
    @DisplayName("已登录但缺少用户上下文且用户不存在时使用当前登录 ID 绑定临时上下文")
    void bindTemporaryPrincipalUsesCurrentLoginIdWhenAlreadyLoggedIn() throws Exception {
        DevProperties props = new DevProperties();
        props.setDefaultUserId("1");
        props.setDefaultUserType(UserType.MANAGER);
        DevAutoLoginFilter filter = new DevAutoLoginFilter(props, userService, sessionPrincipalFactory);

        try (MockedStatic<StpUtil> stpUtil = mockStatic(StpUtil.class)) {
            stpUtil.when(StpUtil::isLogin).thenReturn(true);
            stpUtil.when(StpUtil::getLoginIdAsString).thenReturn("2");
            when(sessionPrincipalFactory.hasCurrentSessionPrincipal()).thenReturn(false);
            when(userService.getById("2")).thenReturn(null);

            filter.doFilterInternal(request, response, chain);

            stpUtil.verify(() -> StpUtil.login(any(), any(SaLoginParameter.class)), never());
            verify(sessionPrincipalFactory).bindCurrentSession("2", UserType.MANAGER);
            verify(chain).doFilter(request, response);
        }
    }

    @Test
    @DisplayName("默认用户不存在时使用配置的用户类型绑定临时 dev 用户上下文")
    void bindTemporaryDevPrincipalWhenDefaultUserDoesNotExist() throws Exception {
        DevProperties props = new DevProperties();
        props.setDefaultUserId("1");
        props.setDefaultUserType(UserType.MANAGER);
        DevAutoLoginFilter filter = new DevAutoLoginFilter(props, userService, sessionPrincipalFactory);

        try (MockedStatic<StpUtil> stpUtil = mockStatic(StpUtil.class)) {
            stpUtil.when(StpUtil::isLogin).thenReturn(false);
            when(userService.getById("1")).thenReturn(null);

            filter.doFilterInternal(request, response, chain);

            stpUtil.verify(() -> StpUtil.login(eq("1"), any(SaLoginParameter.class)));
            verify(sessionPrincipalFactory).bindCurrentSession("1", UserType.MANAGER);
            verify(chain).doFilter(request, response);
        }
    }

    @Test
    @DisplayName("defaultDevUserId null skips auto login")
    void skipLoginWhenDefaultUserIdIsNull() throws Exception {
        DevProperties props = new DevProperties();
        DevAutoLoginFilter filter = new DevAutoLoginFilter(props, userService, sessionPrincipalFactory);

        try (MockedStatic<StpUtil> stpUtil = mockStatic(StpUtil.class)) {
            filter.doFilterInternal(request, response, chain);

            stpUtil.verifyNoInteractions();
            verify(chain).doFilter(request, response);
        }
    }

    @Test
    @DisplayName("defaultDevUserId empty skips auto login")
    void skipLoginWhenDefaultUserIdIsEmpty() throws Exception {
        DevProperties props = new DevProperties();
        props.setDefaultUserId("");
        DevAutoLoginFilter filter = new DevAutoLoginFilter(props, userService, sessionPrincipalFactory);

        try (MockedStatic<StpUtil> stpUtil = mockStatic(StpUtil.class)) {
            filter.doFilterInternal(request, response, chain);

            stpUtil.verifyNoInteractions();
            verify(chain).doFilter(request, response);
        }
    }
}
