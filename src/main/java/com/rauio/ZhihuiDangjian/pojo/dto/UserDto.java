package com.rauio.ZhihuiDangjian.pojo.dto;

import com.rauio.ZhihuiDangjian.pojo.User;
import com.rauio.ZhihuiDangjian.utils.Spec.UserStatus;
import com.rauio.ZhihuiDangjian.utils.Spec.UserType;
import com.rauio.ZhihuiDangjian.annotation.validation.Sensitive;
import lombok.Data;

@Data
public class UserDto {
    private String  userId;
    private String  username;
    private String  realName;
    private String  party_member_id;
    private UserStatus party_status;
    private String  branch_name;
    private UserType user_type;
    @Sensitive(type = Sensitive.SensitiveType.EMAIL)
    private String  email;
    @Sensitive(type = Sensitive.SensitiveType.PHONE)
    private String  phone;

    public UserDto(User user) {
        this.userId = user.getId();
        this.username = user.getUsername();
        this.realName = user.getRealName();
        this.party_member_id = String.valueOf(user.getPartyMemberId());
        this.party_status = user.getPartyStatus();
        this.branch_name = user.getBranchName();
        this.user_type = user.getUserType();
        this.email = user.getEmail();
        this.phone = user.getPhone();
    }
}
