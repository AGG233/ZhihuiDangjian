package com.rauio.smartdangjian.config;

import org.apache.tika.Tika;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
public class BeanConfig {
    @Bean
    public static Tika tika() {
        return new Tika();
    }
}
