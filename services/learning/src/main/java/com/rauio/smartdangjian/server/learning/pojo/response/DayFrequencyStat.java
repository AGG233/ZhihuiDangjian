package com.rauio.smartdangjian.server.learning.pojo.response;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonFormat;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "每日碎片化学习频率统计")
public class DayFrequencyStat {

    @Schema(description = "日期（日粒度）")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate date;

    @Schema(description = "学习次数")
    private Long recordCount;

    @Schema(description = "学习总时长（秒）")
    private Long totalDuration;
}
