package com.rauio.smartdangjian.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Sa-Token 令牌请求头配置回归。
 *
 * <p>前端、Swagger 描述、前端迁移指南、SpringDoc securityScheme 与 CI 烟雾测试统一按
 * {@code Authorization: Bearer <accessToken>} 发送令牌；一旦 {@code sa-token.token-name}
 * 与之不一致，Sa-Token 读不到令牌，登录后的所有接口都会返回 401。
 */
@DisplayName("Sa-Token 令牌请求头配置回归")
class AuthTokenHeaderConfigTest {

    private static Path projectRoot() {
        Path dir = Path.of("").toAbsolutePath();
        while (dir != null && !Files.exists(dir.resolve("settings.gradle"))) {
            dir = dir.getParent();
        }
        if (dir == null) {
            throw new IllegalStateException("未找到项目根目录（缺少 settings.gradle）");
        }
        return dir;
    }

    @Test
    @DisplayName("token-name 必须为 Authorization，且不再使用历史值 Zhihui-Token")
    void tokenNameMatchesAuthorizationHeader() throws IOException {
        String yaml = Files.readString(projectRoot().resolve("server/src/main/resources/application.yaml"));

        assertThat(yaml).as("令牌请求头名必须与前端/文档/CI 约定一致，否则登录后所有接口 401").contains("token-name: Authorization");
        assertThat(yaml).as("历史头名 Zhihui-Token 会导致前端请求无法被识别").doesNotContain("Zhihui-Token");
    }
}
