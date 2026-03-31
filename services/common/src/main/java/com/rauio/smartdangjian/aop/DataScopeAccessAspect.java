package com.rauio.smartdangjian.aop;

import com.rauio.smartdangjian.aop.annotation.DataScopeAccess;
import com.rauio.smartdangjian.aop.support.DataScopeContext;
import com.rauio.smartdangjian.aop.support.DataScopeResolver;
import com.rauio.smartdangjian.constants.ErrorConstants;
import com.rauio.smartdangjian.exception.BusinessException;
import com.rauio.smartdangjian.security.CurrentUserPrincipal;
import com.rauio.smartdangjian.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.util.List;

@Aspect
@Component
@RequiredArgsConstructor
public class DataScopeAccessAspect {

    private final List<DataScopeResolver> resolvers;

    @Around("@annotation(access)")
    public Object checkAccess(ProceedingJoinPoint joinPoint, DataScopeAccess access) throws Throwable {
        CurrentUserPrincipal currentUser = requireCurrentUser();
        DataScopeResolver resolver = findResolver(access.resource());
        DataScopeContext context = new DataScopeContext(joinPoint, access, currentUser);
        resolver.before(context);
        Object result = joinPoint.proceed();
        return resolver.after(context, result);
    }

    private CurrentUserPrincipal requireCurrentUser() {
        Object principal = SecurityUtils.getCurrentUser();
        if (!(principal instanceof CurrentUserPrincipal user)) {
            throw new BusinessException(ErrorConstants.USER_NOT_EXISTS, "用户不存在");
        }
        return user;
    }

    private DataScopeResolver findResolver(String resource) {
        return resolvers.stream()
                .filter(resolver -> resolver.supports(resource))
                .findFirst()
                .orElseThrow(() -> new BusinessException(ErrorConstants.RESOURCE_NOT_AUTHORIZED, "未找到数据权限解析器"));
    }
}
