package com.rauio.ZhihuiDangjian.pojo.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import java.util.Objects;

@Data
@Builder
@AllArgsConstructor
public class ApiResponse {
    @Builder.Default
    String code = "200";
    @Builder.Default
    String message = "OK";
    Object data;

    public ApiResponse() {
        this.code   = "200";
        this.message = "OK";
    }
    
    public ApiResponse(Object data) {
        this.code = "200";
        this.message = "OK";
        if ((data instanceof Boolean && !(Boolean) data) || Objects.isNull(data)) {
            this.code = "400";
            this.message = "Operation failed";
            this.data = data;
        } else {
            this.data = data;
        }
    }
}