package com.rauio.ZhihuiDangjiang.constants;

/**
 * 错误码常量定义
 */
public class ErrorConstants {
    // 用户相关错误码
    public static final int PHONE_EXISTS = 1001;
    public static final int EMAIL_EXISTS = 1002;
    public static final int USERNAME_EXISTS = 1003;
    public static final int PARTY_MEMBER_ID_EXISTS = 1004;
    public static final int USER_NOT_EXISTS = 1005;
    
    // 参数相关错误码
    public static final int ARGS_ERROR      = 2001;
    public static final int NOT_FOUND       = 2002;
    public static final int MAX_VALUE       = 2003;
    public static final int MIN_VALUE       = 2004;

    // 资源错误码
    public static final int RESOURCE_NOT_EXISTS     = 4001;
    public static final int RESOURCE_NOT_AVAILABLE  = 4002;
    public static final int RESOURCE_NOT_AUTHORIZED = 4003;
}