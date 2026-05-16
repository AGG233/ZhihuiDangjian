package com.rauio.smartdangjian.server.auth.constants;

/**
 * 认证模块错误码常量（范围 1000-1999）
 */
public class AuthErrorConstants {

    // Token 相关错误
    public static final int TOKEN_INVALID_SUBJECT = 1001;
    public static final int TOKEN_USER_NOT_FOUND = 1002;
    public static final int TOKEN_EXPIRED = 1003;
    public static final int TOKEN_VERIFICATION_FAILED = 1004;
    public static final int TOKEN_SERVER_ERROR = 1005;
    public static final int TOKEN_DECODE_ERROR = 1006;

    // 认证业务错误
    public static final int CAPTCHA_ERROR = 1010;
    public static final int PASSWORD_ERROR = 1011;
    public static final int UNAUTHORIZED = 1012;
    public static final int USER_NOT_FOUND = 1013;
    public static final int OLD_PASSWORD_ERROR = 1014;
}
