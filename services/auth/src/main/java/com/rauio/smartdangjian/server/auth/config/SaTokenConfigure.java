package com.rauio.smartdangjian.server.auth.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.rauio.smartdangjian.server.auth.service.StatelessTokenGuard;

import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.stp.StpUtil;

@Configuration
public class SaTokenConfigure implements WebMvcConfigurer {

    private static final String[] EXCLUDE_PATHS = {
        "/api/auth/login",
        "/api/auth/refresh",
        "/api/auth/captcha/**",
        "/api/auth/register",
        "/api/schools/list",
        "/v3/api-docs/**",
        "/swagger-ui/**",
        "/swagger-ui.html",
        "/webjars/**",
        "/error"
    };

    private final StatelessTokenGuard statelessTokenGuard;
    private final Environment environment;

    public SaTokenConfigure(StatelessTokenGuard statelessTokenGuard, Environment environment) {
        this.statelessTokenGuard = statelessTokenGuard;
        this.environment = environment;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new SaInterceptor(handle -> StpUtil.checkLogin()))
                .addPathPatterns("/**")
                .excludePathPatterns(EXCLUDE_PATHS);
        // 令牌版本号强制下线校验仅在生产链路启用：dev/test 环境跳过对外部 Redis 的依赖
        if (isProdProfile()) {
            registry.addInterceptor(new SaInterceptor(handle -> statelessTokenGuard.verify()))
                    .addPathPatterns("/**")
                    .excludePathPatterns(EXCLUDE_PATHS);
        }
    }

    private boolean isProdProfile() {
        for (String profile : environment.getActiveProfiles()) {
            if ("prod".equals(profile)) {
                return true;
            }
        }
        return false;
    }
}
