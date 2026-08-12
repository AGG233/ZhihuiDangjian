package com.rauio.smartdangjian.server.content.comment.pojo.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "发表评论请求")
public class CommentCreateRequest {

    @Schema(description = "评论目标类型: course=课程, article=文章", requiredMode = Schema.RequiredMode.REQUIRED)
    private String targetType;

    @Schema(description = "评论目标ID（课程ID或文章ID）", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long targetId;

    @Schema(description = "评论内容", requiredMode = Schema.RequiredMode.REQUIRED)
    private String content;

    @Schema(description = "父评论ID，回复时使用，可为空")
    private Long parentId;
}
