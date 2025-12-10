package com.rauio.ZhihuiDangjian.pojo.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "登录请求体")
public class LoginRequest {

    @Schema(description = "用户的手机号码，邮箱，身份证号码等等",example = "admin")
    @NotBlank(message = "请填写用户名/手机号码/身份证号码")
    private String  passport;

    @Schema(description = "登录密码",example = "123456")
    @NotBlank(message = "密码不能为空")
    private String  password;

    @Schema(description = "验证码的uuid,在/auth/captcha获取")
    @NotBlank(message = "请填写验证码uuid")
    private String  captchaUUID;

    @Schema(description = "验证码，前往/auth/captcha获取")
    @NotBlank(message = "请填写验证码")
    private String  captchaCode;
}