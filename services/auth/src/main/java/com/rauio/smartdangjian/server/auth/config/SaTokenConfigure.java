package com.rauio.smartdangjian.server.auth.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.web.servlet.config.annotation.InterceptorRegistration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.stp.StpUtil;

@Configuration
@EnableConfigurationProperties(AuthProperties.class)
public class SaTokenConfigure implements WebMvcConfigurer {

    private static final String[] PUBLIC_EXCLUDE_PATHS = {
        "/api/auth/login", "/api/auth/captcha/**", "/api/auth/register", "/api/schools/list", "/error"
    };

    private static final String[] DOC_EXCLUDE_PATHS = {
        "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html", "/doc.html", "/webjars/**"
    };

    private final Environment environment;

    public SaTokenConfigure(Environment environment) {
        this.environment = environment;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        InterceptorRegistration registration = registry.addInterceptor(
                        new SaInterceptor(handle -> StpUtil.checkLogin()))
                .addPathPatterns("/**")
                .excludePathPatterns(PUBLIC_EXCLUDE_PATHS);
        if (!isProdProfile()) {
            registration.excludePathPatterns(DOC_EXCLUDE_PATHS);
        }
    }

    private boolean isProdProfile() {
        return java.util.Arrays.asList(environment.getActiveProfiles()).contains("prod");
    }
}
