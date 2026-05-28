package com.rauio.smartdangjian.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import com.rauio.smartdangjian.exception.GlobalExceptionHandler;

@SpringBootTest(classes = WebConfig.class)
@TestPropertySource(properties = {"app.cors.allowed-origins=http://localhost:*,http://127.0.0.1:*,"})
@DisplayName("WebConfig 配置测试")
class WebConfigTest {

    @Autowired
    private WebConfig webConfig;

    @Test
    @DisplayName("GlobalExceptionHandler Bean 正确创建")
    void globalExceptionHandlerBean() {
        GlobalExceptionHandler handler = webConfig.globalExceptionHandler();
        assertThat(handler).isNotNull();
    }
}
