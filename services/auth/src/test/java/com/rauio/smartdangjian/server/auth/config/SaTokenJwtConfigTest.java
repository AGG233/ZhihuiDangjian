package com.rauio.smartdangjian.server.auth.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import cn.dev33.satoken.jwt.StpLogicJwtForSimple;

class SaTokenJwtConfigTest {

    private final SaTokenJwtConfig config = new SaTokenJwtConfig();

    @Test
    @DisplayName("getStpLogicJwt 注册 JWT 简单模式 StpLogic")
    void getStpLogicJwt() {
        assertThat(config.getStpLogicJwt()).isInstanceOf(StpLogicJwtForSimple.class);
    }

    @Test
    @DisplayName("dev/test profile 下占位符密钥可通过校验")
    void validateJwtSecretKeyAllowsDevProfileWithPlaceholder() {
        setJwtSecretKey("CHANGE_ME_IN_PROD");
        setActiveProfiles("dev");
        config.validateJwtSecretKey();
    }

    @Test
    @DisplayName("非开发 profile 使用占位符密钥时拒绝启动")
    void validateJwtSecretKeyRejectsPlaceholderInProd() {
        setJwtSecretKey("CHANGE_ME_IN_PROD");
        setActiveProfiles("prod");
        assertThatThrownBy(config::validateJwtSecretKey).isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("非开发 profile 使用空密钥时拒绝启动")
    void validateJwtSecretKeyRejectsBlankKeyInProd() {
        setJwtSecretKey(" ");
        setActiveProfiles("prod");
        assertThatThrownBy(config::validateJwtSecretKey).isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("非开发 profile 显式配置真实密钥时可通过校验")
    void validateJwtSecretKeyAllowsRealKeyInProd() {
        setJwtSecretKey("prod-real-secret-key");
        setActiveProfiles("prod");
        config.validateJwtSecretKey();
    }

    private void setJwtSecretKey(String value) {
        ReflectionTestUtils.setField(config, "jwtSecretKey", value);
    }

    private void setActiveProfiles(String value) {
        ReflectionTestUtils.setField(config, "activeProfiles", value);
    }
}
