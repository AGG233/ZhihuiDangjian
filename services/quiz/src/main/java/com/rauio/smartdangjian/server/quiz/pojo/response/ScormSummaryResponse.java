package com.rauio.smartdangjian.server.quiz.pojo.response;

import java.math.BigDecimal;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * SCORM 学习汇总响应。
 *
 * <p>按学习包聚合某用户的注册数、已完成数（lesson_status=completed）与平均分。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "SCORM学习汇总响应")
public class ScormSummaryResponse {

    @JsonSerialize(using = ToStringSerializer.class)
    @Schema(description = "学习包ID")
    private Long packageId;

    @Schema(description = "课程标题")
    private String title;

    @Schema(description = "注册（SCO）数量", example = "3")
    private Integer registrationCount;

    @Schema(description = "已完成数量（lesson_status=completed）", example = "2")
    private Integer completedCount;

    @Schema(description = "平均分", example = "82.50")
    private BigDecimal avgScore;
}
