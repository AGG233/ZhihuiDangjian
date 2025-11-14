package com.rauio.ZhihuiDangjiang.pojo.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {
    private String  passport;
    private String  password;
    private String  captchaUUID;
    private String  captchaCode;
}