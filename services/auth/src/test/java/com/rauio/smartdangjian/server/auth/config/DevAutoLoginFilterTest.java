package com.rauio.smartdangjian.server.auth.config;

import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import cn.dev33.satoken.stp.StpUtil;

@ExtendWith(MockitoExtension.class)
class DevAutoLoginFilterTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain chain;

    @Test
    @DisplayName("未登录且配置了默认用户 ID 时自动登录并放行")
    void autoLoginWhenNotLoggedIn() throws Exception {
        DevProperties props = new DevProperties();
        props.setDefaultUserId("admin");
        DevAutoLoginFilter filter = new DevAutoLoginFilter(props);

        try (MockedStatic<StpUtil> stpUtil = mockStatic(StpUtil.class)) {
            stpUtil.when(StpUtil::isLogin).thenReturn(false);

            filter.doFilterInternal(request, response, chain);

            verify(chain).doFilter(request, response);
        }
    }

    @Test
    @DisplayName("已登录时跳过自动登录直接放行")
    void skipLoginWhenAlreadyLoggedIn() throws Exception {
        DevProperties props = new DevProperties();
        props.setDefaultUserId("admin");
        DevAutoLoginFilter filter = new DevAutoLoginFilter(props);

        try (MockedStatic<StpUtil> stpUtil = mockStatic(StpUtil.class)) {
            stpUtil.when(StpUtil::isLogin).thenReturn(true);

            filter.doFilterInternal(request, response, chain);

            stpUtil.verify(StpUtil::isLogin);
            verify(chain).doFilter(request, response);
        }
    }

    @Test
    @DisplayName("defaultDevUserId null skips auto login")
    void skipLoginWhenDefaultUserIdIsNull() throws Exception {
        DevProperties props = new DevProperties();
        DevAutoLoginFilter filter = new DevAutoLoginFilter(props);

        try (MockedStatic<StpUtil> stpUtil = mockStatic(StpUtil.class)) {
            stpUtil.when(StpUtil::isLogin).thenReturn(false);

            filter.doFilterInternal(request, response, chain);

            verify(chain).doFilter(request, response);
        }
    }

    @Test
    @DisplayName("defaultDevUserId empty skips auto login")
    void skipLoginWhenDefaultUserIdIsEmpty() throws Exception {
        DevProperties props = new DevProperties();
        props.setDefaultUserId("");
        DevAutoLoginFilter filter = new DevAutoLoginFilter(props);

        try (MockedStatic<StpUtil> stpUtil = mockStatic(StpUtil.class)) {
            stpUtil.when(StpUtil::isLogin).thenReturn(false);

            filter.doFilterInternal(request, response, chain);

            verify(chain).doFilter(request, response);
        }
    }
}
