package com.rauio.smartdangjian.aop;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;

import com.rauio.smartdangjian.aop.annotation.DataScopeAccess;
import com.rauio.smartdangjian.aop.support.DataScopeContext;
import com.rauio.smartdangjian.aop.support.DataScopeResolver;
import com.rauio.smartdangjian.constants.ErrorConstants;
import com.rauio.smartdangjian.exception.BusinessException;
import com.rauio.smartdangjian.security.CurrentUserPrincipal;
import com.rauio.smartdangjian.utils.SecurityUtils;

class DataScopeAccessAspectTest {

    private MockedStatic<SecurityUtils> securityUtilsMock;
    private DataScopeResolver resolver;
    private DataScopeAccess access;
    private ProceedingJoinPoint joinPoint;

    @BeforeEach
    void setUp() {
        securityUtilsMock = mockStatic(SecurityUtils.class);
        resolver = mock(DataScopeResolver.class, Answers.CALLS_REAL_METHODS);
        access = mock(DataScopeAccess.class);
        joinPoint = mock(ProceedingJoinPoint.class);
    }

    @AfterEach
    void tearDown() {
        securityUtilsMock.close();
    }

    @Test
    @DisplayName("当前用户不是 CurrentUserPrincipal 时抛出 BusinessException(USER_NOT_EXISTS)")
    void noCurrentUserThrows() {
        securityUtilsMock.when(SecurityUtils::getCurrentUser).thenReturn(null);

        DataScopeAccessAspect aspect = new DataScopeAccessAspect(List.of(resolver));

        assertThatThrownBy(() -> aspect.checkAccess(joinPoint, access))
                .isInstanceOf(BusinessException.class)
                .extracting("code")
                .isEqualTo(ErrorConstants.USER_NOT_EXISTS);
    }

    @Test
    @DisplayName("未找到支持的 DataScopeResolver 时抛出 BusinessException(RESOURCE_NOT_AUTHORIZED)")
    void noResolverFoundThrows() {
        CurrentUserPrincipal user = mock(CurrentUserPrincipal.class);
        securityUtilsMock.when(SecurityUtils::getCurrentUser).thenReturn(user);
        when(access.resource()).thenReturn("UNKNOWN_RESOURCE");
        when(resolver.supports("UNKNOWN_RESOURCE")).thenReturn(false);

        DataScopeAccessAspect aspect = new DataScopeAccessAspect(List.of(resolver));

        assertThatThrownBy(() -> aspect.checkAccess(joinPoint, access))
                .isInstanceOf(BusinessException.class)
                .extracting("code")
                .isEqualTo(ErrorConstants.RESOURCE_NOT_AUTHORIZED);
    }

    @Test
    @DisplayName("checkAccess 成功时调用 resolver.before 和 resolver.after")
    void checkAccessCallsResolverAndProceed() throws Throwable {
        CurrentUserPrincipal user = mock(CurrentUserPrincipal.class);
        securityUtilsMock.when(SecurityUtils::getCurrentUser).thenReturn(user);
        when(access.resource()).thenReturn("USER_MANAGEMENT");
        when(resolver.supports("USER_MANAGEMENT")).thenReturn(true);
        when(joinPoint.proceed()).thenReturn("proceed-result");

        DataScopeAccessAspect aspect = new DataScopeAccessAspect(List.of(resolver));
        Object result = aspect.checkAccess(joinPoint, access);

        assertThat(result).isEqualTo("proceed-result");
        ArgumentCaptor<DataScopeContext> contextCaptor = ArgumentCaptor.forClass(DataScopeContext.class);
        verify(resolver).before(contextCaptor.capture());
        verify(resolver).after(contextCaptor.capture(), any());

        DataScopeContext capturedContext = contextCaptor.getAllValues().get(0);
        assertThat(capturedContext.getCurrentUser()).isEqualTo(user);
        assertThat(capturedContext.getAccess()).isEqualTo(access);
        assertThat(capturedContext.getJoinPoint()).isEqualTo(joinPoint);
    }
}
