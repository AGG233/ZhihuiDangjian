package com.rauio.ZhihuiDangjiang.constants;

import java.util.regex.Pattern;

/**
 * 数据验证相关常量定义
 */
public class ValidationConstants {
    /**
     * 邮箱正则表达式
     */
    public static final Pattern EMAIL_PATTERN = 
            Pattern.compile("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");
    
    /**
     * 手机号正则表达式
     */
    public static final Pattern PHONE_PATTERN = 
            Pattern.compile("^1[3-9]\\d{9}$");
    
    /**
     * 密码正则表达式
     * 至少8位，包含字母、数字和特殊字符
     */
    public static final Pattern PASSWORD_PATTERN = 
            Pattern.compile("^(?=.*[a-zA-Z0-9])(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?]).{8,}$");
}