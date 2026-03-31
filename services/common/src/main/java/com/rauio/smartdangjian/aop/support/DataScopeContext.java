package com.rauio.smartdangjian.aop.support;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.rauio.smartdangjian.aop.annotation.DataScopeAccess;
import com.rauio.smartdangjian.constants.ErrorConstants;
import com.rauio.smartdangjian.exception.BusinessException;
import com.rauio.smartdangjian.security.CurrentUserPrincipal;
import lombok.Getter;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.context.expression.MethodBasedEvaluationContext;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.standard.SpelExpressionParser;

import java.lang.reflect.Method;

@Getter
public class DataScopeContext {

    private final ProceedingJoinPoint joinPoint;
    private final DataScopeAccess access;
    private final CurrentUserPrincipal currentUser;
    private final SpelExpressionParser parser = new SpelExpressionParser();
    private final DefaultParameterNameDiscoverer paramDiscoverer = new DefaultParameterNameDiscoverer();

    public DataScopeContext(ProceedingJoinPoint joinPoint, DataScopeAccess access, CurrentUserPrincipal currentUser) {
        this.joinPoint = joinPoint;
        this.access = access;
        this.currentUser = currentUser;
    }

    public <T> T resolve(String expression, Class<T> targetType) {
        if (StringUtils.isBlank(expression)) {
            return null;
        }
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        Object[] args = joinPoint.getArgs();
        MethodBasedEvaluationContext context = new MethodBasedEvaluationContext(null, method, args, paramDiscoverer);
        try {
            Expression parsed = parser.parseExpression(expression);
            return parsed.getValue(context, targetType);
        } catch (Exception e) {
            throw new BusinessException(ErrorConstants.ARGS_ERROR, "参数解析失败");
        }
    }

    public <T> T require(String expression, Class<T> targetType, String message) {
        T value = resolve(expression, targetType);
        if (value == null) {
            throw new BusinessException(ErrorConstants.ARGS_ERROR, message);
        }
        return value;
    }
}
