package com.rauio.smartdangjian.server.auth.pojo.request;

import com.rauio.smartdangjian.server.user.utils.spec.UserStatus;
import com.rauio.smartdangjian.utils.spec.UserType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "注册请求体")
public class RegisterRequest {

    @Schema(description = "用户类型")
    private UserType type;

    @NotBlank(message = "用户名不能为空")
    @Size(min = 2, max = 16, message = "用户名长度必须在2-16个字符之间")
    @Schema(description = "用户名称")
    private String username;

    @Size(min = 8, max = 20, message = "密码长度必须在8-20个字符之间")
    @Pattern(
            regexp = "^(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?]).{8,20}$",
            message = "密码必须包含大写字母、数字和特殊符号"
    )
    @Schema(description = "密码")
    private String password;

    @NotBlank(message = "真实姓名不能为空")
    @Size(min = 2, max = 16, message = "真实姓名长度必须在2-16个字符之间")
    @Schema(description = "真实姓名")
    private String realName;

    @NotBlank(message = "身份证号码不能为空")
    @Pattern(regexp = "^(\\d{15}|\\d{17}[\\dXx])$", message = "身份证格式错误")
    @Schema(description = "身份证号码")
    private String idCard;

    @Size(min = 20, max = 20, message = "党员编号长度必须为20")
    @Schema(description = "党员编号，如果没有请留空")
    private String partyMemberId;

    @NotNull(message = "党员状态不能为空")
    @Schema(description = "党员状态")
    private UserStatus partyStatus;

    @Size(min = 2, max = 100, message = "分支名称长度必须在2-100个字符之间")
    @Schema(description = "所属党支部名称")
    private String branchName;

    @Email(message = "邮箱格式错误")
    @Schema(description = "邮箱")
    private String email;

    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式错误")
    @Schema(description = "手机号")
    private String phone;

    @NotBlank(message = "验证码UUID不能为空")
    @Schema(description = "验证码的uuid")
    private String captchaUUID;

    @NotBlank(message = "验证码不能为空")
    @Schema(description = "验证码")
    private String captchaCode;

    @NotBlank(message = "学校ID不能为空")
    @Schema(description = "学校ID")
    private String universityId;

    @Schema(description = "入党时间")
    private LocalDateTime joinPartyDate;
}
