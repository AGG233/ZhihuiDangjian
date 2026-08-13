package com.rauio.smartdangjian.server.learning.pojo.response;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "碎片化学习频率统计响应")
public class FrequencyStatsResponse {

    @Schema(description = "每日明细列表（仅含存在学习记录的日期）")
    private List<DayFrequencyStat> days;

    @Schema(description = "学习总次数")
    private long totalCount;

    @Schema(description = "学习总时长（秒）")
    private long totalDuration;

    @Schema(description = "日均学习次数（总次数 / 统计窗口天数）")
    private double avgPerDay;
}
