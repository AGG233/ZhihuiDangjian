package com.rauio.smartdangjian.server.quiz.pojo.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "按章节答题准确率聚合行（Mapper 查询结果）")
public class ChapterAccuracyRow {

    @Schema(description = "章节ID")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long chapterId;

    @Schema(description = "该章节答题记录数")
    private Integer questionCount;

    @Schema(description = "该章节答对记录数")
    private Integer correctCount;
}
