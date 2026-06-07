package com.rauio.smartdangjian.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * 验证 docker-compose 文件中 Redis requirepass 配置。
 */
class DockerComposeStructureTest {

    private static final Path PROD_COMPOSE = projectRoot().resolve("docker-compose.yml");
    private static final Path DEV_COMPOSE = projectRoot().resolve("docker-compose.dev.yml");

    private static Path projectRoot() {
        Path dir = Path.of("").toAbsolutePath().normalize();
        while (dir != null && !Files.exists(dir.resolve("settings.gradle"))) {
            dir = dir.getParent();
        }
        if (dir == null) {
            throw new IllegalStateException("Cannot find project root (marker: settings.gradle)");
        }
        return dir;
    }

    @Test
    @DisplayName("docker-compose.yml 中 Redis 服务配置了 requirepass")
    void prodRedisRequirepassConfigured() throws IOException {
        String content = Files.readString(PROD_COMPOSE);
        assertThat(content)
                .as("docker-compose.yml 中 Redis 应启用 requirepass")
                .contains("requirepass")
                .contains("\"${REDIS_PASSWORD}\"");
    }

    @Test
    @DisplayName("docker-compose.yml 中 Redis healthcheck 使用 -a 参数传递密码")
    void prodRedisHealthcheckIncludesPassword() throws IOException {
        String content = Files.readString(PROD_COMPOSE);
        assertThat(content)
                .as("docker-compose.yml 中 Redis healthcheck 应传递密码参数")
                .contains("redis-cli")
                .contains("-a")
                .contains("${REDIS_PASSWORD}");
    }

    @Test
    @DisplayName("docker-compose.yml 中 App 服务传递 REDIS_PASSWORD 环境变量")
    void prodAppHasRedisPasswordEnv() throws IOException {
        String content = Files.readString(PROD_COMPOSE);
        assertThat(content)
                .as("docker-compose.yml 中 App 服务应传递 REDIS_PASSWORD 环境变量")
                .contains("REDIS_PASSWORD");
    }

    @Test
    @DisplayName("docker-compose.yml 中 App 服务将日志路径指向容器挂载目录")
    void prodAppLogPathUsesMountedDirectory() throws IOException {
        String content = Files.readString(PROD_COMPOSE);
        assertThat(content)
                .as("docker-compose.yml 中 App 服务日志应写入 /app/logs")
                .contains("LOG_PATH: /app/logs")
                .contains("./logs:/app/logs");
    }

    @Test
    @DisplayName("docker-compose.dev.yml 中 Redis 服务配置了 requirepass（含开发默认值）")
    void devRedisRequirepassConfigured() throws IOException {
        String content = Files.readString(DEV_COMPOSE);
        assertThat(content)
                .as("docker-compose.dev.yml 中 Redis 应启用 requirepass")
                .contains("requirepass")
                .contains("${REDIS_PASSWORD:-redis-dev}");
    }

    @Test
    @DisplayName("docker-compose.dev.yml 中 Redis healthcheck 使用 -a 参数传递密码（含开发默认值）")
    void devRedisHealthcheckIncludesPassword() throws IOException {
        String content = Files.readString(DEV_COMPOSE);
        assertThat(content)
                .as("docker-compose.dev.yml 中 Redis healthcheck 应传递密码参数")
                .contains("redis-cli")
                .contains("-a")
                .contains("${REDIS_PASSWORD:-redis-dev}");
    }

    @Test
    @DisplayName("docker-compose.dev.yml 中 App 服务传递 REDIS_PASSWORD 环境变量（含开发默认值）")
    void devAppHasRedisPasswordEnv() throws IOException {
        String content = Files.readString(DEV_COMPOSE);
        assertThat(content)
                .as("docker-compose.dev.yml 中 App 服务应传递 REDIS_PASSWORD 环境变量")
                .contains("REDIS_PASSWORD")
                .contains("${REDIS_PASSWORD:-redis-dev}");
    }

    @Test
    @DisplayName("docker-compose.dev.yml 中 App 服务将日志路径指向容器挂载目录")
    void devAppLogPathUsesMountedDirectory() throws IOException {
        String content = Files.readString(DEV_COMPOSE);
        assertThat(content)
                .as("docker-compose.dev.yml 中 App 服务日志应写入 /app/logs")
                .contains("LOG_PATH: /app/logs")
                .contains("./logs:/app/logs");
    }
}
