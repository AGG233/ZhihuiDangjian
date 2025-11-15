package com.rauio.ZhihuiDangjian.pojo.response;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.springframework.http.ResponseEntity;

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
    
    public static ResponseEntity<String> buildResponse(Object data) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        ApiResponse apiResponse = new ApiResponse(data);
        String json = objectMapper.writeValueAsString(apiResponse);
        return ResponseEntity.ok(json);
    }
    
    public static ResponseEntity<String> buildResponse(String code, String message, Object data) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        ApiResponse apiResponse = ApiResponse.builder()
                .code(code)
                .message(message)
                .data(data)
                .build();
        String json = objectMapper.writeValueAsString(apiResponse);
        return ResponseEntity.ok(json);
    }
}