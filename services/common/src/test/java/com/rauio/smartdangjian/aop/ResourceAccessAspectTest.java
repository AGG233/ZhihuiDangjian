package com.rauio.smartdangjian.aop;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
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
import com.rauio.smartdangjian.constants.ErrorConstants;
import com.rauio.smartdangjian.exception.BusinessException;
import com.rauio.smartdangjian.utils.SecurityUtils;
import com.rauio.smartdangjian.utils.spec.UserType;

class ResourceAccessAspectTest {

    private MockedStatic<SecurityUtils> securityUtilsMock;
    private ProceedingJoinPoint joinPoint;
    private MethodSignature signature;
    private ResourceOwnerResolver resolver;

    @BeforeEach
    void setUp() {
        securityUtilsMock = mockStatic(SecurityUtils.class);
        joinPoint = mock(ProceedingJoinPoint.class);
        signature = mock(MethodSignature.class);
        resolver = mock(ResourceOwnerResolver.class);
    }

    @AfterEach
    void tearDown() {
        securityUtilsMock.close();
    }

    @Test
    @DisplayName("非法 SpEL 表达式时抛出 BusinessException(ARGS_ERROR)")
    void invalidSpelExpressionThrows() throws Throwable {
        Method method = TestTarget.class.getMethod("methodWithId");
        when(signature.getMethod()).thenReturn(method);
        when(joinPoint.getSignature()).thenReturn(signature);

        ResourceAccessAspect aspect = new ResourceAccessAspect(List.of());

        assertThatThrownBy(() -> aspect.checkResourceAccess(joinPoint))
                .isInstanceOf(BusinessException.class)
                .extracting("code")
                .isEqualTo(ErrorConstants.ARGS_ERROR);
    }

    @Test
    @DisplayName("资源ID为空时抛出 BusinessException(ARGS_ERROR)")
    void blankResourceIdThrows() throws Throwable {
        Method method = TestTarget.class.getMethod("methodWithBlankId");
        when(signature.getMethod()).thenReturn(method);
        when(joinPoint.getSignature()).thenReturn(signature);

        ResourceAccessAspect aspect = new ResourceAccessAspect(List.of());
        securityUtilsMock.when(SecurityUtils::getCurrentUserId).thenReturn("current-user");
        securityUtilsMock.when(SecurityUtils::getCurrentUserType).thenReturn(UserType.STUDENT);

        assertThatThrownBy(() -> aspect.checkResourceAccess(joinPoint))
                .isInstanceOf(BusinessException.class)
                .extracting("code")
                .isEqualTo(ErrorConstants.ARGS_ERROR);
    }

    @Test
    @DisplayName("当前用户为 MANAGER 时直接放行")
    void managerBypassesCheck() throws Throwable {
        Method method = TestTarget.class.getMethod("methodWithIdParam", String.class);
        when(signature.getMethod()).thenReturn(method);
        when(signature.getParameterNames()).thenReturn(new String[] {"userId"});
        when(joinPoint.getSignature()).thenReturn(signature);
        when(joinPoint.getArgs()).thenReturn(new Object[] {"target-user"});
        when(joinPoint.proceed()).thenReturn("proceed-result");

        securityUtilsMock.when(SecurityUtils::getCurrentUserId).thenReturn("admin-user");
        securityUtilsMock.when(SecurityUtils::getCurrentUserType).thenReturn(UserType.MANAGER);

        ResourceAccessAspect aspect = new ResourceAccessAspect(List.of());
        Object result = aspect.checkResourceAccess(joinPoint);

        assertThat(result).isEqualTo("proceed-result");
    }

    @Test
    @DisplayName("资源类型为 USER 时直接比较 ID，匹配则放行")
    void userTypeMatchProceeds() throws Throwable {
        Method method = TestTarget.class.getMethod("methodWithIdParam", String.class);
        when(signature.getMethod()).thenReturn(method);
        when(signature.getParameterNames()).thenReturn(new String[] {"userId"});
        when(joinPoint.getSignature()).thenReturn(signature);
        when(joinPoint.getArgs()).thenReturn(new Object[] {"current-user"});
        when(joinPoint.proceed()).thenReturn("proceed-result");

        securityUtilsMock.when(SecurityUtils::getCurrentUserId).thenReturn("current-user");
        securityUtilsMock.when(SecurityUtils::getCurrentUserType).thenReturn(UserType.STUDENT);

        ResourceAccessAspect aspect = new ResourceAccessAspect(List.of());
        Object result = aspect.checkResourceAccess(joinPoint);

        assertThat(result).isEqualTo("proceed-result");
    }

