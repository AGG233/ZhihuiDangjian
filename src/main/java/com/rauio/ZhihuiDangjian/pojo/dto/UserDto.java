package com.rauio.ZhihuiDangjian.pojo.dto;

import com.rauio.ZhihuiDangjian.annotation.validation.Sensitive;
import com.rauio.ZhihuiDangjian.utils.Spec.UserStatus;
import com.rauio.ZhihuiDangjian.utils.Spec.UserType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserDto {

    @Schema(description = "用户ID，留空即可")
    private String  userId;

    @Schema(description = "用户名")
    private String  username;

    @Schema(description = "真实姓名")
    private String  realName;

    @Schema(description = "密码")
    private String  password;

    @Schema(description = "党员ID")
    private String  party_member_id;

    @Schema(description = "党员状态")
    private UserStatus party_status;

    @Schema(description = "党支部名称")
    private String  branch_name;

    @Schema(description = "用户类型")
    private UserType user_type;

    @Schema(description = "学校ID")
    private String  university_id;

    @Schema(description = "入党时间")
    private LocalDateTime    joinPartyDate;

    @Sensitive(type = Sensitive.SensitiveType.EMAIL)
    @Schema(description = "邮箱")
    private String  email;


    @Sensitive(type = Sensitive.SensitiveType.PHONE)
    @Schema(description = "手机号")
    private String  phone;
}
