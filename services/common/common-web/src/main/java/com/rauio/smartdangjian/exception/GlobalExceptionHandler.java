package com.rauio.smartdangjian.exception;

import jakarta.validation.ConstraintViolationException;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

import com.rauio.smartdangjian.constants.ErrorConstants;
import com.rauio.smartdangjian.pojo.response.Result;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotPermissionException;
import cn.dev33.satoken.exception.NotRoleException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    private Result buildResult(String code, String message) {
        return Result.builder().code(code).message(message).build();
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Result> handleBusinessExceptions(BusinessException e) {
        HttpStatus status =
                e.getCode() == ErrorConstants.RESOURCE_NOT_AUTHORIZED ? HttpStatus.FORBIDDEN : HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(status).body(buildResult(String.valueOf(e.getCode()), e.getMessage()));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result handleHttpMessageNotReadableException(HttpMessageNotReadableException e) {
        return buildResult("400", "请求体缺失或格式错误");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result handleArgumentNotValidExceptions(MethodArgumentNotValidException e) {
        String msg = e.getBindingResult().getFieldErrors().getFirst().getDefaultMessage();
        return buildResult("400", msg);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result handleConstraintViolationException(ConstraintViolationException e) {
        String msg = e.getConstraintViolations().iterator().next().getMessage();
        return buildResult("400", msg);
    }

    @ExceptionHandler(NotLoginException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public Result handleNotLoginException(NotLoginException e) {
        return buildResult("401", "未登录或登录已过期，请重新登录");
    }

    @ExceptionHandler(NotRoleException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public Result handleNotRoleException(NotRoleException e) {
        return buildResult("403", "无权限访问该资源");
    }

    @ExceptionHandler(NotPermissionException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public Result handleNotPermissionException(NotPermissionException e) {
        return buildResult("403", "无权限执行该操作");
    }

    @ExceptionHandler(DuplicateKeyException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public Result handleDuplicateKeyException(DuplicateKeyException e) {
        log.warn("数据重复:", e);
        return buildResult("409", "数据已存在");
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result handleDataIntegrityViolationException(DataIntegrityViolationException e) {
        log.warn("数据完整性约束违反:", e);
        return buildResult("400", "数据完整性约束违反，请检查请求参数");
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result handleIllegalArgumentException(IllegalArgumentException e) {
        return buildResult("400", e.getMessage());
    }

    @ExceptionHandler(IllegalStateException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public Result handleIllegalStateException(IllegalStateException e) {
        return buildResult("409", e.getMessage());
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException e) {
        return buildResult(
                "400",
                "请求参数类型错误: " + e.getName() + " 应为 "
                        + (e.getRequiredType() != null ? e.getRequiredType().getSimpleName() : "合法值"));
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Result<?> handleNoHandlerFound(NoHandlerFoundException e) {
        return buildResult("404", "请求的资源不存在");
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    public Result<?> handleMethodNotSupported(HttpRequestMethodNotSupportedException e) {
        return buildResult("405", "不支持的请求方法: " + e.getMethod());
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    @ResponseStatus(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
    public Result<?> handleMediaTypeNotSupported(HttpMediaTypeNotSupportedException e) {
        return buildResult("415", "不支持的媒体类型");
    }

    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result handleRuntimeExceptions(RuntimeException e) {
        log.error("系统运行时异常:", e);
        return buildResult("500", "服务器内部错误，请稍后重试");
    }
}
