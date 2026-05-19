package com.rauio.smartdangjian.aop;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import com.rauio.smartdangjian.aop.annotation.ResourceAccess;
import com.rauio.smartdangjian.aop.resolver.ResourceOwnerResolver;
import com.rauio.smartdangjian.exception.BusinessException;
import com.rauio.smartdangjian.security.CurrentUserPrincipal;
import com.rauio.smartdangjian.utils.spec.UserType;

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
        ProceedingJoinPoint joinPoint = joinPoint("byUserId", new Object[] {"user-001"});
        when(joinPoint.proceed()).thenReturn("ok");

        assertThatCode(() -> aspect.checkResourceAccess(joinPoint)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("当前用户访问他人资源时拒绝")
    void rejectsOtherUserAccess() {
        setSecurityContext("user-001", UserType.STUDENT);
        ProceedingJoinPoint joinPoint = joinPoint("byUserId", new Object[] {"user-002"});

        assertThatThrownBy(() -> aspect.checkResourceAccess(joinPoint))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("权限不足");
    }

    @Test
    @DisplayName("资源表达式解析为空时返回业务错误而不是 NPE")
    void missingTargetIdThrowsBusinessException() {
        setSecurityContext("user-001", UserType.STUDENT);
        ProceedingJoinPoint joinPoint = joinPoint("byDto", new Object[] {new TestDto(null)});

        assertThatThrownBy(() -> aspect.checkResourceAccess(joinPoint))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("资源ID不能为空");
    }

    @Test
    @DisplayName("非用户资源通过 resolver 解析到当前用户时放行")
    void allowsResolvedResourceOwnerAccess() throws Throwable {
        setSecurityContext("user-001", UserType.STUDENT);
        ResourceOwnerResolver resolver = resourceOwnerResolver("RESOURCE_META", "user-001");
        ResourceAccessAspect resourceAspect = new ResourceAccessAspect(List.of(resolver));
        ProceedingJoinPoint joinPoint = joinPoint("byResourceMeta", new Object[] {"resource-001"});
        when(joinPoint.proceed()).thenReturn("ok");

        Object result = resourceAspect.checkResourceAccess(joinPoint);

        assertThat(result).isEqualTo("ok");
        verify(resolver).findResourceOwner("resource-001");
    }

    @Test
    @DisplayName("非用户资源解析到其他用户时拒绝访问")
    void rejectsResolvedResourceOwnedByOtherUser() {
        setSecurityContext("user-001", UserType.STUDENT);
        ResourceOwnerResolver resolver = resourceOwnerResolver("RESOURCE_META", "user-002");
        ResourceAccessAspect resourceAspect = new ResourceAccessAspect(List.of(resolver));
        ProceedingJoinPoint joinPoint = joinPoint("byResourceMeta", new Object[] {"resource-001"});

        assertThatThrownBy(() -> resourceAspect.checkResourceAccess(joinPoint))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("权限不足");
        verify(resolver).findResourceOwner("resource-001");
    }

    @Test
    @DisplayName("非用户资源无法解析归属时拒绝访问")
    void rejectsResourceWhenOwnerCannotBeResolved() {
        setSecurityContext("user-001", UserType.STUDENT);
        ProceedingJoinPoint joinPoint = joinPoint("byResourceMeta", new Object[] {"resource-001"});

        assertThatThrownBy(() -> aspect.checkResourceAccess(joinPoint))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("无法解析资源归属");
    }

    @ResourceAccess(id = "#userId")
    void byUserId(String userId) {}

    @ResourceAccess(id = "#dto.userId")
    void byDto(TestDto dto) {}

    @ResourceAccess(id = "#resourceId", type = "RESOURCE_META")
    void byResourceMeta(String resourceId) {}

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
        SecurityContextHolder.getContext()
                .setAuthentication(new UsernamePasswordAuthenticationToken(principal, null, Collections.emptyList()));
    }

    private ResourceOwnerResolver resourceOwnerResolver(String resourceType, String ownerId) {
        ResourceOwnerResolver resolver = mock(ResourceOwnerResolver.class);
        when(resolver.supports(resourceType)).thenReturn(true);
        when(resolver.findResourceOwner("resource-001")).thenReturn(ownerId);
        return resolver;
    }

    private record TestDto(String userId) {}
}
