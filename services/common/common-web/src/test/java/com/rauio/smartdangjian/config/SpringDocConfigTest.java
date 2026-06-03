package com.rauio.smartdangjian.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import io.swagger.v3.oas.models.OpenAPI;

class SpringDocConfigTest {

    private final SpringDocConfig config = new SpringDocConfig();

    @Test
    @DisplayName("customOpenAPI 创建 OpenAPI 实例")
    void customOpenAPI() {
        OpenAPI openAPI = config.customOpenAPI();

        assertThat(openAPI).isNotNull();
        assertThat(openAPI.getInfo()).isNotNull();
        assertThat(openAPI.getInfo().getTitle()).isEqualTo("API接口文档");
    }

    @Test
    @DisplayName("customOpenAPI 包含 JWT 安全方案")
    void customOpenAPIHasSecurityScheme() {
        OpenAPI openAPI = config.customOpenAPI();

        assertThat(openAPI.getComponents()).isNotNull();
        assertThat(openAPI.getComponents().getSecuritySchemes()).containsKey("JWT");
        assertThat(openAPI.getSecurity()).isNotEmpty();
    }
}
