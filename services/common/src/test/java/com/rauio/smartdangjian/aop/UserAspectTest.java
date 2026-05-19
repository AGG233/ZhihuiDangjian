package com.rauio.smartdangjian.aop;

import com.rauio.smartdangjian.aop.annotation.PermissionAccess;
import com.rauio.smartdangjian.constants.ErrorConstants;
import com.rauio.smartdangjian.exception.BusinessException;
import com.rauio.smartdangjian.utils.SecurityUtils;
import com.rauio.smartdangjian.utils.spec.UserType;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

class UserAspectTest {

    private final UserAspect aspect = new UserAspect();
    private MockedStatic<SecurityUtils> securityUtilsMock;

    @BeforeEach
    void setUp() {
        securityUtilsMock = mockStatic(SecurityUtils.class);
    }

    @AfterEach
    void tearDown() {
        securityUtilsMock.close();
    }

    @Test
    @DisplayName("注解不存在时抛出 BusinessException(RESOURCE_NOT_AUTHORIZED)")
    void noAnnotationThrows() throws Exception {
        Method method = NoAnnotationClass.class.getMethod("method");
        MethodSignature signature = mockSignature(method);
        JoinPoint joinPoint = mockJoinPoint(signature, new NoAnnotationClass());

        assertThatThrownBy(() -> aspect.checkPermissionAccess(joinPoint))
                .isInstanceOf(BusinessException.class)
                .extracting("code")
                .isEqualTo(ErrorConstants.RESOURCE_NOT_AUTHORIZED);
    }

    @Test
    @DisplayName("当前用户类型为 null 时抛出 BusinessException(RESOURCE_NOT_AUTHORIZED)")
    void nullUserTypeThrows() throws Exception {
        Method method = StudentLevelClass.class.getMethod("method");
        MethodSignature signature = mockSignature(method);
        JoinPoint joinPoint = mockJoinPoint(signature, new StudentLevelClass());

        securityUtilsMock.when(SecurityUtils::getCurrentUserType).thenReturn(null);

        assertThatThrownBy(() -> aspect.checkPermissionAccess(joinPoint))
                .isInstanceOf(BusinessException.class)
                .extracting("code")
                .isEqualTo(ErrorConstants.RESOURCE_NOT_AUTHORIZED);
    }

    @Test
    @DisplayName("用户权限等级低于注解要求时抛出 BusinessException(RESOURCE_NOT_AUTHORIZED)")
    void insufficientPermissionThrows() throws Exception {
        Method method = SchoolLevelClass.class.getMethod("method");
        MethodSignature signature = mockSignature(method);
        JoinPoint joinPoint = mockJoinPoint(signature, new SchoolLevelClass());

        securityUtilsMock.when(SecurityUtils::getCurrentUserType).thenReturn(UserType.STUDENT);

        assertThatThrownBy(() -> aspect.checkPermissionAccess(joinPoint))
                .isInstanceOf(BusinessException.class)
                .extracting("code")
                .isEqualTo(ErrorConstants.RESOURCE_NOT_AUTHORIZED);
    }

    @Test
    @DisplayName("用户权限等级与注解要求相同时通过")
    void samePermissionLevelPasses() throws Exception {
        Method method = StudentLevelClass.class.getMethod("method");
        MethodSignature signature = mockSignature(method);
        JoinPoint joinPoint = mockJoinPoint(signature, new StudentLevelClass());

        securityUtilsMock.when(SecurityUtils::getCurrentUserType).thenReturn(UserType.STUDENT);

        assertThatCode(() -> aspect.checkPermissionAccess(joinPoint))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("用户权限等级高于注解要求时通过")
    void higherPermissionLevelPasses() throws Exception {
        Method method = StudentLevelClass.class.getMethod("method");
        MethodSignature signature = mockSignature(method);
        JoinPoint joinPoint = mockJoinPoint(signature, new StudentLevelClass());

        securityUtilsMock.when(SecurityUtils::getCurrentUserType).thenReturn(UserType.MANAGER);

        assertThatCode(() -> aspect.checkPermissionAccess(joinPoint))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("类级别注解被正确读取")
    void classLevelAnnotationPasses() throws Exception {
        Method method = SchoolLevelClass.class.getMethod("method");
        MethodSignature signature = mockSignature(method);
        JoinPoint joinPoint = mockJoinPoint(signature, new SchoolLevelClass());

        securityUtilsMock.when(SecurityUtils::getCurrentUserType).thenReturn(UserType.SCHOOL);

        assertThatCode(() -> aspect.checkPermissionAccess(joinPoint))
                .doesNotThrowAnyException();
    }

    private MethodSignature mockSignature(Method method) {
        MethodSignature signature = mock(MethodSignature.class);
        when(signature.getMethod()).thenReturn(method);
        return signature;
    }

    private JoinPoint mockJoinPoint(MethodSignature signature, Object target) {
        JoinPoint joinPoint = mock(JoinPoint.class);
        when(joinPoint.getSignature()).thenReturn(signature);
        when(joinPoint.getTarget()).thenReturn(target);
        return joinPoint;
    }

    static class NoAnnotationClass {
        public void method() {}
    }

    @PermissionAccess(UserType.STUDENT)
    static class StudentLevelClass {
        public void method() {}
    }

    @PermissionAccess(UserType.SCHOOL)
    static class SchoolLevelClass {
        public void method() {}
    }
}
