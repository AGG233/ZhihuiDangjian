package com.rauio.smartdangjian.config;

import java.util.List;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.rauio.smartdangjian.aop.ResourceAccessAspect;
import com.rauio.smartdangjian.aop.UserAspect;
import com.rauio.smartdangjian.aop.resolver.ResourceOwnerResolver;
import com.rauio.smartdangjian.exception.GlobalExceptionHandler;

@AutoConfiguration
public class WebConfig implements WebMvcConfigurer {

    @Bean
    @ConditionalOnProperty(name = "app.security.enabled", havingValue = "true", matchIfMissing = true)
    public UserAspect userAspect() {
        return new UserAspect();
    }

    @Bean
    @ConditionalOnProperty(name = "app.security.enabled", havingValue = "true", matchIfMissing = true)
    public ResourceAccessAspect resourceAccessAspect(List<ResourceOwnerResolver> ownerResolvers) {
        return new ResourceAccessAspect(ownerResolvers);
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

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:./uploads/");
    }
}
