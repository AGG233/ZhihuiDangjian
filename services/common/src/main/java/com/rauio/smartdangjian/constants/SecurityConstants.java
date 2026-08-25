package com.rauio.smartdangjian.constants;

/**
 * 安全相关常量。
 *
 * <p>历史遗留的自研 JWT/RSA 常量已随认证切换到 Sa-Token 无状态 JWT 双令牌体系
 * （见 auth 模块 {@code JwtClaims}/{@code RefreshTokenService}）而移除；
 * 本类仅保留仍在使用的验证码过期时间。
 */
public class SecurityConstants {

    // JWT过期时间相关常量(毫秒)
    public static final long CAPTCHA_EXPIRATION = 1000 * 60;
}
