package com.rauio.smartdangjian.server.auth.config;

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
 */
@Configuration
public class SaTokenJwtConfig {

    /**
     * 注册 JWT 简单模式 StpLogic，替换默认随机 token 生成器。
     *
     * @return JWT 简单模式 StpLogic
     */
    @Bean
    public StpLogic getStpLogicJwt() {
        return new StpLogicJwtForSimple();
    }
}
