package com.rauio.ZhihuiDangjian.pojo.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;

import java.util.Objects;

@Slf4j
@Data
@Builder
@AllArgsConstructor
@Schema(description = "API的返回结果")
public class Result<T> {
    @Builder.Default
    @Schema(description = "响应码")
    private String code = "200";
    @Builder.Default
    @Schema(description = "响应信息，如果没有特殊信息一般为空或者OK")
    private String message = "OK";

    private T data;
    
    @Schema(hidden = true)
    @JsonIgnore
    private ResponseEntity<Result<T>> responseEntity;

    public Result() {
        this.code   = "200";
        this.message = "OK";
        this.responseEntity = ResponseEntity.ok(this);
    }

    public Result(T data) {
        this.code = "200";
        this.message = "OK";
        if ((data instanceof Boolean && !(Boolean) data) || Objects.isNull(data)) {
            this.code = "400";
            this.message = "Operation failed";
            this.data = data;
        } else {
            this.data = data;
        }
        this.responseEntity = ResponseEntity.ok(this);
    }

    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "操作成功")
    public static <T> Result<T> ok(T data) {
        Result<T> response = new Result<T>(data);
        response.responseEntity = ResponseEntity.ok(response);
        return response;
    }

    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "操作成功")
    public static <T> Result<T> ok(String code, String message, T data) {
        Result<T> response = Result.<T>builder()
                .code(code)
                .message(message)
                .data(data)
                .build();
        response.responseEntity = ResponseEntity.ok(response);
        return response;
    }

    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "操作失败，详情看信息")
    public static <T> Result<T> error(String code, String message) {
        Result<T> response = Result.<T>builder()
                .code(code)
                .message(message)
                .build();
        response.responseEntity = ResponseEntity.ok(response);
        return response;
    }

    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "服务器内部错误，请联系开发")
    public static <T> Result<T> internalError(String code, String message) {
        return error(code, message);
    }
}