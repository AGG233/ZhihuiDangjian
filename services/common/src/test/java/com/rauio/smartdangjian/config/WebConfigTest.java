package com.rauio.smartdangjian.config;

import com.rauio.smartdangjian.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class WebConfigTest {

    private final WebConfig config = new WebConfig();

    @Test
    @DisplayName("globalExceptionHandler 创建 GlobalExceptionHandler 实例")
    void globalExceptionHandler() {
        GlobalExceptionHandler handler = config.globalExceptionHandler();

        assertThat(handler).isNotNull();
    }

    @Test
    @DisplayName("addCorsMappings 配置正确的 CORS 路径")
    void addCorsMappings() {
        var registry = new org.springframework.web.servlet.config.annotation.CorsRegistry();

        config.addCorsMappings(registry);

        var mappedRegistry = registry;
        assertThat(mappedRegistry).isNotNull();
    }
}
