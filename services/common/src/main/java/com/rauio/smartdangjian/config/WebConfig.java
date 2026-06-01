package com.rauio.smartdangjian.config;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.rauio.smartdangjian.exception.GlobalExceptionHandler;

@AutoConfiguration
public class WebConfig implements WebMvcConfigurer {

    private final Environment environment;

    @Value("${app.cors.allowed-origins:http://localhost:*,http://127.0.0.1:*}")
    private String allowedOrigins;

    @Value("${app.storage.local-root:./uploads}")
    private String localStorageRoot;

    public WebConfig(Environment environment) {
        this.environment = environment;
    }

    @Bean
    public GlobalExceptionHandler globalExceptionHandler() {
        return new GlobalExceptionHandler();
    }

    @Bean
    public SecurityHeadersFilter securityHeadersFilter() {
        return new SecurityHeadersFilter();
    }

    /**
     * CORS configuration - security notes:
     * - allowCredentials(true) with allowedOriginPatterns IS safe (not the insecure allowCredentials+allowedOrigins("*") pattern)
     * - Current defaults (localhost:*, 127.0.0.1:*) are safe for development
     * - Production: set app.cors.allowed-origins to specific domain names
     * - If origins are ever relaxed to "*", allowCredentials must be set to false
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        String[] origins = Arrays.stream(allowedOrigins.split(","))
                .map(String::trim)
                .filter(origin -> !origin.isEmpty())
                .toArray(String[]::new);
        validateCorsOrigins(origins);
        registry.addMapping("/**")
                .allowedOriginPatterns(origins)
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowCredentials(true)
                .maxAge(3600);
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String location = "file:"
                + java.nio.file.Path.of(localStorageRoot).toAbsolutePath().normalize() + "/";
        registry.addResourceHandler("/uploads/**").addResourceLocations(location);
    }

    private void validateCorsOrigins(String[] origins) {
        if (!Arrays.asList(environment.getActiveProfiles()).contains("prod")) {
            return;
        }
        boolean hasWildcard = Arrays.stream(origins).anyMatch(origin -> origin.contains("*"));
        if (origins.length == 0 || hasWildcard) {
            throw new IllegalStateException("生产环境必须配置明确的 app.cors.allowed-origins，禁止使用通配符");
        }
    }
}
