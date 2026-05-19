package com.rauio.smartdangjian.config;

import org.apache.tika.Tika;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;

class BeanConfigTest {

    private final BeanConfig config = new BeanConfig();

    @Test
    @DisplayName("passwordEncoder 返回 BCryptPasswordEncoder 实例")
    void passwordEncoder() {
        PasswordEncoder encoder = config.passwordEncoder();

        assertThat(encoder).isNotNull();
        assertThat(encoder.encode("test")).isNotEqualTo("test");
    }

    @Test
    @DisplayName("tika 返回 Tika 实例")
    void tika() {
        Tika tika = BeanConfig.tika();

        assertThat(tika).isNotNull();
    }

    @Test
    @DisplayName("BCryptPasswordEncoder 的 matches 验证正确")
    void passwordEncoderMatches() {
        PasswordEncoder encoder = config.passwordEncoder();
        String encoded = encoder.encode("myPassword");

        assertThat(encoder.matches("myPassword", encoded)).isTrue();
        assertThat(encoder.matches("wrongPassword", encoded)).isFalse();
    }
}
