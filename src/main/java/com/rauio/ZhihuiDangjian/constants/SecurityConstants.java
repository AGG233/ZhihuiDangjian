package com.rauio.ZhihuiDangjian.constants;


public class SecurityConstants {
    // JWT相关常量
    public static final String ACCESS_TOKEN_PREFIX  = "jwt:access:";
    public static final String REFRESH_TOKEN_PREFIX = "jwt:refresh:";
    public static final String DEFAULT_SECRET_KEY   = "ZHDJ";
    
    // RSA相关常量
    public static final String RSA_KEY_ALGORITHM = "RSA";
    public static final int RSA_KEY_SIZE = 2048;

    // JWT过期时间相关常量(毫秒)
    public static final long ACCESS_TOKEN_EXPIRATION = 1000 * 60 * 60; // 一个小时
    public static final long REFRESH_TOKEN_EXPIRATION = 1000 * 60 * 60 * 24 * 7; // 7天
    public static final long CAPTCHA_EXPIRATION = 1000 * 60;
}