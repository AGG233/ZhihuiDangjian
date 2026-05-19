package com.rauio.smartdangjian.aop.support;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.rauio.smartdangjian.aop.annotation.DataScopeAccess;
import com.rauio.smartdangjian.constants.ErrorConstants;
import com.rauio.smartdangjian.exception.BusinessException;
import com.rauio.smartdangjian.security.CurrentUserPrincipal;

@ExtendWith(MockitoExtension.class)
class DataScopeContextTest {

    @Mock
    private ProceedingJoinPoint joinPoint;

    @Mock
    private DataScopeAccess access;

    @Mock
    private CurrentUserPrincipal currentUser;

    @Test
    @DisplayName("resolve 表达式为空时返回 null")
    void resolveBlankReturnsNull() {
        DataScopeContext context = new DataScopeContext(joinPoint, access, currentUser);

        Object result = context.resolve("", String.class);

        assertThat(result).isNull();
    }

    @Test
    @DisplayName("resolve 非法表达式时抛出 BusinessException(ARGS_ERROR)")
    void resolveInvalidExpressionThrows() {
        DataScopeContext context = new DataScopeContext(joinPoint, access, currentUser);

        assertThatThrownBy(() -> context.resolve("invalid expression with spaces", String.class))
                .isInstanceOf(BusinessException.class)
                .extracting("code")
                .isEqualTo(ErrorConstants.ARGS_ERROR);
    }

    @Test
    @DisplayName("require 传入空表达式时抛出 BusinessException(ARGS_ERROR)")
    void requireNullThrows() {
        DataScopeContext context = new DataScopeContext(joinPoint, access, currentUser);

        assertThatThrownBy(() -> context.require("", String.class, "ID不能为空"))
                .isInstanceOf(BusinessException.class)
                .extracting("code")
                .isEqualTo(ErrorConstants.ARGS_ERROR);
    }

    @Test
    @DisplayName("resolve 解析字面量表达式并返回结果")
    void resolveLiteralExpression() throws Exception {
        Method method = TestTarget.class.getMethod("method", String.class);
        MethodSignature signature = mock(MethodSignature.class);
        when(signature.getMethod()).thenReturn(method);
        when(joinPoint.getSignature()).thenReturn(signature);

        DataScopeContext context = new DataScopeContext(joinPoint, access, currentUser);

        String result = context.resolve("'test-value'", String.class);

        assertThat(result).isEqualTo("test-value");
    }

    @Test
    @DisplayName("require 成功解析字面量并返回结果")
    void requireReturnsValue() throws Exception {
        Method method = TestTarget.class.getMethod("method", String.class);
        MethodSignature signature = mock(MethodSignature.class);
        when(signature.getMethod()).thenReturn(method);
        when(joinPoint.getSignature()).thenReturn(signature);

        DataScopeContext context = new DataScopeContext(joinPoint, access, currentUser);

        String result = context.require("'valid-id'", String.class, "ID不能为空");

        assertThat(result).isEqualTo("valid-id");
    }

    @Test
    @DisplayName("getter 方法正确返回构造参数")
    void getters() {
        DataScopeContext context = new DataScopeContext(joinPoint, access, currentUser);

        assertThat(context.getJoinPoint()).isEqualTo(joinPoint);
        assertThat(context.getAccess()).isEqualTo(access);
        assertThat(context.getCurrentUser()).isEqualTo(currentUser);
    }

    static class TestTarget {
        public void method(String id) {}
    }
}
