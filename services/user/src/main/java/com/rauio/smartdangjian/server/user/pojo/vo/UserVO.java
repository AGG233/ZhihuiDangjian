package com.rauio.smartdangjian.server.user.pojo.vo;

import com.rauio.smartdangjian.annotation.validation.Sensitive;
import com.rauio.smartdangjian.server.user.utils.spec.AccountStatus;
import com.rauio.smartdangjian.server.user.utils.spec.PartyStatus;
import com.rauio.smartdangjian.utils.spec.UserType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "用户完整信息（管理员视角），联系方式经脱敏处理")
public class UserVO {

    @Schema(description = "用户ID")
    private String id;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "真实姓名")
    private String realName;

    @Schema(description = "党员编号")
    private String partyMemberId;

    @Schema(description = "政治面貌")
    private PartyStatus partyStatus;

    @Schema(description = "党支部名称")
    private String branchName;

    @Schema(description = "用户类型")
    private UserType userType;

    @Schema(description = "账号状态：active表示正常，inactive表示未激活，banned表示封禁")
    private AccountStatus status;

    @Schema(description = "学校ID")
    private String universityId;

    @Schema(description = "入党时间")
    private LocalDateTime joinPartyDate;

    @Sensitive(type = Sensitive.SensitiveType.EMAIL)
    @Schema(description = "邮箱（脱敏）")
    private String email;

    @Sensitive(type = Sensitive.SensitiveType.PHONE)
    @Schema(description = "手机号（脱敏）")
    private String phone;
}
