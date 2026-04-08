package com.rauio.smartdangjian.server.user.pojo.vo;

import com.rauio.smartdangjian.server.user.utils.spec.UserStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "用户公开信息，不包含邮箱、手机等敏感数据")
public class UserPublicVO {

    @Schema(description = "用户ID")
    private String id;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "真实姓名")
    private String realName;

    @Schema(description = "党员编号")
    private String partyMemberId;

    @Schema(description = "党员状态")
    private UserStatus partyStatus;

    @Schema(description = "党支部名称")
    private String branchName;
}
