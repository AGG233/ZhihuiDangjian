package com.rauio.smartdangjian.server.learning.pojo.dto;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "用户行为请求体")
public class UserBehaviorDto {
    @Schema(description = "用户ID")
    private String userId;
    @Schema(description = "章节ID")
    private String chapterId;
}
