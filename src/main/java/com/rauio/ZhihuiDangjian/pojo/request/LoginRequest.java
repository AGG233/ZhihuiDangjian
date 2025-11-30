package com.rauio.ZhihuiDangjian.pojo.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "登录请求体")
public class LoginRequest {
    @Schema(description = "用户的手机号码，邮箱，身份证号码等等")
    private String  passport;

    @Schema(description = "登录密码")
    private String  password;

    @Schema(description = "验证码的uuid")
    private String  captchaUUID;

    @Schema(description = "验证码")
    private String  captchaCode;
}