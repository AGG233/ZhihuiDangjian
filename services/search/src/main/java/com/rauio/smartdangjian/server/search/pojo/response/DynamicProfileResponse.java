package com.rauio.smartdangjian.server.search.pojo.response;

import java.time.LocalDate;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "动态画像视图对象")
public class DynamicProfileResponse {

    @Schema(description = "近期热点标签（近30天学习章节标题频次Top3）")
    private List<HotTag> hotTags;

    @Schema(description = "成长趋势（近8周按周聚合学习时长与答题正确率）")
    private List<GrowthTrend> growthTrend;

    @Schema(description = "薄弱知识域（正确率低于50%的难度/章节）")
    private List<WeakDomain> weakDomains;

    @Data
    @Builder
    @Schema(description = "热点标签")
    public static class HotTag {
        @Schema(description = "标签内容（章节标题）", example = "习近平新时代中国特色社会主义思想")
        private String tag;

        @Schema(description = "出现次数", example = "5")
        private Long count;
    }

    @Data
    @Builder
    @Schema(description = "成长趋势（周维度）")
    public static class GrowthTrend {
        @Schema(description = "周起始日期", example = "2026-08-10")
        private LocalDate weekStart;

        @Schema(description = "本周学习时长（秒）", example = "3600")
        private Integer studyDuration;

        @Schema(description = "本周答题正确率", example = "0.8")
        private Double quizAccuracy;
    }

    @Data
    @Builder
    @Schema(description = "薄弱知识域")
    public static class WeakDomain {
        @Schema(description = "薄弱域类型：DIFFICULTY表示按难度，CHAPTER表示按章节", example = "DIFFICULTY")
        private String type;

        @Schema(description = "薄弱域名称（难度值或章节标题）", example = "hard")
        private String name;

        @Schema(description = "正确率", example = "0.33")
        private Double accuracy;
    }
}
