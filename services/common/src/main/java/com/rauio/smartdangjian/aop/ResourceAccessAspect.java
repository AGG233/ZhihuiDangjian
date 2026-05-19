package com.rauio.smartdangjian.aop;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.context.expression.MethodBasedEvaluationContext;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.standard.SpelExpressionParser;

import com.rauio.smartdangjian.aop.annotation.ResourceAccess;
import com.rauio.smartdangjian.aop.resolver.ResourceOwnerResolver;
import com.rauio.smartdangjian.constants.ErrorConstants;
import com.rauio.smartdangjian.exception.BusinessException;
import com.rauio.smartdangjian.utils.SecurityUtils;
import com.rauio.smartdangjian.utils.spec.UserType;

import lombok.extern.slf4j.Slf4j;

@Aspect
@Slf4j
public class ResourceAccessAspect {

    private static final Pattern SAFE_SPEL_PATTERN =
            Pattern.compile("^#[_a-zA-Z][_a-zA-Z0-9]*(\\.[_a-zA-Z][_a-zA-Z0-9]*)*$|^'[^']*'$");

    private final SpelExpressionParser parser = new SpelExpressionParser();
    private final DefaultParameterNameDiscoverer paramDiscoverer = new DefaultParameterNameDiscoverer();
    private final List<ResourceOwnerResolver> ownerResolvers;

    public ResourceAccessAspect() {
        this(Collections.emptyList());
    }

    public ResourceAccessAspect(List<ResourceOwnerResolver> ownerResolvers) {
        this.ownerResolvers = ownerResolvers == null ? Collections.emptyList() : ownerResolvers;
    }

    @Around("@annotation(com.rauio.smartdangjian.aop.annotation.ResourceAccess)")
    public Object checkResourceAccess(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        ResourceAccess resourceAccess = method.getAnnotation(ResourceAccess.class);

        String currentUser = SecurityUtils.getCurrentUserId();
        UserType currentUserType = SecurityUtils.getCurrentUserType();
        String targetResourceId = getTargetUserId(joinPoint, resourceAccess.id());
        if (targetResourceId == null || targetResourceId.isBlank()) {
            throw new BusinessException(ErrorConstants.ARGS_ERROR, "资源ID不能为空");
        }

        if (currentUserType == UserType.MANAGER) {
            log.warn("管理员 {} 访问资源", currentUser);
            return joinPoint.proceed();
        }
        String targetUser = resolveOwner(resourceAccess.type(), targetResourceId);
        if (targetUser.equals(currentUser)) {
            return joinPoint.proceed();
        }
        throw new BusinessException(ErrorConstants.RESOURCE_NOT_AUTHORIZED, "权限不足");
    }

    private String resolveOwner(String resourceType, String resourceId) {
        if (resourceType == null || resourceType.isBlank() || "USER".equals(resourceType)) {
            return resourceId;
        }
        return ownerResolvers.stream()
                .filter(resolver -> resolver.supports(resourceType))
                .findFirst()
                .map(resolver -> resolver.findResourceOwner(resourceId))
                .filter(ownerId -> ownerId != null && !ownerId.isBlank())
                .orElseThrow(() -> new BusinessException(ErrorConstants.RESOURCE_NOT_AUTHORIZED, "无法解析资源归属"));
    }

    private String getTargetUserId(ProceedingJoinPoint joinPoint, String spelExpression) {
        validateExpression(spelExpression);
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        Object[] args = joinPoint.getArgs();

        MethodBasedEvaluationContext context = new MethodBasedEvaluationContext(null, method, args, paramDiscoverer);

        try {
            // nosemgrep: java.spring.security.audit.spel-injection.spel-injection
            Expression expression = parser.parseExpression(spelExpression);
            Object value = expression.getValue(context, Object.class);
            if (value instanceof String stringValue) {
                return stringValue;
            }
            if (value instanceof Number numberValue) {
                return String.valueOf(numberValue.longValue());
            }
            return null;
        } catch (Exception e) {
            log.error("解析SpEL表达式 '{}' 失败", spelExpression, e);
            return null;
        }
    }

    private void validateExpression(String expression) {
        if (!SAFE_SPEL_PATTERN.matcher(expression).matches()) {
            throw new BusinessException(ErrorConstants.ARGS_ERROR, "非法SpEL表达式");
        }
    }
}
