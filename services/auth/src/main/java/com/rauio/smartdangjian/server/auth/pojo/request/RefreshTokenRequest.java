package com.rauio.smartdangjian.server.auth.pojo.request;

import jakarta.validation.constraints.NotBlank;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "刷新令牌请求体")
public class RefreshTokenRequest {

    @Schema(description = "登录时下发的刷新令牌", example = "3f2b8c6e-9d41-4a7b-b5a0-1c2d3e4f5a6b")
    @NotBlank(message = "请填写刷新令牌")
    private String refreshToken;
}
