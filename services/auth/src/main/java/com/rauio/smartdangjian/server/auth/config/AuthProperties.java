package com.rauio.smartdangjian.server.auth.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;

@Data
@ConfigurationProperties(prefix = "auth")
public class AuthProperties {

    private Captcha captcha = new Captcha();

    @Data
    public static class Captcha {
        private String testCode;
    }
}
