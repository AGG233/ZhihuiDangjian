package com.rauio.smartdangjian.config;

import com.rauio.smartdangjian.aop.ResourceAccessAspect;
import com.rauio.smartdangjian.aop.UserAspect;
import com.rauio.smartdangjian.exception.GlobalExceptionHandler;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@AutoConfiguration
public class WebConfig implements WebMvcConfigurer {

    @Bean
    public UserAspect userAspect() {
        return new UserAspect();
    }

    @Bean
    public ResourceAccessAspect resourceAccessAspect() {
        return new ResourceAccessAspect();
    }

    @Bean
    public GlobalExceptionHandler globalExceptionHandler() {
        return new GlobalExceptionHandler();
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowCredentials(true)
                .maxAge(3600);
    }
}
