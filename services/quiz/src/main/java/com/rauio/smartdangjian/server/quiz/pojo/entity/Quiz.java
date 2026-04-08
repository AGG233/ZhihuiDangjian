package com.rauio.smartdangjian.server.quiz.pojo.entity;

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
@TableName("quiz")
@Schema(description = "测验题目")
public class Quiz {

    @TableId
    @JsonSerialize(using = ToStringSerializer.class)
    @Schema(description = "题目ID")
    private String id;

    @Schema(description = "章节ID")
    private String chapterId;

    @Schema(description = "题目内容")
    private String question;

    @Schema(description = "题目类型", example = "single_choice")
    private String questionType;

    @Schema(description = "题目分值", example = "5")
    private Integer score;

    @Schema(description = "难度", example = "medium")
    private String difficulty;

    @Schema(description = "题目解析")
    private String explanation;

    @Schema(description = "是否启用")
    private Boolean isActive;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;

}
