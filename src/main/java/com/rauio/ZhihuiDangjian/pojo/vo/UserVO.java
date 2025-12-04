package com.rauio.ZhihuiDangjian.pojo.vo;

import com.rauio.ZhihuiDangjian.annotation.validation.Sensitive;
import com.rauio.ZhihuiDangjian.utils.Spec.UserStatus;
import lombok.Data;

@Data
public class UserVO {

    private Long id;
    private String username;
    private String realName;
    private String party_member_id;
    private UserStatus party_status;
    private String branch_name;
    @Sensitive(type = Sensitive.SensitiveType.EMAIL)
    private String email;
    @Sensitive(type = Sensitive.SensitiveType.PHONE)
    private String phone;
}