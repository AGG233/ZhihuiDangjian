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
    @DisplayName("docker-compose.yml 中 Redis 服务无密码模式（仅内网访问）")
    void prodRedisNoPassword() throws IOException {
        String content = Files.readString(PROD_COMPOSE);
        assertThat(content)
                .as("生产 Redis 绑定 127.0.0.1，无需密码")
                .contains("redis-server")
                .doesNotContain("requirepass");
    }

    @Test
    @DisplayName("docker-compose.yml 中 Redis healthcheck 使用无密码 ping")
    void prodRedisHealthcheckNoPassword() throws IOException {
        String content = Files.readString(PROD_COMPOSE);
        assertThat(content)
                .as("Redis healthcheck 不传密码参数")
                .contains("redis-cli")
                .contains("ping")
                .doesNotContain("redis-cli\", \"-a\"");
    }

    @Test
    @DisplayName("docker-compose.yml 中 App 服务不传递 REDIS_PASSWORD（无密码模式）")
    void prodAppNoRedisPasswordEnv() throws IOException {
        String content = Files.readString(PROD_COMPOSE);
        assertThat(content)
                .as("生产 Redis 无密码，App 不传 REDIS_PASSWORD")
                .doesNotContain("REDIS_PASSWORD");
    }

    @Test
    @DisplayName("docker-compose.yml 中 App 镜像由发布流程写入，带 GHCR 默认回退")
    void prodAppImageIsProvidedByReleasePipeline() throws IOException {
        String content = Files.readString(PROD_COMPOSE);
        assertThat(content)
                .as("生产 Compose 使用 APP_IMAGE 变量，缺失时回退到 GHCR 默认镜像")
                .contains("image: ${APP_IMAGE:-ghcr.io/agg233/zhihuidangjian:latest}");
    }

    @Test
    @DisplayName("docker-compose.yml 中 App 服务使用 named volume 挂载上传目录，无文件日志")
    void prodAppLogPathUsesWritableMountedDirectory() throws IOException {
        String content = Files.readString(PROD_COMPOSE);
        assertThat(content)
                .as("生产 Compose 使用 named volume 挂载上传目录，日志走 Docker stdout")
                .contains("app-uploads:/app/uploads")
                .doesNotContain("app-logs:")
                .doesNotContain("LOG_PATH")
                .doesNotContain("app-permissions:");
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
    @DisplayName("docker-compose.dev.yml 中 App 服务将日志路径指向已修正权限的挂载目录")
    void devAppLogPathUsesWritableMountedDirectory() throws IOException {
        String content = Files.readString(DEV_COMPOSE);
        assertThat(content)
                .as("docker-compose.dev.yml 中 App 服务日志应写入 /app/logs")
                .contains("LOG_PATH: /app/logs")
                .contains("./logs:/app/logs")
                .contains("app-permissions:")
                .contains("chown -R 1000:1000 /app/logs")
                .contains("condition: service_completed_successfully");
    }
}
