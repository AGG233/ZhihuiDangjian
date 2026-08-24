package com.rauio.smartdangjian.server.auth.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
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

import com.rauio.smartdangjian.server.auth.service.StatelessTokenGuard;

import cn.dev33.satoken.interceptor.SaInterceptor;

@ExtendWith(MockitoExtension.class)
class SaTokenConfigureTest {

    @Captor
    private ArgumentCaptor<SaInterceptor> interceptorCaptor;

    private InterceptorRegistration registration;

    private Environment environmentWith(String... profiles) {
        Environment environment = mock(Environment.class);
        when(environment.getActiveProfiles()).thenReturn(profiles);
        return environment;
    }

    private InterceptorRegistry registryAcceptingAny() {
        InterceptorRegistry registry = mock(InterceptorRegistry.class);
        registration = mock(InterceptorRegistration.class);
        when(registry.addInterceptor(any())).thenReturn(registration);
        when(registration.addPathPatterns(anyString())).thenReturn(registration);
        when(registration.excludePathPatterns(any(String[].class))).thenReturn(registration);
        return registry;
    }

    @Test
    @DisplayName("addInterceptors 注册 SaInterceptor 到注册表")
    void addInterceptors() {
        var registry = registryAcceptingAny();
        var config = new SaTokenConfigure(mock(StatelessTokenGuard.class), environmentWith());

        config.addInterceptors(registry);

        verify(registry).addInterceptor(interceptorCaptor.capture());
        assertThat(interceptorCaptor.getValue()).isInstanceOf(SaInterceptor.class);
        verify(registration).addPathPatterns("/**");
        verify(registration)
                .excludePathPatterns(
                        "/api/auth/login",
                        "/api/auth/refresh",
                        "/api/auth/captcha/**",
                        "/api/auth/register",
                        "/api/schools/list",
                        "/v3/api-docs/**",
                        "/swagger-ui/**",
                        "/swagger-ui.html",
                        "/webjars/**",
                        "/error");
    }

    @Test
    @DisplayName("prod profile 额外注册令牌版本号校验拦截器")
    void prodProfileRegistersStatelessGuardInterceptor() {
        var registry = registryAcceptingAny();
        var config = new SaTokenConfigure(mock(StatelessTokenGuard.class), environmentWith("prod"));

        config.addInterceptors(registry);

        verify(registry, times(2)).addInterceptor(interceptorCaptor.capture());
        assertThat(interceptorCaptor.getAllValues()).hasSize(2).allSatisfy(i -> assertThat(i)
                .isInstanceOf(SaInterceptor.class));
    }

    @Test
    @DisplayName("dev profile 不注册版本号校验拦截器")
    void devProfileSkipsStatelessGuard() {
        var registry = registryAcceptingAny();
        var config = new SaTokenConfigure(mock(StatelessTokenGuard.class), environmentWith("dev"));

        config.addInterceptors(registry);

        verify(registry, times(1)).addInterceptor(any(SaInterceptor.class));
    }
}
