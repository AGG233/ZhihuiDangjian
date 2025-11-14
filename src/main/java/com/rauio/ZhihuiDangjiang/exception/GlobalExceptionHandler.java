package com.rauio.ZhihuiDangjiang.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rauio.ZhihuiDangjiang.pojo.response.ApiResponse;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.bind.BindException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.io.IOException;

import static com.baomidou.mybatisplus.extension.ddl.DdlScriptErrorHandler.PrintlnLogErrorHandler.log;

@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    @Autowired
    private ObjectMapper objectMapper;

    @ExceptionHandler(BusinessException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public void handleBusinessExceptions(BusinessException e, HttpServletResponse response) throws IOException {
        ResponseEntity<ApiResponse> responseEntity = buildResponse(
                HttpStatus.BAD_REQUEST,
                String.valueOf(e.getCode()),
                e.getMessage());
        writeResponse(response, responseEntity);
    }
    
    @ExceptionHandler(value = RuntimeException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public void handleRuntimeExceptions(RuntimeException e, HttpServletResponse response) throws IOException {
        log.error("[ERROR]", e);
        ResponseEntity<ApiResponse> responseEntity = buildResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                HttpStatus.INTERNAL_SERVER_ERROR.toString(),
                "服务器内部出现异常，请重试"
        );
        writeResponse(response, responseEntity);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public void handleArgumentNotValidExceptions(MethodArgumentNotValidException e, HttpServletResponse response) throws IOException {
        ResponseEntity<ApiResponse> responseEntity = buildResponse(
                HttpStatus.BAD_REQUEST,
                HttpStatus.BAD_REQUEST.toString(),
                e.getBindingResult().getFieldErrors().getFirst().getDefaultMessage()
        );
        writeResponse(response, responseEntity);
    }

    @ExceptionHandler(BindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public void handleBindException(BindException e, HttpServletResponse response) throws IOException {
        ResponseEntity<ApiResponse> responseEntity = buildResponse(
                HttpStatus.BAD_REQUEST,
                HttpStatus.BAD_REQUEST.toString(),
                e.getMessage()
        );
        writeResponse(response, responseEntity);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public void handleMissingServletRequestParameterException(MissingServletRequestParameterException e, HttpServletResponse response) throws IOException {
        String parameterName = e.getParameterName();
        String parameterType = e.getParameterType();

        ResponseEntity<ApiResponse> responseEntity = buildResponse(
                HttpStatus.BAD_REQUEST,
                HttpStatus.BAD_REQUEST.toString(),
                "参数：" + parameterName + "不存在"
        );
        writeResponse(response, responseEntity);
    }
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public void handleExceptions(Exception e, HttpServletResponse response) throws IOException {
        log.error("[ERROR]", e);
        ResponseEntity<ApiResponse> responseEntity = buildResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                HttpStatus.INTERNAL_SERVER_ERROR.toString(),
                "服务器内部出现异常，请重试"
        );
        writeResponse(response, responseEntity);
    }
    
    private void writeResponse(HttpServletResponse response, ResponseEntity<ApiResponse> responseEntity) throws IOException {
        response.setStatus(responseEntity.getStatusCode().value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(responseEntity.getBody()));
    }
    
    public static ResponseEntity<ApiResponse> buildResponse(HttpStatus status, String code,String message) {
        ApiResponse errorBody = ApiResponse.builder()
                .code(code)
                .message(message)
                .build();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new ResponseEntity<>(errorBody, headers, status);
    }
}