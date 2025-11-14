package com.rauio.ZhihuiDangjian.pojo.request;

import com.rauio.ZhihuiDangjian.annotation.validation.IsIdCard;
import com.rauio.ZhihuiDangjian.utils.Spec.UserStatus;
import com.rauio.ZhihuiDangjian.annotation.validation.IsPassword;
import com.rauio.ZhihuiDangjian.annotation.validation.IsPhone;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class RegisterRequest {

    private String type;

    @NotBlank(message = "用户名不能为空")
    @Size(min = 2, max = 16, message = "用户名长度必须在2-16个字符之间")
    private String username;

    @Size(min = 8, max = 20, message = "密码长度必须在8-20个字符之间")
    @IsPassword
    private String password;

    @NotBlank(message = "真实姓名不能为空")
    @Size(min = 2, max = 16, message = "真实姓名长度必须在2-50个字符之间")
    private String real_name;

    @NotBlank(message = "身份证号码不能为空")
    @Size(min = 10,max = 18, message = "身份证不合法")
    @IsIdCard
    private String id_card;

    @NotBlank(message = "党员编号不能为空")
    @Size(min = 20, max = 20, message = "党员编号长度必须为20")
    private String party_member_id;

    @NotNull(message = "党员状态不能为空")
    private UserStatus party_status;

    @NotBlank(message = "分支名称不能为空")
    @Size(min = 2, max = 100, message = "分支名称长度必须在2-100个字符之间")
    private String branch_name;

    @NotBlank(message = "邮箱不能为空")
    @Email
    private String email;

    @NotBlank(message = "手机号不能为空")
    @IsPhone
    private String phone;

    @NotBlank(message = "验证码不能为空")
    private String captchaUUID;

    @NotBlank(message = "验证码不能为空")
    private String captchaCode;

    @NotBlank(message = "学校名称不能为空")
    private String universityName;
}
