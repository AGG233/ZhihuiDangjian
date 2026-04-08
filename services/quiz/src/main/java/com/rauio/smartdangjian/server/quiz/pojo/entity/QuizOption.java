package com.rauio.smartdangjian.server.quiz.pojo.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@TableName("quiz_option")
@Schema(description = "测验选项")
public class QuizOption {

    @TableId
    @JsonSerialize(using = ToStringSerializer.class)
    @Schema(description = "选项ID")
    private String id;

    @Schema(description = "题目ID")
    private String quizId;

    @Schema(description = "选项内容")
    private String optionText;

    @Schema(description = "是否正确选项")
    private Boolean isCorrect;

    @Schema(description = "选项排序序号")
    private String orderIndex;

}
