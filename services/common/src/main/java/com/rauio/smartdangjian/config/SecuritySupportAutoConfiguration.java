package com.rauio.smartdangjian.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rauio.smartdangjian.aop.RequireUserAspect;
import com.rauio.smartdangjian.security.LoginEntryPoint;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
public class SecuritySupportAutoConfiguration {

    @Bean
    @ConditionalOnProperty(name = "app.security.enabled", havingValue = "true", matchIfMissing = true)
    public RequireUserAspect requireUserAspect() {
        return new RequireUserAspect();
    }

    @Bean
    public LoginEntryPoint loginEntryPoint(ObjectMapper objectMapper) {
        return new LoginEntryPoint(objectMapper);
    }
}
