package com.rauio.ZhihuiDangjian.aop;

import com.rauio.ZhihuiDangjian.aop.annotation.PermissionAccess;
import com.rauio.ZhihuiDangjian.constants.ErrorConstants;
import com.rauio.ZhihuiDangjian.exception.BusinessException;
import com.rauio.ZhihuiDangjian.pojo.User;
import com.rauio.ZhihuiDangjian.service.JwtService;
import com.rauio.ZhihuiDangjian.service.UserService;
import com.rauio.ZhihuiDangjian.utils.Spec.UserType;
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
    private final JwtService jwtService;

    @Before("@annotation(com.rauio.ZhihuiDangjian.aop.annotation.PermissionAccess) ||" +
    "@within(com.rauio.ZhihuiDangjian.aop.annotation.PermissionAccess)")
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

        UserType userType = annotation.value();
        User user = userService.getUserFromAuthentication();
        if (user == null) {
            throw new BusinessException(BusinessException.USER_NOT_EXISTS, "用户不存在");
        }

        if (user.getUserType() !=  userType) {
            throw new BusinessException(ErrorConstants.RESOURCE_NOT_AUTHORIZED, "用户无权限");
        }
    }
}
