package com.rauio.smartdangjian.config;

import java.time.Clock;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
public class BeanConfig {
    @Bean
    public Clock clock() {
        return Clock.systemDefaultZone();
    }
}
