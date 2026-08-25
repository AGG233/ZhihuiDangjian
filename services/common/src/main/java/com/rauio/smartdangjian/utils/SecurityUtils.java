package com.rauio.smartdangjian.utils;

import com.rauio.smartdangjian.security.CurrentUserPrincipal;
import com.rauio.smartdangjian.security.JwtUserPrincipal;
import com.rauio.smartdangjian.utils.spec.UserType;

import cn.dev33.satoken.exception.SaTokenContextException;
import cn.dev33.satoken.stp.StpUtil;

/**
 * 当前登录用户工具（无状态 JWT 版）。
 *
 * <p>身份要素从访问令牌 claims 读取：loginId 即用户 ID，{@code role} claim
 * 承载用户类型，{@code uni} claim 承载所属高校 ID；服务端不再保存会话对象。
 */
public class SecurityUtils {

    public static CurrentUserPrincipal getCurrentUser() {
        try {
            if (!StpUtil.isLogin()) {
                return null;
            }
            Object id = StpUtil.getLoginIdDefaultNull();
            if (id == null) {
                return null;
            }
            UserType userType = userTypeFromClaim(StpUtil.getExtra("role"));
            if (userType == null) {
                return null;
            }
            Object universityId = StpUtil.getExtra("uni");
            return new JwtUserPrincipal(
                    Long.valueOf(String.valueOf(id)),
                    userType,
                    universityId != null ? String.valueOf(universityId) : null);
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

    private static UserType userTypeFromClaim(Object role) {
        if (role == null) {
            return null;
        }
        try {
            return UserType.valueOf(String.valueOf(role));
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
