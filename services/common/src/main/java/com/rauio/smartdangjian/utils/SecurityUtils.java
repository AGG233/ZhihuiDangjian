package com.rauio.smartdangjian.utils;

import com.rauio.smartdangjian.security.CurrentUserPrincipal;
import com.rauio.smartdangjian.utils.spec.UserType;

import cn.dev33.satoken.exception.SaTokenContextException;
import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.stp.StpUtil;

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
