package com.rauio.smartdangjian.aop;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;
import java.util.List;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import com.rauio.smartdangjian.aop.annotation.ResourceAccess;
import com.rauio.smartdangjian.aop.resolver.ResourceOwnerResolver;
import com.rauio.smartdangjian.exception.BusinessException;
import com.rauio.smartdangjian.security.CurrentUserPrincipal;
import com.rauio.smartdangjian.utils.spec.UserType;

import cn.dev33.satoken.stp.StpUtil;

@DisplayName("ResourceAccessAspect 单元测试")
class ResourceAccessAspectTest {

    private final ResourceAccessAspect aspect = new ResourceAccessAspect();
    private MockedStatic<StpUtil> stpUtilMock;

    @BeforeEach
    void setUp() {
        stpUtilMock = mockStatic(StpUtil.class);
    }

    @AfterEach
    void clearContext() {
        if (stpUtilMock != null) {
            stpUtilMock.close();
        }
    }

    @Test
    @DisplayName("当前用户访问自己的资源时放行")
    void allowsOwnerAccess() throws Throwable {
        setSecurityContext(1L, UserType.STUDENT);
        ProceedingJoinPoint joinPoint = joinPoint("byUserId", new Object[] {"1"});
        when(joinPoint.proceed()).thenReturn("ok");

        assertThatCode(() -> aspect.checkResourceAccess(joinPoint)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("当前用户访问他人资源时拒绝")
    void rejectsOtherUserAccess() {
        setSecurityContext(1L, UserType.STUDENT);
        ProceedingJoinPoint joinPoint = joinPoint("byUserId", new Object[] {"2"});

        assertThatThrownBy(() -> aspect.checkResourceAccess(joinPoint))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("权限不足");
    }

    @Test
    @DisplayName("资源表达式解析为空时返回业务错误而不是 NPE")
    void missingTargetIdThrowsBusinessException() {
        setSecurityContext(1L, UserType.STUDENT);
        ProceedingJoinPoint joinPoint = joinPoint("byDto", new Object[] {new TestDto(null)});

        assertThatThrownBy(() -> aspect.checkResourceAccess(joinPoint))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("资源ID不能为空");
    }

    @Test
    @DisplayName("非用户资源通过 resolver 解析到当前用户时放行")
    void allowsResolvedResourceOwnerAccess() throws Throwable {
        setSecurityContext(1L, UserType.STUDENT);
        ResourceOwnerResolver resolver = resourceOwnerResolver("RESOURCE_META", "1");
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
        setSecurityContext(1L, UserType.STUDENT);
        ResourceOwnerResolver resolver = resourceOwnerResolver("RESOURCE_META", "2");
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
        setSecurityContext(1L, UserType.STUDENT);
        ProceedingJoinPoint joinPoint = joinPoint("byResourceMeta", new Object[] {"resource-001"});

        assertThatThrownBy(() -> aspect.checkResourceAccess(joinPoint))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("无法解析资源归属");
    }

    @Test
    @DisplayName("管理员访问任何资源直接放行")
    void managerAccessBypassesPermissionCheck() throws Throwable {
        var session = mock(cn.dev33.satoken.session.SaSession.class);
        CurrentUserPrincipal principal = new CurrentUserPrincipal() {
            @Override
            public Long getId() {
                return 1L;
            }

            @Override
            public UserType getUserType() {
                return UserType.MANAGER;
            }

            @Override
            public String getUniversityId() {
                return "uni-001";
            }
        };
        when(session.get("user")).thenReturn(principal);
        stpUtilMock.when(StpUtil::getSession).thenReturn(session);
        stpUtilMock.when(StpUtil::isLogin).thenReturn(true);
        stpUtilMock.when(StpUtil::getLoginIdAsString).thenReturn("1");

        ProceedingJoinPoint joinPoint = joinPoint("byUserId", new Object[] {"999"});
        when(joinPoint.proceed()).thenReturn("manager-access");

        Object result = aspect.checkResourceAccess(joinPoint);

        assertThat(result).isEqualTo("manager-access");
        verify(joinPoint).proceed();
    }

    @Test
    @DisplayName("SpEL 解析结果为数值时转换为字符串")
    void numericSpelResultReturnsString() throws Throwable {
        setSecurityContext(1L, UserType.STUDENT);
        ProceedingJoinPoint joinPoint = joinPoint("byNumericId", new Object[] {1L});
        when(joinPoint.proceed()).thenReturn("ok");

        assertThatCode(() -> aspect.checkResourceAccess(joinPoint)).doesNotThrowAnyException();
        verify(joinPoint).proceed();
    }

    @Test
    @DisplayName("SpEL 属性访问失败时返回业务错误")
    void spelEvaluationExceptionReturnsNullTargetId() {
        setSecurityContext(1L, UserType.STUDENT);
        ProceedingJoinPoint joinPoint = joinPoint("byNonExistentField", new Object[] {new TestDto("1")});

        assertThatThrownBy(() -> aspect.checkResourceAccess(joinPoint))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("资源ID不能为空");
    }

    @ResourceAccess(id = "#userId")
    void byUserId(String userId) {}

    @ResourceAccess(id = "#dto.userId")
    void byDto(TestDto dto) {}

    @ResourceAccess(id = "#resourceId", type = "RESOURCE_META")
    void byResourceMeta(String resourceId) {}

    @ResourceAccess(id = "#id")
    void byNumericId(Long id) {}

    @ResourceAccess(id = "#dto.nonExistentField")
    void byNonExistentField(TestDto dto) {}

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

    private void setSecurityContext(Long userId, UserType userType) {
        CurrentUserPrincipal principal = new CurrentUserPrincipal() {
            @Override
            public Long getId() {
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
        stpUtilMock.when(StpUtil::isLogin).thenReturn(true);
        String loginId = String.valueOf(userId);
        stpUtilMock.when(StpUtil::getLoginIdAsString).thenReturn(loginId);
    }

    private ResourceOwnerResolver resourceOwnerResolver(String resourceType, String ownerId) {
        ResourceOwnerResolver resolver = mock(ResourceOwnerResolver.class);
        when(resolver.supports(resourceType)).thenReturn(true);
        when(resolver.findResourceOwner("resource-001")).thenReturn(ownerId);
        return resolver;
    }

    private record TestDto(String userId) {}

    @ResourceAccess(id = "#id")
    void byLongId(Long id) {}

    @Test
    @DisplayName("SpEL 表达式解析为 Number 类型时转换为字符串")
    void spelResultIsNumber() throws Throwable {
        setSecurityContext(1L, UserType.STUDENT);
        ProceedingJoinPoint joinPoint = joinPoint("byLongId", new Object[] {1L});
        when(joinPoint.proceed()).thenReturn("ok");

        assertThatCode(() -> aspect.checkResourceAccess(joinPoint)).doesNotThrowAnyException();
        verify(joinPoint).proceed();
    }

    @Test
    @DisplayName("SpEL 解析异常时在 catch 块中返回 null 并抛出业务异常")
    void spelCatchBlockReturnsNull() {
        setSecurityContext(1L, UserType.STUDENT);
        ProceedingJoinPoint joinPoint = joinPoint("byNonExistentField", new Object[] {"arg"});

        assertThatThrownBy(() -> aspect.checkResourceAccess(joinPoint))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("资源ID不能为空");
    }
}
