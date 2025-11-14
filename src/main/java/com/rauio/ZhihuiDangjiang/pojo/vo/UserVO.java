package com.rauio.ZhihuiDangjiang.pojo.vo;

import com.rauio.ZhihuiDangjiang.annotation.validation.Sensitive;
import com.rauio.ZhihuiDangjiang.utils.Spec.UserStatus;
import lombok.Data;

@Data
public class UserVO {

    private String id;
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
