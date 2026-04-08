package com.rauio.smartdangjian.pojo.response;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

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

    @Schema(description = "响应数据")
    private T data;

    public Result() {
        this.code = "200";
        this.message = "OK";
    }

    public Result(T data) {
        this.code = "200";
        this.message = "OK";
        if ((data instanceof Boolean && !(Boolean) data) || Objects.isNull(data)) {
            this.code = "400";
            this.message = "Operation failed";
        }
        this.data = data;
    }

    @ApiResponse(responseCode = "200", description = "操作成功")
    public static <T> Result<T> ok(T data) {
        return new Result<>(data);
    }

    @ApiResponse(responseCode = "200", description = "操作成功")
    public static <T> Result<T> ok(String code, String message, T data) {
        return Result.<T>builder()
                .code(code)
                .message(message)
                .data(data)
                .build();
    }

    @ApiResponse(responseCode = "400", description = "操作失败，详情看信息")
    public static <T> Result<T> error(String code, String message) {
        return Result.<T>builder()
                .code(code)
                .message(message)
                .build();
    }

    @ApiResponse(responseCode = "500", description = "服务器内部错误，请联系开发")
    public static <T> Result<T> internalError(String code, String message) {
        return error(code, message);
    }
}
