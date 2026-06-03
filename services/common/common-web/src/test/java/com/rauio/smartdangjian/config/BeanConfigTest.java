package com.rauio.smartdangjian.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Clock;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class BeanConfigTest {

    private final BeanConfig config = new BeanConfig();

    @Test
    @DisplayName("clock 返回系统默认时钟")
    void clock() {
        Clock clock = config.clock();

        assertThat(clock).isNotNull();
        assertThat(clock.getZone()).isEqualTo(Clock.systemDefaultZone().getZone());
    }
}
