package com.rauio.smartdangjian.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import com.rauio.smartdangjian.service.SensitiveWordService;

@AutoConfiguration
@EnableConfigurationProperties(SensitiveWordProperties.class)
public class SensitiveWordConfig {

    @Bean
    @ConditionalOnProperty(prefix = "app.sensitive-word", name = "enabled", havingValue = "true", matchIfMissing = true)
    public SensitiveWordService sensitiveWordService(SensitiveWordProperties properties) {
        return new SensitiveWordService(properties);
    }
}
