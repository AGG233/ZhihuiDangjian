package com.rauio.smartdangjian.server.social.pojo.entity;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@Builder
@TableName(value = "user_like", autoResultMap = true)
@Schema(description = "用户点赞")
public class UserLike extends Model<UserLike> {

    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "点赞ID")
    private Long id;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "目标类型：comment/article/course")
    private String targetType;

    @Schema(description = "目标ID")
    private Long targetId;

    @TableField(fill = FieldFill.INSERT)
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
}
