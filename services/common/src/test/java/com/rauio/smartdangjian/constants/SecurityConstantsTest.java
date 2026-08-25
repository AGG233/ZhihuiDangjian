package com.rauio.smartdangjian.constants;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class SecurityConstantsTest {

    @Test
    @DisplayName("CAPTCHA_EXPIRATION 为 60000 毫秒 (1分钟)")
    void captchaExpiration() {
        assertThat(SecurityConstants.CAPTCHA_EXPIRATION).isEqualTo(60_000L);
    }
}
