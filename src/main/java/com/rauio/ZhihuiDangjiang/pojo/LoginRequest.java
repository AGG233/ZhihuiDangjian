package com.rauio.ZhihuiDangjiang.pojo;

import com.rauio.ZhihuiDangjiang.annotation.validation.AtLeastOneNoBlank;
import lombok.Data;

@Data
@AtLeastOneNoBlank(
        fields = {"phone", "username", "email", "party_member_id"},
        message = "用户相关信息为空"
)
public class LoginRequest {
    private String  phone;
    private String  username;
    private String  email;
    private String  party_member_id;
    private String  password;
    private String  captchaID;
    private String  code;
}
