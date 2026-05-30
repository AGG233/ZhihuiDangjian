package com.rauio.smartdangjian.server.search.pojo.response;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "学习趋势")
public class LearningTrendResponse {
    @Schema(description = "统计天数")
    private int days;

    @Schema(description = "总学习次数")
    private int totalCount;

    @Schema(description = "日均学习次数")
    private double avgDailyCount;

    @Schema(description = "每日学习数据")
    private List<DailyCount> dailyData;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "每日学习统计")
    public static class DailyCount {
        @Schema(description = "日期", example = "2026-05-30")
        private String date;

        @Schema(description = "学习次数")
        private Integer count;
    }
}
