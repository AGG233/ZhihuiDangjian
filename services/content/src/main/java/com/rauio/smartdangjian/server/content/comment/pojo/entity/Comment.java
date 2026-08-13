package com.rauio.smartdangjian.server.content.comment.pojo.entity;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;

@Data
@Builder
@ToString
@TableName("comment")
@Schema(description = "评论")
public class Comment {

    @TableId
    @Schema(description = "评论ID")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    @Schema(description = "评论用户ID")
    private Long userId;

    @Schema(description = "评论目标类型: course=课程, article=文章")
    private String targetType;

    @Schema(description = "评论目标ID（课程ID或文章ID）")
    private Long targetId;

    @Schema(description = "评论内容")
    private String content;

    @Schema(description = "父评论ID，回复时使用，根评论为空")
    private Long parentId;

    @Schema(description = "状态: 1=正常, 0=已删除/屏蔽")
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;
}
