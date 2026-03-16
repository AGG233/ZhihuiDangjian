package com.rauio.smartdangjian.aop;

import com.rauio.smartdangjian.aop.annotation.PermissionAccess;
import com.rauio.smartdangjian.constants.ErrorConstants;
import com.rauio.smartdangjian.exception.BusinessException;
import com.rauio.smartdangjian.pojo.User;
import com.rauio.smartdangjian.service.auth.JwtService;
import com.rauio.smartdangjian.service.user.UserService;
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
    public void checkPermissionAccess(JoinPoint joinPoint) throws Throwable {
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
        UserType userType = userService.getUserFromAuthentication().getUserType();

        if ( !(userType == UserType.MANAGER || userType ==  permissionType)) {
            throw new BusinessException(ErrorConstants.RESOURCE_NOT_AUTHORIZED, "用户无权限");
        }
    }
}
