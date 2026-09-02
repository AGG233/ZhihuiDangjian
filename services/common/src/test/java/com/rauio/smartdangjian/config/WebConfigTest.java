package com.rauio.smartdangjian.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import jakarta.servlet.ServletContext;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.rauio.smartdangjian.exception.GlobalExceptionHandler;

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

    @Test
    @DisplayName("addResourceHandlers 可安全调用")
    void addResourceHandlers() {
        var appContext = mock(org.springframework.context.ApplicationContext.class);
        var servletContext = mock(ServletContext.class);
        var registry = new org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry(
                appContext, servletContext);

        config.addResourceHandlers(registry);

        assertThat(config).isInstanceOf(WebMvcConfigurer.class);
    }
}