    @Test
    @DisplayName("资源类型为 USER 时 ID 不匹配则抛出 BusinessException(RESOURCE_NOT_AUTHORIZED)")
    void userTypeMismatchThrows() throws Throwable {
        Method method = TestTarget.class.getMethod("methodWithIdParam", String.class);
        when(signature.getMethod()).thenReturn(method);
        when(signature.getParameterNames()).thenReturn(new String[] {"userId"});
        when(joinPoint.getSignature()).thenReturn(signature);
        when(joinPoint.getArgs()).thenReturn(new Object[] {"other-user"});

        securityUtilsMock.when(SecurityUtils::getCurrentUserId).thenReturn("current-user");
        securityUtilsMock.when(SecurityUtils::getCurrentUserType).thenReturn(UserType.STUDENT);

        ResourceAccessAspect aspect = new ResourceAccessAspect(List.of());
        assertThatThrownBy(() -> aspect.checkResourceAccess(joinPoint))
                .isInstanceOf(BusinessException.class)
                .extracting("code")
                .isEqualTo(ErrorConstants.RESOURCE_NOT_AUTHORIZED);
    }

    @Test
    @DisplayName("自定义资源类型通过 resolver 解析归属并放行")
    void customResourceTypeMatchProceeds() throws Throwable {
        Method method = TestTarget.class.getMethod("methodWithCourseParam", String.class);
        when(signature.getMethod()).thenReturn(method);
        when(signature.getParameterNames()).thenReturn(new String[] {"courseId"});
        when(joinPoint.getSignature()).thenReturn(signature);
        when(joinPoint.getArgs()).thenReturn(new Object[] {"course-1"});
        when(joinPoint.proceed()).thenReturn("proceed-result");

        securityUtilsMock.when(SecurityUtils::getCurrentUserId).thenReturn("current-user");
        securityUtilsMock.when(SecurityUtils::getCurrentUserType).thenReturn(UserType.STUDENT);
        when(resolver.supports("COURSE")).thenReturn(true);
        when(resolver.findResourceOwner("course-1")).thenReturn("current-user");

        ResourceAccessAspect aspect = new ResourceAccessAspect(List.of(resolver));
        Object result = aspect.checkResourceAccess(joinPoint);

        assertThat(result).isEqualTo("proceed-result");
    }

    @Test
    @DisplayName("自定义资源类型无法解析归属时抛出 BusinessException(RESOURCE_NOT_AUTHORIZED)")
    void customResourceTypeNoResolverThrows() throws Throwable {
        Method method = TestTarget.class.getMethod("methodWithCourseParam", String.class);
        when(signature.getMethod()).thenReturn(method);
        when(signature.getParameterNames()).thenReturn(new String[] {"courseId"});
        when(joinPoint.getSignature()).thenReturn(signature);
        when(joinPoint.getArgs()).thenReturn(new Object[] {"course-1"});

        securityUtilsMock.when(SecurityUtils::getCurrentUserId).thenReturn("current-user");
        securityUtilsMock.when(SecurityUtils::getCurrentUserType).thenReturn(UserType.STUDENT);
        when(resolver.supports("COURSE")).thenReturn(false);

        ResourceAccessAspect aspect = new ResourceAccessAspect(List.of(resolver));
        assertThatThrownBy(() -> aspect.checkResourceAccess(joinPoint))
                .isInstanceOf(BusinessException.class)
                .extracting("code")
                .isEqualTo(ErrorConstants.RESOURCE_NOT_AUTHORIZED);
    }

    static class TestTarget {
        @ResourceAccess(id = "")
        public void methodWithId() {}

        @ResourceAccess(id = "'   '")
        public void methodWithBlankId() {}

        @ResourceAccess(id = "#userId")
        public void methodWithIdParam(String userId) {}

        @ResourceAccess(id = "#courseId", type = "COURSE")
        public void methodWithCourseParam(String courseId) {}
    }
}
