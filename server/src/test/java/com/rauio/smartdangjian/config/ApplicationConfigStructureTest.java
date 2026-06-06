package com.rauio.smartdangjian.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.validation.annotation.Validated;

/**
 * 验证 application YAML 配置结构，防止弱默认密码、隐式时间单位等配置隐患。
 */
class ApplicationConfigStructureTest {

    private static final Path PROD_YAML = projectRoot().resolve("server/src/main/resources/application-prod.yaml");
    private static final Path BASE_YAML = projectRoot().resolve("server/src/main/resources/application.yaml");

    private static Path projectRoot() {
        // 从当前 working dir 向上查找包含 settings.gradle 的目录
        Path dir = Path.of("").toAbsolutePath().normalize();
        while (dir != null && !Files.exists(dir.resolve("settings.gradle"))) {
            dir = dir.getParent();
        }
        if (dir == null) {
            throw new IllegalStateException("Cannot find project root (marker: settings.gradle)");
        }
        return dir;
    }

    /** 提取 YAML 中 spring.data.redis 章节内的行。 */
    private static List<String> redisSectionLines(String yamlContent) {
        List<String> lines = yamlContent.lines().toList();
        int redisIdx = -1;
        for (int i = 0; i < lines.size(); i++) {
            if (lines.get(i).strip().equals("redis:")) {
                redisIdx = i;
                break;
            }
        }
        if (redisIdx < 0) {
            return List.of();
        }
        // redis 章节的缩进深度
        String redisIndent =
                lines.get(redisIdx).substring(0, lines.get(redisIdx).indexOf("redis:"));
        // 章节结束于下一个相同或更浅缩进的非空行
        int endIdx = lines.size();
        for (int i = redisIdx + 1; i < lines.size(); i++) {
            String line = lines.get(i);
            if (line.isBlank() || line.strip().startsWith("#")) {
                continue;
            }
            String indent = line.substring(0, line.indexOf(line.strip()));
            if (indent.length() <= redisIndent.length()) {
                endIdx = i;
                break;
            }
        }
        return lines.subList(redisIdx + 1, endIdx);
    }

    @Test
    @DisplayName("application-prod.yaml 中 spring.data.redis.password 必须从环境变量读取，无默认值（无冒号默认语法）")
    void prodRedisPasswordMustBeRequiredEnvVar() throws IOException {
        String content = Files.readString(PROD_YAML);
        // 查找 password: 行的值部分，验证为 ${REDIS_PASSWORD}（无冒号默认值语法）
        boolean hasRequiredPassword = content.lines()
                .filter(line -> line.strip().startsWith("password:"))
                .anyMatch(line -> {
                    String value = line.substring(line.indexOf(':') + 1).strip();
                    return value.equals("${REDIS_PASSWORD}");
                });
        assertThat(hasRequiredPassword)
                .as("application-prod.yaml 必须使用 ${REDIS_PASSWORD}（无默认值），禁止硬编码密码或 ${REDIS_PASSWORD:xxx} 默认值")
                .isTrue();
    }

    @Test
    @DisplayName("application-prod.yaml 不包含弱默认密码（password、123456）")
    void prodRedisPasswordMustNotContainWeakDefaults() throws IOException {
        String content = Files.readString(PROD_YAML);
        assertThat(content)
                .as("application-prod.yaml 禁止包含弱默认密码")
                .doesNotContain("123456", "password: password", "password: redis");
    }

    @Test
    @DisplayName("application.yaml Redis timeout 使用显式时间单位（s/ms/m），不使用裸数字")
    void redisTimeoutUsesExplicitTimeUnit() throws IOException {
        String content = Files.readString(BASE_YAML);
        List<String> redisLines = redisSectionLines(content);

        // 找出 Redis 章节内与超时相关的行
        List<String> timeoutLines = redisLines.stream()
                .filter(line ->
                        line.contains("timeout:") || line.contains("connect-timeout:") || line.contains("max-wait:"))
                .toList();

        assertThat(timeoutLines)
                .as("Redis 章节应包含 timeout/connect-timeout/max-wait 配置")
                .isNotEmpty();

        for (String line : timeoutLines) {
            String trimmed = line.strip();
            if (trimmed.startsWith("#")) {
                continue;
            }
            String value = trimmed.substring(trimmed.indexOf(':') + 1).strip();
            boolean isBareNumber = value.matches("\\d+");
            assertThat(isBareNumber)
                    .as("Redis timeout 值 '%s' 必须使用显式时间单位（如 5s、2000ms），不能使用裸数字", value)
                    .isFalse();
        }
    }

