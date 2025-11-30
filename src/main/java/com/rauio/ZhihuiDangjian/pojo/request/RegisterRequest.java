package com.rauio.ZhihuiDangjian.pojo.request;

import com.rauio.ZhihuiDangjian.annotation.validation.IsIdCard;
import com.rauio.ZhihuiDangjian.annotation.validation.IsPassword;
import com.rauio.ZhihuiDangjian.annotation.validation.IsPhone;
import com.rauio.ZhihuiDangjian.utils.Spec.UserStatus;
import com.rauio.ZhihuiDangjian.utils.Spec.UserType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.ToString;

import java.util.Date;

@Data
@ToString
@Schema(description = "注册请求体")
public class RegisterRequest {

    @Schema(description = "用户类型")
    private UserType type;

    @NotBlank(message = "用户名不能为空")
    @Size(min = 2, max = 16, message = "用户名长度必须在2-16个字符之间")
    @Schema(description = "用户名称")
    private String username;

    @Size(min = 8, max = 20, message = "密码长度必须在8-20个字符之间")
    @IsPassword
    @Schema(description = "密码,至少包含一个大写字母，一个特殊符号[.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?]和一个数字，匹配规则：\"^(?=.*[a-zA-Z0-9])(?=.*[!@#$%^&*()_+\\\\-=\\\\[\\\\]{};':\\\"\\\\\\\\|,.<>\\\\/?]).{8,}$\"")
    private String password;

    @NotBlank(message = "真实姓名不能为空")
    @Size(min = 2, max = 16, message = "真实姓名长度必须在2-50个字符之间")
    @Schema(description = "真实姓名")
    private String real_name;

    @NotBlank(message = "身份证号码不能为空")
    @Size(min = 10,max = 18, message = "身份证不合法")
    @IsIdCard
    @Schema(description = "身份证号码")
    private String id_card;

    @Size(min = 20, max = 20, message = "党员编号长度必须为20")
    @Schema(description = "党员编号，如果没有请留空")
    private String party_member_id;

    @NotNull(message = "党员状态不能为空")
    @Schema(description = "党员状态")
    private UserStatus party_status;

    @Size(min = 2, max = 100, message = "分支名称长度必须在2-100个字符之间")
    @Schema(description = "所属党支部名称")
    private String branch_name;

    @Email
    @Schema(description = "邮箱")
    private String email;

    @IsPhone
    @NotBlank(message = "手机号不能为空")
    private String phone;

    @NotBlank(message = "验证码不能为空")
    @Schema(description = "验证码的uuid")
    private String captchaUUID;

    @NotBlank(message = "验证码不能为空")
    @Schema(description = "验证码")
    private String captchaCode;

    @NotBlank(message = "学校ID不能为空")
    @Schema(description = "学校ID")
    private String universityId;

    @Schema(description = "入党时间")
    private Date joinPartyDate;
}
