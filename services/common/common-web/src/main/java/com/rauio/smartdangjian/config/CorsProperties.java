package com.rauio.smartdangjian.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;

@Data
@ConfigurationProperties(prefix = "app.cors")
public class CorsProperties {

    private String allowedOrigins = "http://localhost:*,http://127.0.0.1:*";
}
