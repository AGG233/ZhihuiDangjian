package com.rauio.smartdangjian.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.core.env.Environment;

@ExtendWith(OutputCaptureExtension.class)
@DisplayName("StartupInfoPrinter 启动信息测试")
class StartupInfoPrinterTest {

    private static final Clock FIXED_CLOCK =
            Clock.fixed(Instant.parse("2026-06-01T08:15:30Z"), ZoneId.of("Asia/Shanghai"));

    @Test
    @DisplayName("启动信息使用注入的 Clock 输出固定时间")
    void runUsesInjectedClock(CapturedOutput output) throws Exception {
        Environment env = org.mockito.Mockito.mock(Environment.class);
        when(env.getActiveProfiles()).thenReturn(new String[] {"test"});
        when(env.getProperty("server.port", "9000")).thenReturn("8080");
        when(env.getProperty("spring.application.name", "server")).thenReturn("zhdj");
        when(env.getProperty("server.servlet.context-path", "")).thenReturn("");
        when(env.getProperty("spring.datasource.url", "N/A")).thenReturn("jdbc:mysql://localhost:3306/zhdj");
        when(env.getProperty("spring.data.redis.host", "N/A")).thenReturn("127.0.0.1");
        when(env.getProperty("spring.data.redis.port", "N/A")).thenReturn("6379");
        when(env.getProperty("spring.neo4j.uri", "N/A")).thenReturn("bolt://localhost:7687");
        when(env.getProperty("auth.captcha.test-code", "")).thenReturn("");

        StartupInfoPrinter printer = new StartupInfoPrinter(env, FIXED_CLOCK);

        printer.run(new DefaultApplicationArguments());

        assertThat(output.getOut())
                .contains("启动时间: 2026-06-01 16:15:30")
                .contains("MySQL:    jdbc:mysql://localho****");
    }
}
