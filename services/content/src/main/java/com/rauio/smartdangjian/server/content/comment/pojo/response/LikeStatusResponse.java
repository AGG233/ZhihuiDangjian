package com.rauio.smartdangjian.server.content.comment.pojo.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "点赞状态响应")
public class LikeStatusResponse {

    @Schema(description = "当前用户是否已点赞")
    private Boolean liked;

    @Schema(description = "点赞总数")
    private Long count;
}
