package com.rauio.smartdangjian.service;

import org.springframework.stereotype.Component;

import com.rauio.smartdangjian.constants.ErrorConstants;
import com.rauio.smartdangjian.exception.BusinessException;
import com.rauio.smartdangjian.security.CurrentUserPrincipal;
import com.rauio.smartdangjian.utils.SecurityUtils;
import com.rauio.smartdangjian.utils.spec.UserType;

@Component
public class PermissionValidator {

    public void requireResourceAccess(Object resourceOwnerId) {
        requireResourceAccess(resourceOwnerId, "无权访问该资源");
    }

    public void requireResourceAccess(Object resourceOwnerId, String message) {
        if (isManager()) {
            return;
        }
        CurrentUserPrincipal user = getCurrentUser();
        if (user == null) {
            throw new BusinessException(ErrorConstants.RESOURCE_NOT_AUTHORIZED, message);
        }
        String currentUserId = String.valueOf(user.getId());
        String ownerId = resourceOwnerId == null ? null : String.valueOf(resourceOwnerId);
        if (ownerId == null || !ownerId.equals(currentUserId)) {
            throw new BusinessException(ErrorConstants.RESOURCE_NOT_AUTHORIZED, message);
        }
    }

    public boolean isResourceOwner(Object resourceOwnerId) {
        if (isManager()) {
            return true;
        }
        CurrentUserPrincipal user = getCurrentUser();
        if (user == null) {
            return false;
        }
        String currentUserId = String.valueOf(user.getId());
        String ownerId = resourceOwnerId == null ? null : String.valueOf(resourceOwnerId);
        return ownerId != null && ownerId.equals(currentUserId);
    }

    private boolean isManager() {
        CurrentUserPrincipal user = getCurrentUser();
        return user != null && user.getUserType() == UserType.MANAGER;
    }

    private CurrentUserPrincipal getCurrentUser() {
        return SecurityUtils.getCurrentUser();
    }
}