    @Test
    @DisplayName("application.yaml 包含 Redis 连接池配置（max-active、max-idle、min-idle）")
    void baseYamlContainsRedisPoolConfig() throws IOException {
        String content = Files.readString(BASE_YAML);
        List<String> redisLines = redisSectionLines(content);
        String redisContent = String.join("\n", redisLines);
        assertThat(redisContent)
                .as("Redis 章节应包含 Lettuce 连接池配置")
                .contains("max-active:")
                .contains("max-idle:")
                .contains("min-idle:")
                .contains("max-wait:");
    }

    @Test
    @DisplayName("application.yaml 包含 Redis 超时配置（timeout、connect-timeout）")
    void baseYamlContainsRedisTimeoutConfig() throws IOException {
        String content = Files.readString(BASE_YAML);
        List<String> redisLines = redisSectionLines(content);
        String redisContent = String.join("\n", redisLines);
        assertThat(redisContent).as("Redis 章节应包含超时配置").contains("timeout:").contains("connect-timeout:");
    }

    @Test
    @DisplayName("application-dev.yaml 配置了 dev 环境的 CORS allowed-origins")
    void devYamlHasCorsAllowedOrigins() throws IOException {
        Path devYaml = projectRoot().resolve("server/src/main/resources/application-dev.yaml");
        String content = Files.readString(devYaml);
        assertThat(content)
                .as("application-dev.yaml 应包含 app.cors.allowed-origins 配置")
                .contains("allowed-origins:");
    }

    @Test
    @DisplayName("application-prod.yaml 禁止包含 CORS 通配符（运行时 validateCorsOrigins 兜底校验）")
    void prodYamlMustNotContainCorsWildcards() throws IOException {
        String content = Files.readString(PROD_YAML);
        // prod YAML 没有 app.cors 配置，由 WebConfig.validateCorsOrigins 运行时检查通配符并阻止启动
        // 此测试确保不会意外加入通配符配置绕过校验
        assertThat(content)
                .as("application-prod.yaml 禁止包含 CORS 通配符")
                .doesNotContain("allowed-origins: *")
                .doesNotContain("allowed-origins: '*'")
                .doesNotContain("allowed-origins: \"*\"");
    }

    @Test
    @DisplayName("application.yaml 中 spring.ai 没有弱默认 API key（dummy-key 仅限 dev profile）")
    void baseYamlAiApiKeyHasNoWeakDefault() throws IOException {
        String content = Files.readString(BASE_YAML);
        boolean hasDummyKey = content.lines()
                .filter(line -> line.strip().startsWith("api-key:"))
                .anyMatch(line -> line.contains("dummy-key"));
        assertThat(hasDummyKey)
                .as("application.yaml 中 spring.ai.openai.api-key 不应包含 dummy-key 默认值，dummy-key 仅限 dev profile")
                .isFalse();
    }

    @Test
    @DisplayName("所有 @ConfigurationProperties 类必须带有 @Validated 注解")
    void allConfigurationPropertiesMustBeValidated() throws Exception {
        var scanner = new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(new AnnotationTypeFilter(ConfigurationProperties.class));
        Set<BeanDefinition> beans = scanner.findCandidateComponents("com.rauio.smartdangjian");
        assertThat(beans).as("至少应有一个 @ConfigurationProperties 类").isNotEmpty();
        for (BeanDefinition bd : beans) {
            AnnotatedTypeMetadata metadata =
                    ((org.springframework.beans.factory.annotation.AnnotatedBeanDefinition) bd).getMetadata();
            assertThat(metadata.isAnnotated(Validated.class.getName()))
                    .as("@ConfigurationProperties 类 %s 必须带有 @Validated 注解", bd.getBeanClassName())
                    .isTrue();
        }
    }

    @Test
    @DisplayName("application-prod.yaml 不包含 dummy-key")
    void prodYamlMustNotContainDummyKey() throws IOException {
        String content = Files.readString(PROD_YAML);
        assertThat(content).as("application-prod.yaml 禁止包含 dummy-key 弱默认值").doesNotContain("dummy-key");
    }
}
