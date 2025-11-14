package com.rauio.ZhihuiDangjian.aop;


import com.rauio.ZhihuiDangjian.aop.annotation.ResourceAccess;
import com.rauio.ZhihuiDangjian.service.JwtService;
import com.rauio.ZhihuiDangjian.utils.SecurityUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.expression.MethodBasedEvaluationContext;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.util.Objects;


@Aspect
@Component
@RequiredArgsConstructor
public class ResourceAccessAspect {


    private static final Logger logger = LoggerFactory.getLogger(ResourceAccessAspect.class);
    private final JwtService jwtService;
    private final SpelExpressionParser parser = new SpelExpressionParser();
    private final DefaultParameterNameDiscoverer paramDiscoverer = new DefaultParameterNameDiscoverer();

    @Around("@annotation(com.rauio.ZhihuiDangjian.aop.annotation.ResourceAccess)")
    public Object checkResourceAccess(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        ResourceAccess resourceAccess = method.getAnnotation(ResourceAccess.class);

        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            throw new RuntimeException("无法获取上下文");
        }

        HttpServletRequest request = attributes.getRequest();
        String jwt = request.getHeader("Authorization").substring(7);
        String currentUser = SecurityUtils.getCurrentUserId();
        String targetUser = Objects.requireNonNull(getTargetUserId(joinPoint, resourceAccess.id())).toString();
        String accessUserRole = jwtService.getDecodedJWT(jwt).getClaim("role").asString();


        // todo 对其他资源进行鉴权，增加教师权限
        //放行
        if (accessUserRole.equals("管理员")) {
            logger.warn("管理员 {} 访问资源", jwtService.getIdFromToken(jwt));
            return joinPoint.proceed();
        }
        if (targetUser.equals(currentUser)) {
            return joinPoint.proceed();
        }
        throw new RuntimeException("权限不足");
    }

    private Long getTargetUserId(ProceedingJoinPoint joinPoint, String spelExpression) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        Object[] args = joinPoint.getArgs();

        MethodBasedEvaluationContext context = new MethodBasedEvaluationContext(null, method, args, paramDiscoverer);

        try {
            Expression expression = parser.parseExpression(spelExpression);
            Object value = expression.getValue(context, Object.class);
            if (value instanceof Long) {
                return (Long) value;
            } else if (value instanceof Number) {
                return ((Number) value).longValue();
            }
            return null;
        } catch (Exception e) {
            logger.error("解析SpEL表达式 '{}' 失败", spelExpression, e);
            return null;
        }
    }
}
