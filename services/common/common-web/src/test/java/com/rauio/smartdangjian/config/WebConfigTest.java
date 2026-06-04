package com.rauio.smartdangjian.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.env.Environment;
import org.springframework.mock.env.MockEnvironment;

import com.rauio.smartdangjian.exception.GlobalExceptionHandler;

class WebConfigTest {

    private CorsProperties corsProperties() {
        return new CorsProperties();
    }

    private StorageProperties storageProperties() {
        return new StorageProperties();
    }

    private WebConfig webConfig(Environment env) {
        return new WebConfig(env, corsProperties(), storageProperties());
    }

    @Test
    @DisplayName("WebConfig 使用 Environment、CorsProperties 和 StorageProperties 构造")
    void constructor() {
        Environment env = new MockEnvironment();
        WebConfig config = webConfig(env);
        assertThat(config).isNotNull();
    }

    @Test
    @DisplayName("globalExceptionHandler Bean 正确创建")
    void globalExceptionHandlerBean() {
        Environment env = new MockEnvironment();
        WebConfig config = webConfig(env);
        GlobalExceptionHandler handler = config.globalExceptionHandler();
        assertThat(handler).isNotNull();
    }

    @Test
    @DisplayName("securityHeadersFilter Bean 正确创建")
    void securityHeadersFilter() {
        Environment env = new MockEnvironment();
        WebConfig config = webConfig(env);
        SecurityHeadersFilter filter = config.securityHeadersFilter();
        assertThat(filter).isNotNull();
    }

    @Test
    @DisplayName("非生产环境 CORS 校验通过")
    void validateCorsOriginsNonProd() {
        MockEnvironment env = new MockEnvironment();
        env.setActiveProfiles("dev");
        WebConfig config = webConfig(env);
        assertThat(config).isNotNull();
    }
}
