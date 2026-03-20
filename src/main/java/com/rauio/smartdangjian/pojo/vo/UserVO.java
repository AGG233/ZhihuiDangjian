package com.rauio.smartdangjian.pojo.vo;

import com.rauio.smartdangjian.annotation.validation.Sensitive;
import com.rauio.smartdangjian.utils.spec.UserStatus;
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
