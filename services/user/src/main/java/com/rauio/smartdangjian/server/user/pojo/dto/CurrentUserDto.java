package com.rauio.smartdangjian.server.user.pojo.dto;

import com.rauio.smartdangjian.utils.spec.UserType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "当前登录用户信息")
public class CurrentUserDto {
    @Schema(description = "当前用户ID")
    private String id;
    @Schema(description = "当前用户类型")
    private UserType userType;
    @Schema(description = "当前用户所属学校ID")
    private String universityId;
}
