package com.rauio.smartdangjian.aop;

import com.rauio.smartdangjian.aop.annotation.ResourceAccess;
import com.rauio.smartdangjian.utils.SecurityUtils;
import com.rauio.smartdangjian.utils.spec.UserType;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.context.expression.MethodBasedEvaluationContext;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.standard.SpelExpressionParser;

import java.lang.reflect.Method;
import java.util.Objects;
import java.util.regex.Pattern;

@Aspect
@Slf4j
public class ResourceAccessAspect {

    private static final Pattern SAFE_SPEL_PATTERN = Pattern.compile(
            "^#[_a-zA-Z][_a-zA-Z0-9]*(\\.[_a-zA-Z][_a-zA-Z0-9]*)*$|^'[^']*'$"
    );

    private final SpelExpressionParser parser = new SpelExpressionParser();
    private final DefaultParameterNameDiscoverer paramDiscoverer = new DefaultParameterNameDiscoverer();

    @Around("@annotation(com.rauio.smartdangjian.aop.annotation.ResourceAccess)")
    public Object checkResourceAccess(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        ResourceAccess resourceAccess = method.getAnnotation(ResourceAccess.class);

        String currentUser = SecurityUtils.getCurrentUserId();
        UserType currentUserType = SecurityUtils.getCurrentUserType();
        String targetUser = Objects.requireNonNull(getTargetUserId(joinPoint, resourceAccess.id())).toString();

        if (currentUserType == UserType.MANAGER) {
            log.warn("管理员 {} 访问资源", currentUser);
            return joinPoint.proceed();
        }
        if (targetUser.equals(currentUser)) {
            return joinPoint.proceed();
        }
        throw new RuntimeException("权限不足");
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
            throw new IllegalArgumentException("非法SpEL表达式");
        }
    }
}
