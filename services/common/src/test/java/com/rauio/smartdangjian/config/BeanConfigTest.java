package com.rauio.smartdangjian.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.apache.tika.Tika;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class BeanConfigTest {

    private final BeanConfig config = new BeanConfig();

    @Test
    @DisplayName("tika 返回 Tika 实例")
    void tika() {
        Tika tika = BeanConfig.tika();

        assertThat(tika).isNotNull();
    }
}
