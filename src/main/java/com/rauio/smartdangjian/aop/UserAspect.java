package com.rauio.smartdangjian.aop;

import com.rauio.smartdangjian.aop.annotation.PermissionAccess;
import com.rauio.smartdangjian.constants.ErrorConstants;
import com.rauio.smartdangjian.exception.BusinessException;
import com.rauio.smartdangjian.user.service.UserService;
import com.rauio.smartdangjian.utils.spec.UserType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class UserAspect {

    private final UserService userService;

    @Before("@annotation(com.rauio.smartdangjian.aop.annotation.PermissionAccess) ||" +
    "@within(com.rauio.smartdangjian.aop.annotation.PermissionAccess)")
    public void checkPermissionAccess(JoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        Class<?> targetClass = joinPoint.getTarget().getClass();

        PermissionAccess annotation = method.getAnnotation(PermissionAccess.class);
        if (annotation == null){
            annotation = AnnotationUtils.findAnnotation(targetClass, PermissionAccess.class);
        }

        if (annotation == null){
            throw new RuntimeException("权限注解不存在");
        }

        UserType permissionType = annotation.value();
        UserType userType = userService.getCurrentUser().getUserType();

        if (getPermissionLevel(userType) < getPermissionLevel(permissionType)) {
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
