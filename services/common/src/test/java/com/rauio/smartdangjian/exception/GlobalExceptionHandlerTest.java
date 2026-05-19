package com.rauio.smartdangjian.exception;

import com.auth0.jwt.exceptions.TokenExpiredException;
import com.rauio.smartdangjian.pojo.response.Result;
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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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
    @DisplayName("handleTokenExpiredException 返回 401 和过期信息")
    void handleTokenExpiredException() {
        TokenExpiredException ex = mock(TokenExpiredException.class);

        Result<?> result = handler.handleTokenExpiredException(ex);

        assertThat(result.getCode()).isEqualTo("401");
        assertThat(result.getMessage()).isEqualTo("登录已过期，请重新登录");
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
    @DisplayName("handleRuntimeExceptions 返回 500 和服务器错误信息")
    void handleRuntimeException() {
        RuntimeException ex = new RuntimeException("internal error");

        Result<?> result = handler.handleRuntimeExceptions(ex);

        assertThat(result.getCode()).isEqualTo("500");
        assertThat(result.getMessage()).isEqualTo("服务器内部错误，请稍后重试");
    }
}
