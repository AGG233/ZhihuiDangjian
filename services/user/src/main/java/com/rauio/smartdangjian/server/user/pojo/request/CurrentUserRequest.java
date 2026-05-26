package com.rauio.smartdangjian.server.user.pojo.request;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.rauio.smartdangjian.utils.spec.UserType;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CurrentUserRequest {
    @Schema(description = "当前用户ID")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    @Schema(description = "当前用户类型")
    private UserType userType;

    @Schema(description = "当前用户所属学校ID")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long universityId;
}
