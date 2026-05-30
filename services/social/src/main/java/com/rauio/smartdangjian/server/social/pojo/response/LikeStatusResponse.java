package com.rauio.smartdangjian.server.social.pojo.response;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "点赞状态响应体")
public class LikeStatusResponse {

    @Schema(description = "是否已点赞")
    private boolean liked;

    @Schema(description = "点赞总数")
    private Integer likeCount;

    @Schema(description = "目标类型")
    private String targetType;

    @JsonSerialize(using = ToStringSerializer.class)
    @Schema(description = "目标ID")
    private Long targetId;
}
