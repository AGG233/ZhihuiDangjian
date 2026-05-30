package com.rauio.smartdangjian.server.social.pojo.response;

import java.time.LocalDateTime;
import java.util.List;

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
@Schema(description = "评论响应体")
public class CommentResponse {

    @JsonSerialize(using = ToStringSerializer.class)
    @Schema(description = "评论ID")
    private Long id;

    @Schema(description = "目标类型")
    private String targetType;

    @JsonSerialize(using = ToStringSerializer.class)
    @Schema(description = "目标ID")
    private Long targetId;

    @JsonSerialize(using = ToStringSerializer.class)
    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "真实姓名")
    private String realName;

    @JsonSerialize(using = ToStringSerializer.class)
    @Schema(description = "父评论ID")
    private Long parentId;

    @Schema(description = "评论内容")
    private String content;

    @Schema(description = "状态")
    private String status;

    @Schema(description = "点赞数")
    private Integer likeCount;

    @Schema(description = "回复数")
    private Integer replyCount;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    @Schema(description = "子回复列表")
    private List<CommentResponse> replies;
}
