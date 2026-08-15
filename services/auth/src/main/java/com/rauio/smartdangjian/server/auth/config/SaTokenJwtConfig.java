package com.rauio.smartdangjian.server.auth.config;

import jakarta.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import cn.dev33.satoken.jwt.StpLogicJwtForSimple;
import cn.dev33.satoken.stp.StpLogic;

/**
 * JWT 认证配置：启用 sa-token-jwt 插件「简单模式」。
 *
 * <p>登录后签发 JWT 格式的 token（三段式、可跨服务验签），Session 仍存 Redis，
 * 保留在线会话管理能力。JWT 密钥通过 {@code sa-token.jwt-secret-key} 配置
 * （环境变量 {@code SA_TOKEN_JWT_SECRET_KEY} 注入，生产环境必须显式配置）。
 *
 * <p>为防止默认占位符在生产误用，启动时执行 fail-fast 校验：非 dev/test profile
 * 下若密钥为空、为 {@code CHANGE_ME_IN_PROD} 或包含该占位符，则拒绝启动。
 */
@Configuration
public class SaTokenJwtConfig {

    private static final String PLACEHOLDER = "CHANGE_ME_IN_PROD";

    @Value("${sa-token.jwt-secret-key:}")
    private String jwtSecretKey;

    @Value("${spring.profiles.active:}")
    private String activeProfiles;

    /**
     * 注册 JWT 简单模式 StpLogic，替换默认随机 token 生成器。
     *
     * @return JWT 简单模式 StpLogic
     */
    @Bean
    public StpLogic getStpLogicJwt() {
        return new StpLogicJwtForSimple();
    }

    /**
     * 非开发环境禁止使用默认占位符或空密钥启动，必须显式注入真实密钥。
     */
    @PostConstruct
    public void validateJwtSecretKey() {
        if (isDevOrTestProfile()) {
            return;
        }
        if (jwtSecretKey == null || jwtSecretKey.isBlank() || jwtSecretKey.contains(PLACEHOLDER)) {
            throw new IllegalStateException(
                    "JWT secret key must be configured via SA_TOKEN_JWT_SECRET_KEY in non-dev/test profiles. "
                            + "Do not start production with the placeholder '" + PLACEHOLDER + "' or an empty value.");
        }
    }

    private boolean isDevOrTestProfile() {
        return activeProfiles.contains("dev") || activeProfiles.contains("test");
    }
}
