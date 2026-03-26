package com.rauio.smartdangjian.aop;

import com.rauio.smartdangjian.aop.annotation.PermissionAccess;
import com.rauio.smartdangjian.constants.ErrorConstants;
import com.rauio.smartdangjian.exception.BusinessException;
import com.rauio.smartdangjian.utils.SecurityUtils;
import com.rauio.smartdangjian.utils.spec.UserType;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.AnnotationUtils;

import java.lang.reflect.Method;

@Aspect
public class UserAspect {

    @Before("@annotation(com.rauio.smartdangjian.aop.annotation.PermissionAccess) || " +
            "@within(com.rauio.smartdangjian.aop.annotation.PermissionAccess)")
    public void checkPermissionAccess(JoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        Class<?> targetClass = joinPoint.getTarget().getClass();

        PermissionAccess annotation = method.getAnnotation(PermissionAccess.class);
        if (annotation == null) {
            annotation = AnnotationUtils.findAnnotation(targetClass, PermissionAccess.class);
        }

        if (annotation == null) {
            throw new RuntimeException("权限注解不存在");
        }

        UserType currentUserType = SecurityUtils.getCurrentUserType();
        if (currentUserType == null) {
            throw new BusinessException(ErrorConstants.RESOURCE_NOT_AUTHORIZED, "用户未登录或身份无效");
        }

        if (getPermissionLevel(currentUserType) < getPermissionLevel(annotation.value())) {
            throw new BusinessException(ErrorConstants.RESOURCE_NOT_AUTHORIZED, "用户无权限");
        }
    }

    private int getPermissionLevel(UserType userType) {
        return switch (userType) {
            case STUDENT -> 1;
            case SCHOOL -> 2;
            case MANAGER -> 3;
        };
    }
}
