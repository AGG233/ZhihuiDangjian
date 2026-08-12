package com.rauio.smartdangjian.server.quiz.pojo.response;

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
@Schema(description = "按章节答题准确率响应")
public class ChapterAccuracyResponse {

    @Schema(description = "章节ID")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long chapterId;

    @Schema(description = "该章节答题记录数（多选题按每个已提交选项计一行）")
    private Integer questionCount;

    @Schema(description = "该章节答对记录数（仅 isCorrect=1 完全正确计答对，部分正确不计）")
    private Integer correctCount;

    @Schema(description = "正确率（correctCount / questionCount，取值 0~1）", example = "0.8")
    private Double accuracy;
}
