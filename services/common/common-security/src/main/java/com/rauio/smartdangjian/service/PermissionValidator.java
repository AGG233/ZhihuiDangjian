package com.rauio.smartdangjian.service;

import org.springframework.stereotype.Component;

import com.rauio.smartdangjian.constants.ErrorConstants;
import com.rauio.smartdangjian.exception.BusinessException;
import com.rauio.smartdangjian.security.CurrentUserProvider;
import com.rauio.smartdangjian.security.LoginUser;
import com.rauio.smartdangjian.utils.spec.UserType;

import cn.dev33.satoken.exception.NotLoginException;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PermissionValidator {

    private final CurrentUserProvider currentUserProvider;

    public void requireResourceAccess(Object resourceOwnerId) {
        requireResourceAccess(resourceOwnerId, "无权访问该资源");
    }

    public void requireResourceAccess(Object resourceOwnerId, String message) {
        if (isManager()) {
            return;
        }
        LoginUser user = getCurrentUser();
        if (user == null) {
            throw new BusinessException(ErrorConstants.RESOURCE_NOT_AUTHORIZED, message);
        }
        String currentUserId = user.getId();
        String ownerId = resourceOwnerId == null ? null : String.valueOf(resourceOwnerId);
        if (ownerId == null || !ownerId.equals(currentUserId)) {
            throw new BusinessException(ErrorConstants.RESOURCE_NOT_AUTHORIZED, message);
        }
    }

    public boolean isResourceOwner(Object resourceOwnerId) {
        if (isManager()) {
            return true;
        }
        LoginUser user = getCurrentUser();
        if (user == null) {
            return false;
        }
        String currentUserId = user.getId();
        String ownerId = resourceOwnerId == null ? null : String.valueOf(resourceOwnerId);
        return ownerId != null && ownerId.equals(currentUserId);
    }

    private boolean isManager() {
        LoginUser user = getCurrentUser();
        return user != null && user.getUserType() == UserType.MANAGER;
    }

    private LoginUser getCurrentUser() {
        try {
            return currentUserProvider.getCurrentUser();
        } catch (NotLoginException e) {
            return null;
        }
    }
}
