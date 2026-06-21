package com.rauio.smartdangjian.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.core.io.ClassPathResource;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;

@DisplayName("运行时可观测性与运维配置测试")
class ObservabilityConfigTest {

    // ── Helper: 加载 YAML 到 Properties ──────────────────────────────

    private static Properties loadYaml(String path) throws IOException {
        var loader = new YamlPropertySourceLoader();
        var resource = new ClassPathResource(path);
        var propertySources = loader.load(path, resource);

        Properties props = new Properties();
        for (var ps : propertySources) {
            @SuppressWarnings("unchecked")
            Map<String, Object> map = (Map<String, Object>) ps.getSource();
            map.forEach((k, v) -> props.setProperty(k, String.valueOf(v)));
        }
        return props;
    }

    // ── Task A: Actuator 暴露策略 ──────────────────────────────────────

    @Test
    @DisplayName("prod profile 不暴露 env 和 loggers 端点")
    void prodActuatorExcludesSensitiveEndpoints() throws IOException {
        Properties props = loadYaml("application-prod.yaml");

        String include = props.getProperty("management.endpoints.web.exposure.include");
        assertThat(include).isNotBlank();

        String[] endpoints = include.split(",");
        assertThat(endpoints).doesNotContain("env", "loggers", "beans", "heapdump");
    }

    @Test
    @DisplayName("prod profile 配置了 health show-details=when-authorized")
    void prodHealthDetailsIsWhenAuthorized() throws IOException {
        Properties props = loadYaml("application-prod.yaml");

        String showDetails = props.getProperty("management.endpoint.health.show-details");
        assertThat(showDetails).isEqualTo("when-authorized");
    }

    // ── Task B: 日志配置 ───────────────────────────────────────────────

    @Test
    @DisplayName("logback-spring.xml 存在且仅使用 ConsoleAppender 输出到 stdout")
    void logbackFileExistsWithConsoleAppenderOnly() throws Exception {
        ClassPathResource resource = new ClassPathResource("logback-spring.xml");
        assertThat(resource.exists()).isTrue();

        DocumentBuilderFactory factory = secureDocumentBuilderFactory();
        DocumentBuilder builder = factory.newDocumentBuilder();
        try (InputStream is = resource.getInputStream()) {
            Document doc = builder.parse(is);
            NodeList appenders = doc.getElementsByTagName("appender");
            boolean hasConsole = false;
            boolean hasRollingFile = false;
            for (int i = 0; i < appenders.getLength(); i++) {
                Element appender = (Element) appenders.item(i);
                String className = appender.getAttribute("class");
                if (className.contains("ConsoleAppender")) {
                    hasConsole = true;
                }
                if (className.contains("RollingFileAppender")) {
                    hasRollingFile = true;
                }
            }
            assertThat(hasConsole).isTrue();
            assertThat(hasRollingFile).isFalse();
        }
    }

    @Test
    @DisplayName("logback-spring.xml 的 pattern 包含 traceId 和 spanId 占位")
    void logbackPatternIncludesTraceAndSpanPlaceholders() throws Exception {
        ClassPathResource resource = new ClassPathResource("logback-spring.xml");
        DocumentBuilderFactory factory = secureDocumentBuilderFactory();
        DocumentBuilder builder = factory.newDocumentBuilder();
        try (InputStream is = resource.getInputStream()) {
            Document doc = builder.parse(is);
            NodeList patterns = doc.getElementsByTagName("pattern");
            boolean hasTracePlaceholder = false;
            for (int i = 0; i < patterns.getLength(); i++) {
                String text = patterns.item(i).getTextContent();
                if (text.contains("%X{traceId") && text.contains("%X{spanId")) {
                    hasTracePlaceholder = true;
                    break;
                }
            }
            assertThat(hasTracePlaceholder).isTrue();
        }
    }

    @Test
    @DisplayName("prod profile 配置可覆盖且非根目录的日志路径")
    void prodLoggingPathIsOverridableAndNotRootDirectory() throws IOException {
        Properties props = loadYaml("application-prod.yaml");

        String loggingPath = props.getProperty("logging.file.path");

        assertThat(loggingPath).isEqualTo("${LOG_PATH:/app/logs}");
    }

    @Test
    @DisplayName("application.yaml 的日志路径默认使用可写相对目录")
    void defaultLoggingPathUsesWritableRelativeDirectory() throws IOException {
        Properties props = loadYaml("application.yaml");

        String loggingPath = props.getProperty("logging.file.path");

        assertThat(loggingPath).isEqualTo("${LOG_PATH:logs}");
    }

