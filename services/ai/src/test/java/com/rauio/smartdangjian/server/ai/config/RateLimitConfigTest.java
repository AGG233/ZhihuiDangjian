package com.rauio.smartdangjian.server.ai.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rauio.smartdangjian.server.user.service.UserService;

@ExtendWith(MockitoExtension.class)
class RateLimitConfigTest {

    @Captor
    private ArgumentCaptor<HandlerInterceptor> interceptorCaptor;

    @Test
    @DisplayName("启用限流时注册拦\u622A\u5668\u5230\u5173\u952E\u654F\u611F\u63A5\u53E3\u8DEF\u5F84")
    void addInterceptorsWhenEnabled() {
        var userService = mock(UserService.class);
        var objectMapper = mock(ObjectMapper.class);
        var aiProperties = new AiProperties();
        aiProperties.getRateLimit().setEnabled(true);
        aiProperties.getRateLimit().setRequestsPerMinute(10);
        aiProperties
                .getRateLimit()
                .setPathPatterns(List.of(
                        "/api/ai/chat/**",
                        "/api/auth/login",
                        "/api/auth/register",
                        "/api/auth/captcha/**",
                        "/api/resource/files/upload",
                        "/api/user/users/search"));

        var config = new RateLimitConfig(userService, objectMapper, aiProperties);

        var registry = mock(InterceptorRegistry.class);
        var registration = mock(InterceptorRegistration.class);
        org.mockito.Mockito.when(registry.addInterceptor(any())).thenReturn(registration);

        config.addInterceptors(registry);

        verify(registry).addInterceptor(interceptorCaptor.capture());
        assertThat(interceptorCaptor.getValue()).isInstanceOf(HandlerInterceptor.class);
        verify(registration)
                .addPathPatterns(List.of(
                        "/api/ai/chat/**",
                        "/api/auth/login",
                        "/api/auth/register",
                        "/api/auth/captcha/**",
                        "/api/resource/files/upload",
                        "/api/user/users/search"));
    }

    @Test
    @DisplayName("\u7981\u7528\u9650\u6D41\u65F6\u8DF3\u8FC7\u62E6\u622A\u5668\u6CE8\u518C")
    void skipInterceptorsWhenDisabled() {
        var userService = mock(UserService.class);
        var objectMapper = mock(ObjectMapper.class);
        var aiProperties = new AiProperties();
        aiProperties.getRateLimit().setEnabled(false);

        var config = new RateLimitConfig(userService, objectMapper, aiProperties);

        var registry = mock(InterceptorRegistry.class);

        config.addInterceptors(registry);

        verify(registry, never()).addInterceptor(any());
    }

    private HandlerInterceptor captureInterceptor(UserService userService, ObjectMapper objectMapper) {
        var aiProperties = new AiProperties();
        aiProperties.getRateLimit().setEnabled(true);
        aiProperties.getRateLimit().setRequestsPerMinute(10);
        aiProperties.getRateLimit().setPathPatterns(List.of("/api/ai/chat/**"));

        var config = new RateLimitConfig(userService, objectMapper, aiProperties);

        var registry = mock(InterceptorRegistry.class);
        var registration = mock(InterceptorRegistration.class);
        when(registry.addInterceptor(any())).thenReturn(registration);

        config.addInterceptors(registry);

        verify(registry).addInterceptor(interceptorCaptor.capture());
        return interceptorCaptor.getValue();
    }

    @Test
    @DisplayName("\u62E6\u622A\u5668\u5728\u8BF7\u6C42\u9891\u7387\u672A\u8D85\u9650\u65F6\u8FD4\u56DE true")
    void interceptorAllowsRequestWithinLimit() throws Exception {
        var userService = mock(UserService.class);
        var objectMapper = mock(ObjectMapper.class);
        var interceptor = captureInterceptor(userService, objectMapper);
        var request = mock(HttpServletRequest.class);
        var response = mock(HttpServletResponse.class);
        when(userService.getCurrentUserId()).thenReturn("user-1");
        boolean result = interceptor.preHandle(request, response, new Object());
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName(
            "\u62E6\u622A\u5668\u5728\u8BF7\u6C42\u9891\u7387\u8D85\u8FC7\u9650\u5236\u65F6\u8FD4\u56DE false \u5E76\u5199\u5165 429")
    void interceptorRejectsRequestWhenRateExceeded() throws Exception {
        var userService = mock(UserService.class);
        var objectMapper = mock(ObjectMapper.class);
        var interceptor = captureInterceptor(userService, objectMapper);
        var request = mock(HttpServletRequest.class);
        var response = mock(HttpServletResponse.class);
        var writer = mock(java.io.PrintWriter.class);
        when(response.getWriter()).thenReturn(writer);
        when(userService.getCurrentUserId()).thenReturn("user-1");
        for (int i = 0; i < 10; i++) {
            assertThat(interceptor.preHandle(request, response, new Object())).isTrue();
        }
        assertThat(interceptor.preHandle(request, response, new Object())).isFalse();
        verify(response).setStatus(429);
    }

    @Test
    @DisplayName("interceptor falls back to remote addr on exception")
    void interceptorFallsBackToRemoteAddrOnException() throws Exception {
        var userService = mock(UserService.class);
        var objectMapper = mock(ObjectMapper.class);
        var interceptor = captureInterceptor(userService, objectMapper);
        var request = mock(HttpServletRequest.class);
        var response = mock(HttpServletResponse.class);
        when(request.getRemoteAddr()).thenReturn("192.168.1.1");
        when(userService.getCurrentUserId()).thenThrow(new RuntimeException("Session expired"));
        boolean result = interceptor.preHandle(request, response, new Object());
        assertThat(result).isTrue();
        verify(request).getRemoteAddr();
    }

    @Test
    @DisplayName("interceptor falls back to remote addr when userId null")
    void interceptorFallsBackToRemoteAddrWhenUserIdNull() throws Exception {
        var userService = mock(UserService.class);
        var objectMapper = mock(ObjectMapper.class);
        var interceptor = captureInterceptor(userService, objectMapper);
        var request = mock(HttpServletRequest.class);
        var response = mock(HttpServletResponse.class);
        when(request.getRemoteAddr()).thenReturn("10.0.0.1");
        when(userService.getCurrentUserId()).thenReturn(null);
        boolean result = interceptor.preHandle(request, response, new Object());
        assertThat(result).isTrue();
        verify(request).getRemoteAddr();
    }
}
