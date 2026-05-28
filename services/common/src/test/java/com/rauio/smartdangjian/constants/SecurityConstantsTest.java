package com.rauio.smartdangjian.constants;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class SecurityConstantsTest {

    @Test
    @DisplayName("ACCESS_TOKEN_PREFIX 为 jwt:access:")
    void accessTokenPrefix() {
        assertThat(SecurityConstants.ACCESS_TOKEN_PREFIX).isEqualTo("jwt:access:");
    }

    @Test
    @DisplayName("REFRESH_TOKEN_PREFIX 为 jwt:refresh:")
    void refreshTokenPrefix() {
        assertThat(SecurityConstants.REFRESH_TOKEN_PREFIX).isEqualTo("jwt:refresh:");
    }

    @Test
    @DisplayName("DEFAULT_SECRET_KEY 为 ZHDJ")
    void defaultSecretKey() {
        assertThat(SecurityConstants.DEFAULT_SECRET_KEY).isEqualTo("ZHDJ");
    }

    @Test
    @DisplayName("RSA_KEY_ALGORITHM 为 RSA")
    void rsaKeyAlgorithm() {
        assertThat(SecurityConstants.RSA_KEY_ALGORITHM).isEqualTo("RSA");
    }

    @Test
    @DisplayName("RSA_KEY_SIZE 为 2048")
    void rsaKeySize() {
        assertThat(SecurityConstants.RSA_KEY_SIZE).isEqualTo(2048);
    }

    @Test
    @DisplayName("ACCESS_TOKEN_EXPIRATION 为 3600000 毫秒 (1小时)")
    void accessTokenExpiration() {
        assertThat(SecurityConstants.ACCESS_TOKEN_EXPIRATION).isEqualTo(3_600_000L);
    }

    @Test
    @DisplayName("REFRESH_TOKEN_EXPIRATION 为 604800000 毫秒 (7天)")
    void refreshTokenExpiration() {
        assertThat(SecurityConstants.REFRESH_TOKEN_EXPIRATION).isEqualTo(604_800_000L);
    }

    @Test
    @DisplayName("CAPTCHA_EXPIRATION 为 60000 毫秒 (1分钟)")
    void captchaExpiration() {
        assertThat(SecurityConstants.CAPTCHA_EXPIRATION).isEqualTo(60_000L);
    }
}
