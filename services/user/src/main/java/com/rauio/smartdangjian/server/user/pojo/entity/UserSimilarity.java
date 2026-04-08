package com.rauio.smartdangjian.server.user.pojo.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
@TableName("user_similarity")
@Schema(description = "用户相似度")
public class UserSimilarity {

    @TableId
    @JsonSerialize(using = ToStringSerializer.class)
    @Schema(description = "记录ID")
    private String id;

    @Schema(description = "用户1 ID")
    private String userId1;

    @Schema(description = "用户2 ID")
    private String userId2;

    @Schema(description = "相似度分值", example = "0.82")
    private double similarityScore;

    @Schema(description = "相似度类型", example = "learning_behavior")
    private String similarityType;

    @Schema(description = "计算参数")
    private String calculationParams;

    @Schema(description = "数据版本")
    private String dataVersion;

    @Schema(description = "计算时间")
    private LocalDateTime calculatedAt;

    @Schema(description = "是否有效")
    private Boolean isValid;

    @Schema(description = "过期时间")
    private LocalDateTime expiresAt;
}
