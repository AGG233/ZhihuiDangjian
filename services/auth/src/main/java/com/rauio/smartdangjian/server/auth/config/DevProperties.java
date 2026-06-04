package com.rauio.smartdangjian.server.auth.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import lombok.Data;

/**
 * 开发环境自动登录配置属性。
 *
 * <p>仅在 dev profile 下加载，绑定 {@code app.dev.*} 前缀的配置项。</p>
 */
@Data
@Component
@Validated
@Profile("dev")
@ConfigurationProperties(prefix = "app.dev")
public class DevProperties {

    /**
     * 开发环境自动登录使用的默认用户 ID。
     * 当用户在开发环境中未登录时，自动以此用户 ID 登录。
     */
    private String defaultUserId;

}
