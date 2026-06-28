package com.rauio.smartdangjian.server.auth.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.env.Environment;
import org.springframework.web.servlet.config.annotation.InterceptorRegistration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;

import cn.dev33.satoken.interceptor.SaInterceptor;

@ExtendWith(MockitoExtension.class)
class SaTokenConfigureTest {

    private final Environment environment = mock(Environment.class);
    private final SaTokenConfigure config = new SaTokenConfigure(environment);

    @Captor
    private ArgumentCaptor<SaInterceptor> interceptorCaptor;

    @Test
    @DisplayName("addInterceptors 注册 SaInterceptor 到注册表")
    void addInterceptors() {
        var registry = mock(InterceptorRegistry.class);
        var registration = mock(InterceptorRegistration.class);
        when(environment.getActiveProfiles()).thenReturn(new String[] {"dev"});
        when(registry.addInterceptor(any())).thenReturn(registration);
        when(registration.addPathPatterns(anyString())).thenReturn(registration);
        when(registration.excludePathPatterns(any(String[].class))).thenReturn(registration);

        config.addInterceptors(registry);

        verify(registry).addInterceptor(interceptorCaptor.capture());
        assertThat(interceptorCaptor.getValue()).isInstanceOf(SaInterceptor.class);
        assertThat(interceptorCaptor.getValue().isAnnotation).isTrue();
        verify(registration).addPathPatterns("/**");
        verify(registration)
                .excludePathPatterns(
                        "/api/auth/login", "/api/auth/captcha/**", "/api/auth/register", "/api/school/all", "/error");
        verify(registration)
                .excludePathPatterns(
                        "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html", "/doc.html", "/webjars/**");
    }

    @Test
    @DisplayName("生产环境不排除 Swagger 文档路径")
    void prodProfileDoesNotExcludeSwaggerPaths() {
        var registry = mock(InterceptorRegistry.class);
        var registration = mock(InterceptorRegistration.class);
        when(environment.getActiveProfiles()).thenReturn(new String[] {"prod"});
        when(registry.addInterceptor(any())).thenReturn(registration);
        when(registration.addPathPatterns(anyString())).thenReturn(registration);
        when(registration.excludePathPatterns(any(String[].class))).thenReturn(registration);

        config.addInterceptors(registry);

        verify(registration)
                .excludePathPatterns(
                        "/api/auth/login", "/api/auth/captcha/**", "/api/auth/register", "/api/school/all", "/error");
        verify(registration, never())
                .excludePathPatterns(
                        "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html", "/doc.html", "/webjars/**");
    }
}
