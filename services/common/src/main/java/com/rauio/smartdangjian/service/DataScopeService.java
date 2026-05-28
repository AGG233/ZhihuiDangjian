package com.rauio.smartdangjian.service;

import org.springframework.stereotype.Component;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.rauio.smartdangjian.constants.ErrorConstants;
import com.rauio.smartdangjian.exception.BusinessException;
import com.rauio.smartdangjian.security.CurrentUserPrincipal;
import com.rauio.smartdangjian.utils.SecurityUtils;
import com.rauio.smartdangjian.utils.spec.UserType;

@Component
public class DataScopeService {

    public void requireSameUniversity(String entityUniversityId) {
        if (isManager()) {
            return;
        }
        CurrentUserPrincipal user = getCurrentUser();
        if (user == null) {
            throw new BusinessException(ErrorConstants.RESOURCE_NOT_AUTHORIZED, "用户未登录");
        }
        if (StringUtils.isBlank(entityUniversityId)) {
            return;
        }
        String userUniversityId = user.getUniversityId();
        if (StringUtils.isBlank(userUniversityId) || !userUniversityId.equals(entityUniversityId)) {
            throw new BusinessException(ErrorConstants.RESOURCE_NOT_AUTHORIZED, "无权访问本校外资源");
        }
    }

    public void requireManageable(String entityUniversityId) {
        if (isManager()) {
            return;
        }
        CurrentUserPrincipal user = getCurrentUser();
        if (user == null) {
            throw new BusinessException(ErrorConstants.RESOURCE_NOT_AUTHORIZED, "用户未登录");
        }
        if (user.getUserType() == UserType.STUDENT) {
            throw new BusinessException(ErrorConstants.RESOURCE_NOT_AUTHORIZED, "无权管理资源");
        }
        if (StringUtils.isBlank(entityUniversityId)) {
            throw new BusinessException(ErrorConstants.RESOURCE_NOT_AUTHORIZED, "公共资源仅系统管理员可维护");
        }
        String userUniversityId = user.getUniversityId();
        if (StringUtils.isBlank(userUniversityId) || !userUniversityId.equals(entityUniversityId)) {
            throw new BusinessException(ErrorConstants.RESOURCE_NOT_AUTHORIZED, "无权管理本校外资源");
        }
    }

    public boolean isSameUniversity(String entityUniversityId) {
        if (isManager()) {
            return true;
        }
        CurrentUserPrincipal user = getCurrentUser();
        if (user == null) {
            return false;
        }
        if (StringUtils.isBlank(entityUniversityId)) {
            return true;
        }
        String userUniversityId = user.getUniversityId();
        return StringUtils.isNotBlank(userUniversityId) && userUniversityId.equals(entityUniversityId);
    }

    public void requireUniversityId() {
        if (isManager()) {
            return;
        }
        CurrentUserPrincipal user = getCurrentUser();
        if (user == null) {
            throw new BusinessException(ErrorConstants.RESOURCE_NOT_AUTHORIZED, "用户未登录");
        }
        if (StringUtils.isBlank(user.getUniversityId())) {
            throw new BusinessException(ErrorConstants.RESOURCE_NOT_AUTHORIZED, "当前用户未绑定学校");
        }
    }

    private boolean isManager() {
        CurrentUserPrincipal user = getCurrentUser();
        return user != null && user.getUserType() == UserType.MANAGER;
    }

    private CurrentUserPrincipal getCurrentUser() {
        return SecurityUtils.getCurrentUser();
    }
}
