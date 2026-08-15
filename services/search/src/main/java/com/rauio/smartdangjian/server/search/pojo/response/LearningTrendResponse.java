package com.rauio.smartdangjian.server.search.pojo.response;

import java.time.LocalDate;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "学习趋势视图对象")
public class LearningTrendResponse {

    @Schema(description = "日期")
    private LocalDate date;

    @Schema(description = "学习人次（当日学习记录条数）")
    private Integer learningCount;

    @Schema(description = "学习总时长（秒）")
    private Integer totalDuration;
}
