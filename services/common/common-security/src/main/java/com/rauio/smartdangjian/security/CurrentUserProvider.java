package com.rauio.smartdangjian.security;

public interface CurrentUserProvider {

    /**
     * 获取当前登录用户的 ID。
     *
     * @return 用户ID
     * @throws cn.dev33.satoken.exception.NotLoginException 未登录或登录上下文不可用
     */
    String getCurrentUserId();

    /**
     * 获取当前登录用户的角色。
     *
     * @return 角色字符串（MANAGER / SCHOOL / STUDENT）
     * @throws cn.dev33.satoken.exception.NotLoginException 未登录或登录上下文不可用
     */
    String getCurrentUserRole();

    /**
     * 判断当前用户是否拥有指定角色。
     * <p>
     * 底层通过 Sa-Token 的 {@code StpUtil.hasRole(role)} 实现。
     * </p>
     *
     * @param role 角色名称
     * @return 拥有该角色返回 true
     */
    boolean hasRole(String role);

    /**
     * 获取当前登录用户的完整信息。
     *
     * @return LoginUser 对象
     * @throws cn.dev33.satoken.exception.NotLoginException 未登录或登录上下文不可用
     */
    LoginUser getCurrentUser();
}
