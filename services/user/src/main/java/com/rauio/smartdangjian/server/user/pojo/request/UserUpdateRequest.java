package com.rauio.smartdangjian.server.user.pojo.request;

import java.time.LocalDateTime;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

import com.rauio.smartdangjian.server.user.utils.spec.PartyStatus;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "用户资料更新请求体")
public class UserUpdateRequest {

    @Schema(description = "真实姓名")
    @Size(max = 50, message = "真实姓名长度不能超过50个字符")
    private String realName;

    @Schema(description = "党员编号")
    @Size(max = 30, message = "党员编号长度不能超过30个字符")
    private String partyMemberId;

    @Schema(description = "政治面貌：正式党员、预备党员、发展对象、积极分子、群众")
    private PartyStatus partyStatus;

    @Schema(description = "党支部名称")
    @Size(max = 100, message = "党支部名称长度不能超过100个字符")
    private String branchName;

    @Schema(description = "入党时间")
    private LocalDateTime joinPartyDate;

    @Schema(description = "邮箱")
    @Email(message = "邮箱格式不正确")
    @Size(max = 100, message = "邮箱长度不能超过100个字符")
    private String email;

    @Schema(description = "手机号")
    @Size(max = 20, message = "手机号长度不能超过20个字符")
    private String phone;
}
