package com.rauio.smartdangjian.exception;

import com.auth0.jwt.exceptions.TokenExpiredException;
import com.rauio.smartdangjian.pojo.response.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    private Result buildResult(String code, String message) {
        return Result.builder()
                .code(code)
                .message(message)
                .build();
    }

    @ExceptionHandler(BusinessException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result handleBusinessExceptions(BusinessException e) {
        return buildResult(String.valueOf(e.getCode()), e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result handleArgumentNotValidExceptions(MethodArgumentNotValidException e) {
        String msg = e.getBindingResult().getFieldErrors().getFirst().getDefaultMessage();
        return buildResult("400", msg);
    }

    @ExceptionHandler(TokenExpiredException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public Result handleTokenExpiredException(TokenExpiredException e) {
        return buildResult("401", "登录已过期，请重新登录");
    }

    @ExceptionHandler(DuplicateKeyException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public Result handleDuplicateKeyException(DuplicateKeyException e) {
        return buildResult("409", "数据已存在：" + e.getMessage());
    }

    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result handleRuntimeExceptions(RuntimeException e) {
        log.error("系统运行时异常:", e);
        return buildResult("500", "服务器内部错误，请稍后重试");
    }
}
