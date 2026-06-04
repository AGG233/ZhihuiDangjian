package com.rauio.smartdangjian.server.auth.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import lombok.Data;

@Data
@Validated
@ConfigurationProperties(prefix = "auth")
public class AuthProperties {

    private Captcha captcha = new Captcha();

    @Data
    public static class Captcha {
        private String testCode;
    }
}
