package com.rauio.ZhihuiDangjian.pojo.response;

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

    @Schema(description = "刷新令牌，有效时间为七天，中间部分包含有用户的信息，accesss令牌过期时用此令牌在/auth/refresh接口获取新的令牌")
    private String  refreshToken;
    @Schema(description = "访问令牌，有效时间为一个小时，中间部分包含有用户的信息，在请求头的Authorization字段附带信息: Bearer <替换为accesssToken>即可访问受限制的接口")
    private String  accessToken;
}