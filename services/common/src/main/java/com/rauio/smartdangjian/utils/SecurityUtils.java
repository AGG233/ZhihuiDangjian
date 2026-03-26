package com.rauio.smartdangjian.utils;

import com.rauio.smartdangjian.security.CurrentUserPrincipal;
import com.rauio.smartdangjian.utils.spec.UserType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityUtils {
    public static CurrentUserPrincipal getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            return null;
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof CurrentUserPrincipal currentUserPrincipal) {
            return currentUserPrincipal;
        }
        return null;
    }

    public static String getCurrentUserId() {
        CurrentUserPrincipal currentUser = getCurrentUser();
        return currentUser == null ? null : currentUser.getId();
    }

    public static UserType getCurrentUserType() {
        CurrentUserPrincipal currentUser = getCurrentUser();
        return currentUser == null ? null : currentUser.getUserType();
    }
}
