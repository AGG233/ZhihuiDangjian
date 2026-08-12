package com.rauio.smartdangjian.server.content.comment.pojo.response;

import java.time.LocalDateTime;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "评论响应")
public class CommentResponse {

    @Schema(description = "评论ID")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    @Schema(description = "评论用户ID")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long userId;

    @Schema(description = "评论目标类型: course=课程, article=文章")
    private String targetType;

    @Schema(description = "评论目标ID（课程ID或文章ID）")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long targetId;

    @Schema(description = "评论内容")
    private String content;

    @Schema(description = "父评论ID")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long parentId;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
}
