package com.rauio.smartdangjian.utils;

import com.rauio.smartdangjian.security.CurrentUserPrincipal;
import com.rauio.smartdangjian.utils.spec.UserType;

import cn.dev33.satoken.exception.SaTokenContextException;
import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.stp.StpUtil;

/**
 * @deprecated 请使用 {@link com.rauio.smartdangjian.security.CurrentUserProvider} 替代。
 * <p>
 * SecurityUtils 直接依赖 Sa-Token 静态 API，测试困难且语义不明确。
 * CurrentUserProvider 是面向接口的替代方案，支持 mock 和统一的未登录异常处理。
 * <p>
 * 迁移指南：
 * <ul>
 *   <li>注入 {@code CurrentUserProvider} 替代调用 {@code SecurityUtils.getCurrentUserId()}</li>
 *   <li>注入 {@code CurrentUserProvider} 替代调用 {@code SecurityUtils.getCurrentUser()}</li>
 *   <li>注入 {@code CurrentUserProvider} 替代调用 {@code SecurityUtils.getCurrentUserType()}</li>
 * </ul>
 * </p>
 */
@Deprecated(since = "602", forRemoval = true)
public class SecurityUtils {
    public static CurrentUserPrincipal getCurrentUser() {
        try {
            if (!StpUtil.isLogin()) {
                return null;
            }
            SaSession session = StpUtil.getSession();
            if (session == null) {
                return null;
            }
            Object user = session.get("user");
            return user instanceof CurrentUserPrincipal ? (CurrentUserPrincipal) user : null;
        } catch (SaTokenContextException e) {
            return null;
        }
    }

    public static String getCurrentUserId() {
        try {
            if (!StpUtil.isLogin()) {
                return null;
            }
            return StpUtil.getLoginIdAsString();
        } catch (SaTokenContextException e) {
            return null;
        }
    }

    public static UserType getCurrentUserType() {
        CurrentUserPrincipal currentUser = getCurrentUser();
        return currentUser == null ? null : currentUser.getUserType();
    }
}
