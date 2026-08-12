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
@TableName("like_record")
@Schema(description = "点赞记录")
public class LikeRecord {

    @TableId
    @Schema(description = "点赞记录ID")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    @Schema(description = "点赞用户ID")
    private Long userId;

    @Schema(description = "点赞目标类型: course=课程, article=文章")
    private String targetType;

    @Schema(description = "点赞目标ID（课程ID或文章ID）")
    private Long targetId;

    @TableField(fill = FieldFill.INSERT)
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
}
