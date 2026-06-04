package com.rauio.smartdangjian.server.ai.config;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import lombok.Data;

@Data
@Validated
@ConfigurationProperties(prefix = "ai")
public class AiProperties {

    private RateLimit rateLimit = new RateLimit();

    @Data
    public static class RateLimit {
        private boolean enabled = true;
        private int requestsPerMinute = 10;
        private List<String> pathPatterns = List.of(
                "/api/ai/chat/**",
                "/api/auth/login",
                "/api/auth/register",
                "/api/auth/captcha/**",
                "/api/auth/changePassword",
                "/api/resource/files/upload",
                "/api/resource/files/upload/callback/**",
                "/api/resource/files/confirm/**",
                "/api/user/users/search",
                "/api/admin/users/search",
                "/api/search/recommend",
                "/api/search/profile");
    }
}
