package com.rauio.smartdangjian.server.user.pojo.dto;

import com.rauio.smartdangjian.annotation.validation.Sensitive;
import com.rauio.smartdangjian.server.user.utils.spec.AccountStatus;
import com.rauio.smartdangjian.server.user.utils.spec.PartyStatus;
import com.rauio.smartdangjian.utils.spec.UserType;
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
    private String partyMemberId;

    @Schema(description = "政治面貌：正式党员、预备党员、发展对象、积极分子、群众")
    private PartyStatus partyStatus;

    @Schema(description = "党支部名称")
    private String branchName;

    @Schema(description = "用户类型：学生、学校、管理员")
    private UserType userType;

    @Schema(description = "账号状态：active表示正常，inactive表示未激活，banned表示封禁")
    private AccountStatus status;

    @Schema(description = "学校ID")
    private String universityId;

    @Schema(description = "入党时间")
    private LocalDateTime  joinPartyDate;

    @Sensitive(type = Sensitive.SensitiveType.EMAIL)
    @Schema(description = "邮箱")
    private String  email;


    @Sensitive(type = Sensitive.SensitiveType.PHONE)
    @Schema(description = "手机号")
    private String  phone;

    @Sensitive(type = Sensitive.SensitiveType.ID_CARD)
    @Schema(description = "身份证号")
    private String idCard;
}
