package com.rauio.smartdangjian.server.auth.config;

/**
 * 公开路径常量。
 * <p>
 * 统一存放所有无需登录即可访问的 API 路径，避免 Sa-Token 拦截器白名单与
 * DevAutoLoginFilter 排除列表之间的定义漂移。新增公开端点时只需在此处添加。
 */
public final class PublicPathConstants {

    private PublicPathConstants() {}

    // ========== 基础路径（不含通配符） ==========
    public static final String LOGIN = "/api/auth/login";
    public static final String CAPTCHA = "/api/auth/captcha";
    public static final String REGISTER = "/api/auth/register";
    public static final String SCHOOL = "/api/school/all";
    public static final String ERROR = "/error";

    // ========== Sa-Token 拦截器排除路径（AntPathMatcher 模式）==========
    // captcha 使用 /** 覆盖验证码图片子路径
    public static final String[] SA_TOKEN_EXCLUDE = {LOGIN, CAPTCHA + "/**", REGISTER, SCHOOL, ERROR};

    // ========== DevAutoLoginFilter 排除路径（String.startsWith 前缀匹配）==========
    // 仅排除不需要自动登录的公开读接口
    public static final String[] DEV_LOGIN_EXCLUDE = {CAPTCHA, SCHOOL};
}
