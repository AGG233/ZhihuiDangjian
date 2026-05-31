package com.rauio.smartdangjian.config;

import java.time.Clock;

import org.apache.tika.Tika;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
public class BeanConfig {
    @Bean
    public static Tika tika() {
        return new Tika();
    }

    @Bean
    public Clock clock() {
        return Clock.systemDefaultZone();
    }
}