    @Test
    @DisplayName("logback-spring.xml 不初始化文件日志 appender")
    void fileAppenderIsNotInitialized() throws Exception {
        ClassPathResource resource = new ClassPathResource("logback-spring.xml");
        DocumentBuilderFactory factory = secureDocumentBuilderFactory();
        DocumentBuilder builder = factory.newDocumentBuilder();
        try (InputStream is = resource.getInputStream()) {
            Document doc = builder.parse(is);
            NodeList appenders = doc.getElementsByTagName("appender");
            NodeList appenderRefs = doc.getElementsByTagName("appender-ref");

            boolean fileAppenderDefined = false;
            boolean fileAppenderReferenced = false;
            for (int i = 0; i < appenders.getLength(); i++) {
                Element appender = (Element) appenders.item(i);
                if ("FILE".equals(appender.getAttribute("name"))) {
                    fileAppenderDefined = true;
                }
            }

            for (int i = 0; i < appenderRefs.getLength(); i++) {
                Element appenderRef = (Element) appenderRefs.item(i);
                if ("FILE".equals(appenderRef.getAttribute("ref"))) {
                    fileAppenderReferenced = true;
                }
            }

            assertThat(fileAppenderDefined).isFalse();
            assertThat(fileAppenderReferenced).isFalse();
        }
    }

    private static DocumentBuilderFactory secureDocumentBuilderFactory() throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
        factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
        factory.setExpandEntityReferences(false);
        return factory;
    }

    // ── Task D: 链路追踪 ───────────────────────────────────────────────

    @Test
    @DisplayName("application.yaml 的 logging.pattern.level 包含 traceId/spanId MDC 键")
    void loggingPatternLevelContainsTraceAndSpanMdcKeys() throws IOException {
        Properties props = loadYaml("application.yaml");

        String patternLevel = props.getProperty("logging.pattern.level");
        assertThat(patternLevel).contains("%X{traceId");
        assertThat(patternLevel).contains("%X{spanId");
    }

    // ── Task E: 熔断降级配置 ──────────────────────────────────────────

    @Test
    @DisplayName("CircuitBreakerConfig 可通过构建器创建自定义配置")
    void circuitBreakerConfigCanBeBuilt() {
        CircuitBreakerConfig config = CircuitBreakerConfig.custom()
                .failureRateThreshold(50)
                .slidingWindowSize(10)
                .minimumNumberOfCalls(5)
                .waitDurationInOpenState(java.time.Duration.ofSeconds(20))
                .permittedNumberOfCallsInHalfOpenState(3)
                .build();

        assertThat(config.getFailureRateThreshold()).isEqualTo(50f);
        assertThat(config.getSlidingWindowSize()).isEqualTo(10);
        assertThat(config.getMinimumNumberOfCalls()).isEqualTo(5);
        assertThat(config.getPermittedNumberOfCallsInHalfOpenState()).isEqualTo(3);
    }

    @Test
    @DisplayName("CircuitBreakerRegistry 可通过配置创建命名实例")
    void circuitBreakerRegistryCreatesNamedInstance() {
        CircuitBreakerConfig config = CircuitBreakerConfig.ofDefaults();
        var registry = CircuitBreakerRegistry.of(config);
        var cb = registry.circuitBreaker("testService");

        assertThat(cb).isNotNull();
        assertThat(cb.getName()).isEqualTo("testService");
        assertThat(cb.getCircuitBreakerConfig().getFailureRateThreshold()).isEqualTo(50f);
    }

    @Test
    @DisplayName("熔断器连续失败达到阈值后打开 -> 半开 -> 恢复闭环")
    void circuitBreakerOpensOnFailureAndRecovers() {
        CircuitBreakerConfig config = CircuitBreakerConfig.custom()
                .failureRateThreshold(50)
                .slidingWindowSize(10)
                .minimumNumberOfCalls(5)
                .waitDurationInOpenState(java.time.Duration.ofMillis(500))
                .permittedNumberOfCallsInHalfOpenState(3)
                .build();

        var registry = CircuitBreakerRegistry.of(config);
        CircuitBreaker cb = registry.circuitBreaker("testRecovery");

        // 连续失败 6 次（触发熔断阈值 50%，10 次窗口内 5 次调用失败 >= 50%）
        for (int i = 0; i < 6; i++) {
            cb.onError(0, TimeUnit.MILLISECONDS, new RuntimeException("simulated failure"));
        }

        assertThat(cb.getState()).isIn(CircuitBreaker.State.OPEN, CircuitBreaker.State.HALF_OPEN);

        // 进入半开状态
        cb.transitionToHalfOpenState();

        // 半开状态下成功调用 3 次 → 闭环
        for (int i = 0; i < 3; i++) {
            cb.onSuccess(0, TimeUnit.MILLISECONDS);
        }
        assertThat(cb.getState()).isEqualTo(CircuitBreaker.State.CLOSED);
    }
}
