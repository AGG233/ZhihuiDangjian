package com.rauio.smartdangjian.server.ai.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rauio.smartdangjian.server.user.service.UserService;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("java:S3011")
class RateLimitConfigTest {

    @Captor
    private ArgumentCaptor<HandlerInterceptor> interceptorCaptor;

    @Test
    @DisplayName("启用限流时注册拦截器到 /api/ai/chat/** 路径")
    void addInterceptorsWhenEnabled() {
        var userService = mock(UserService.class);
        var objectMapper = mock(ObjectMapper.class);
        var config = new RateLimitConfig(userService, objectMapper);
        ReflectionTestUtils.setField(config, "enabled", true);
        ReflectionTestUtils.setField(config, "requestsPerMinute", 10);

        var registry = mock(InterceptorRegistry.class);
        var registration = mock(InterceptorRegistration.class);
        org.mockito.Mockito.when(registry.addInterceptor(any())).thenReturn(registration);

        config.addInterceptors(registry);

        verify(registry).addInterceptor(interceptorCaptor.capture());
        assertThat(interceptorCaptor.getValue()).isInstanceOf(HandlerInterceptor.class);
        verify(registration).addPathPatterns("/api/ai/chat/**");
    }

    @Test
    @DisplayName("禁用限流时跳过拦截器注册")
    void skipInterceptorsWhenDisabled() {
        var userService = mock(UserService.class);
        var objectMapper = mock(ObjectMapper.class);
        var config = new RateLimitConfig(userService, objectMapper);
        ReflectionTestUtils.setField(config, "enabled", false);

        var registry = mock(InterceptorRegistry.class);

        config.addInterceptors(registry);

        verify(registry, never()).addInterceptor(any());
    }

    private HandlerInterceptor captureInterceptor(UserService userService, ObjectMapper objectMapper) {
        var config = new RateLimitConfig(userService, objectMapper);
        ReflectionTestUtils.setField(config, "enabled", true);
        ReflectionTestUtils.setField(config, "requestsPerMinute", 10);

        var registry = mock(InterceptorRegistry.class);
        var registration = mock(InterceptorRegistration.class);
        when(registry.addInterceptor(any())).thenReturn(registration);

        config.addInterceptors(registry);

        verify(registry).addInterceptor(interceptorCaptor.capture());
        return interceptorCaptor.getValue();
    }

    @Test
    @DisplayName("拦截器在请求频率未超限时返回 true")
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
    @DisplayName("拦截器在请求频率超过限制时返回 false 并写入 429")
    void interceptorRejectsRequestWhenRateExceeded() throws Exception {
        var userService = mock(UserService.class);
        var objectMapper = mock(ObjectMapper.class);
        var interceptor = captureInterceptor(userService, objectMapper);

        var request = mock(HttpServletRequest.class);
        var response = mock(HttpServletResponse.class);
        var writer = mock(java.io.PrintWriter.class);
        when(response.getWriter()).thenReturn(writer);
        when(userService.getCurrentUserId()).thenReturn("user-1");

        // 10 requests within limit (requestsPerMinute = 10)
        for (int i = 0; i < 10; i++) {
            assertThat(interceptor.preHandle(request, response, new Object())).isTrue();
        }

        // 11th request exceeds limit
        assertThat(interceptor.preHandle(request, response, new Object())).isFalse();
        verify(response).setStatus(429);
    }

    @Test
    @DisplayName("interceptor removes old window entries via removeIf lambda")
    void interceptorCleansUpOldWindowEntries() throws Exception {
        var userService = mock(UserService.class);
        var objectMapper = mock(ObjectMapper.class);
        var interceptor = captureInterceptor(userService, objectMapper);

        var request = mock(HttpServletRequest.class);
        var response = mock(HttpServletResponse.class);
        when(userService.getCurrentUserId()).thenReturn("cleanup-user");

        // Access private userCounters field to inject a stale entry
        Field countersField = interceptor.getClass().getDeclaredField("userCounters");
        countersField.setAccessible(true);
        @SuppressWarnings("unchecked")
        Map<String, ConcurrentHashMap<Long, AtomicInteger>> userCounters =
                (Map<String, ConcurrentHashMap<Long, AtomicInteger>>) countersField.get(interceptor);
        // Inject an old window with some requests
        ConcurrentHashMap<Long, AtomicInteger> oldCounters = new ConcurrentHashMap<>();
        oldCounters.put(1L, new AtomicInteger(3));
        userCounters.put("cleanup-user", oldCounters);

        // Make a new request - should trigger removeIf and purge the stale entry
        boolean result = interceptor.preHandle(request, response, new Object());

        assertThat(result).isTrue();
        // The old window (key=1) should be removed; only current window remains
        ConcurrentHashMap<Long, AtomicInteger> finalCounters = userCounters.get("cleanup-user");
        assertThat(finalCounters).hasSize(1);
        assertThat(finalCounters.containsKey(1L)).isFalse();
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
