package com.rauio.smartdangjian.config;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import lombok.Data;

@Data
@Validated
@ConfigurationProperties(prefix = "app.sensitive-word")
public class SensitiveWordProperties {

    private boolean enabled = true;

    private int maxLength = 5000;

    private boolean enableWhitelist = true;

    private String whitelistLocation = "classpath:sensitive-word/whitelist.txt";

    private List<String> customDenyWords = List.of();
}
