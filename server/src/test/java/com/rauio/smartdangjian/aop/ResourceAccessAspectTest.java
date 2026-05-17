package com.rauio.smartdangjian.aop;

import com.rauio.smartdangjian.aop.annotation.ResourceAccess;
import com.rauio.smartdangjian.exception.BusinessException;
import com.rauio.smartdangjian.security.CurrentUserPrincipal;
import com.rauio.smartdangjian.utils.spec.UserType;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.lang.reflect.Method;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("ResourceAccessAspect 单元测试")
class ResourceAccessAspectTest {

    private final ResourceAccessAspect aspect = new ResourceAccessAspect();

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("当前用户访问自己的资源时放行")
    void allowsOwnerAccess() throws Throwable {
        setSecurityContext("user-001", UserType.STUDENT);
        ProceedingJoinPoint joinPoint = joinPoint("byUserId", new Object[]{"user-001"});
        when(joinPoint.proceed()).thenReturn("ok");

        assertThatCode(() -> aspect.checkResourceAccess(joinPoint)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("当前用户访问他人资源时拒绝")
    void rejectsOtherUserAccess() {
        setSecurityContext("user-001", UserType.STUDENT);
        ProceedingJoinPoint joinPoint = joinPoint("byUserId", new Object[]{"user-002"});

        assertThatThrownBy(() -> aspect.checkResourceAccess(joinPoint))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("权限不足");
    }

    @Test
    @DisplayName("资源表达式解析为空时返回业务错误而不是 NPE")
    void missingTargetIdThrowsBusinessException() {
        setSecurityContext("user-001", UserType.STUDENT);
        ProceedingJoinPoint joinPoint = joinPoint("byDto", new Object[]{new TestDto(null)});

        assertThatThrownBy(() -> aspect.checkResourceAccess(joinPoint))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("资源ID不能为空");
    }

    @ResourceAccess(id = "#userId")
    void byUserId(String userId) {
    }

    @ResourceAccess(id = "#dto.userId")
    void byDto(TestDto dto) {
    }

    private ProceedingJoinPoint joinPoint(String methodName, Object[] args) {
        Method method = findMethod(methodName);
        MethodSignature signature = mock(MethodSignature.class);
        when(signature.getMethod()).thenReturn(method);

        ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);
        when(joinPoint.getSignature()).thenReturn(signature);
        when(joinPoint.getArgs()).thenReturn(args);
        return joinPoint;
    }

    private Method findMethod(String methodName) {
        for (Method method : getClass().getDeclaredMethods()) {
            if (method.getName().equals(methodName)) {
                return method;
            }
        }
        throw new AssertionError("Method not found: " + methodName);
    }

    private void setSecurityContext(String userId, UserType userType) {
        CurrentUserPrincipal principal = new CurrentUserPrincipal() {
            @Override
            public String getId() {
                return userId;
            }

            @Override
            public UserType getUserType() {
                return userType;
            }

            @Override
            public String getUniversityId() {
                return "uni-001";
            }
        };
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, Collections.emptyList())
        );
    }

    private record TestDto(String userId) {
    }
}
