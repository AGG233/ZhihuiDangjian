package com.rauio.smartdangjian.server.learning.pojo.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "用户行为请求体")
public class UserBehaviorDto {
    @Schema(description = "用户ID")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long userId;

    @Schema(description = "章节ID")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long chapterId;
}
