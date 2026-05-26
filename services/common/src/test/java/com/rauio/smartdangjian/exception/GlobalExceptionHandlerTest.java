package com.rauio.smartdangjian.exception;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import com.rauio.smartdangjian.pojo.response.Result;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotRoleException;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    @DisplayName("handleBusinessExceptions 返回 BAD_REQUEST 和业务错误码")
    void handleBusinessException() {
        BusinessException ex = new BusinessException(1001, "验证码错误");

        Result<?> result = handler.handleBusinessExceptions(ex);

        assertThat(result.getCode()).isEqualTo("1001");
        assertThat(result.getMessage()).isEqualTo("验证码错误");
    }

    @Test
    @DisplayName("handleHttpMessageNotReadableException 返回 400 和请求体错误信息")
    void handleHttpMessageNotReadableException() {
        HttpMessageNotReadableException ex = new HttpMessageNotReadableException("test");

        Result<?> result = handler.handleHttpMessageNotReadableException(ex);

        assertThat(result.getCode()).isEqualTo("400");
        assertThat(result.getMessage()).isEqualTo("请求体缺失或格式错误");
    }

    @Test
    @DisplayName("handleArgumentNotValidExceptions 返回第一个字段校验错误信息")
    void handleArgumentNotValidException() {
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError = new FieldError("object", "username", "用户名不能为空");
        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError));

        Result<?> result = handler.handleArgumentNotValidExceptions(ex);

        assertThat(result.getCode()).isEqualTo("400");
        assertThat(result.getMessage()).isEqualTo("用户名不能为空");
    }

    @Test
    @DisplayName("handleConstraintViolationException 返回第一个约束违反信息")
    @SuppressWarnings("unchecked")
    void handleConstraintViolationException() {
        ConstraintViolation<Object> violation = mock(ConstraintViolation.class);
        Path path = mock(Path.class);
        when(violation.getMessage()).thenReturn("参数校验失败");
        when(violation.getPropertyPath()).thenReturn(path);

        Set<ConstraintViolation<Object>> violations = new HashSet<>();
        violations.add(violation);

        ConstraintViolationException ex = new ConstraintViolationException(violations);

        Result<?> result = handler.handleConstraintViolationException(ex);

        assertThat(result.getCode()).isEqualTo("400");
        assertThat(result.getMessage()).isEqualTo("参数校验失败");
    }

    @Test
    @DisplayName("handleNotLoginException 返回 401 和未登录信息")
    void handleNotLoginException() {
        NotLoginException ex = new NotLoginException("", "", "");

        Result<?> result = handler.handleNotLoginException(ex);

        assertThat(result.getCode()).isEqualTo("401");
        assertThat(result.getMessage()).isEqualTo("未登录或登录已过期，请重新登录");
    }

    @Test
    @DisplayName("handleNotRoleException 返回 403 和无权限信息")
    void handleNotRoleException() {
        NotRoleException ex = new NotRoleException("ADMIN");

        Result<?> result = handler.handleNotRoleException(ex);

        assertThat(result.getCode()).isEqualTo("403");
        assertThat(result.getMessage()).isEqualTo("无权限访问该资源");
    }

    @Test
    @DisplayName("handleDuplicateKeyException 返回 409 和数据已存在信息")
    void handleDuplicateKeyException() {
        DuplicateKeyException ex = new DuplicateKeyException("duplicate key");

        Result<?> result = handler.handleDuplicateKeyException(ex);

        assertThat(result.getCode()).isEqualTo("409");
        assertThat(result.getMessage()).isEqualTo("数据已存在");
    }

    @Test
    @DisplayName("handleIllegalArgumentException 返回 400 和异常消息")
    void handleIllegalArgumentException() {
        IllegalArgumentException ex = new IllegalArgumentException("参数不合法");

        Result<?> result = handler.handleIllegalArgumentException(ex);

        assertThat(result.getCode()).isEqualTo("400");
        assertThat(result.getMessage()).isEqualTo("参数不合法");
    }

    @Test
    @DisplayName("handleIllegalStateException 返回 409 和异常消息")
    void handleIllegalStateException() {
        IllegalStateException ex = new IllegalStateException("状态异常");

        Result<?> result = handler.handleIllegalStateException(ex);

        assertThat(result.getCode()).isEqualTo("409");
        assertThat(result.getMessage()).isEqualTo("状态异常");
    }

    @Test
    @DisplayName("handleMethodArgumentTypeMismatchException requiredType null returns 400 and fallback message")
    void handleMethodArgumentTypeMismatchExceptionNullRequiredType() {
        MethodArgumentTypeMismatchException ex = mock(MethodArgumentTypeMismatchException.class);
        when(ex.getName()).thenReturn("userId");
        when(ex.getRequiredType()).thenReturn(null);

        Result<?> result = handler.handleMethodArgumentTypeMismatchException(ex);

        assertThat(result.getCode()).isEqualTo("400");
        assertThat(result.getMessage()).isEqualTo("请求参数类型错误: userId 应为 合法值");
    }

    @Test
    @DisplayName("handleMethodArgumentTypeMismatchException with requiredType returns type name")
    void handleMethodArgumentTypeMismatchExceptionWithRequiredType() {
        MethodArgumentTypeMismatchException ex = mock(MethodArgumentTypeMismatchException.class);
        when(ex.getName()).thenReturn("sort");
        when(ex.getRequiredType()).thenReturn((Class) Integer.class);

        Result<?> result = handler.handleMethodArgumentTypeMismatchException(ex);

        assertThat(result.getCode()).isEqualTo("400");
        assertThat(result.getMessage()).isEqualTo("请求参数类型错误: sort 应为 Integer");
    }

    @Test
    @DisplayName("handleRuntimeExceptions returns 500 and server error message")
    void handleRuntimeException() {
        RuntimeException ex = new RuntimeException("internal error");

        Result<?> result = handler.handleRuntimeExceptions(ex);

        assertThat(result.getCode()).isEqualTo("500");
        assertThat(result.getMessage()).isEqualTo("服务器内部错误，请稍后重试");
    }
}
