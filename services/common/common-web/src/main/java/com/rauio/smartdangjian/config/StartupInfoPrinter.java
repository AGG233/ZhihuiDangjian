package com.rauio.smartdangjian.config;

import java.net.InetAddress;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class StartupInfoPrinter implements ApplicationRunner {

    private final Environment env;
    private final Clock clock;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        String[] profiles = env.getActiveProfiles();
        if (profiles.length == 0) {
            profiles = env.getDefaultProfiles();
        }
        String profile = String.join(",", profiles);

        String port = env.getProperty("server.port", "9000");
        String appName = env.getProperty("spring.application.name", "server");
        String contextPath = env.getProperty("server.servlet.context-path", "");
        String dbUrl = maskSensitive(env.getProperty("spring.datasource.url", "N/A"));
        String redisHost = env.getProperty("spring.data.redis.host", "N/A");
        String redisPort = env.getProperty("spring.data.redis.port", "N/A");
        String neo4jUri = env.getProperty("spring.neo4j.uri", "N/A");
        String captchaTestCode = env.getProperty("auth.captcha.test-code", "");
        boolean captchaBypass = captchaTestCode != null && !captchaTestCode.isBlank();

        String localIp = "127.0.0.1";
        try {
            localIp = InetAddress.getLocalHost().getHostAddress();
        } catch (Exception ignored) {
        }

        String now = LocalDateTime.now(clock).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        StringBuilder sb = new StringBuilder();
        sb.append("\n");
        sb.append("========================================\n");
        sb.append("           服务器启动完成\n");
        sb.append("========================================\n");
        sb.append("  应用名称: ").append(appName).append("\n");
        sb.append("  启动时间: ").append(now).append("\n");
        sb.append("  运行环境: ").append(profile).append("\n");
        sb.append("  服务地址: http://")
                .append(localIp)
                .append(":")
                .append(port)
                .append(contextPath)
                .append("\n");
        sb.append("\n");
        sb.append("【数据库】\n");
        sb.append("  MySQL:    ").append(dbUrl).append("\n");
        sb.append("  Redis:    ")
                .append(redisHost)
                .append(":")
                .append(redisPort)
                .append("\n");
        sb.append("  Neo4j:    ").append(neo4jUri).append("\n");
        sb.append("\n");
        sb.append("【安全配置】\n");
        sb.append("  验证码绕过: ")
                .append(captchaBypass ? "已启用 (" + captchaTestCode + ")" : "未启用")
                .append("\n");
        sb.append("\n");
        sb.append("========================================");

        log.info(sb.toString());
    }

    private String maskSensitive(String value) {
        if (value == null || value.length() < 20) {
            return value;
        }
        return value.substring(0, 20) + "****";
    }
}
