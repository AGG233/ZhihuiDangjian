package com.rauio.smartdangjian.server.auth.pojo.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {

    @Schema(
            description =
                    "访问令牌（无状态 JWT），在请求头的Authorization字段附带信息: Bearer <替换为accessToken>即可访问受限制的接口；过期后使用 refreshToken 刷新")
    private String accessToken;

    @Schema(description = "刷新令牌，仅用于调用 /api/auth/refresh 换取新的访问令牌，请勿用于业务接口鉴权")
    private String refreshToken;

    @Schema(description = "访问令牌有效期（秒）", example = "7200")
    private Long expiresIn;
}
