package com.rauio.smartdangjian.server.search.pojo.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "学习情况汇总视图对象")
public class LearningSummaryResponse {

    @Schema(description = "理论维度：课程学习时长与完成率")
    private TheoryDimension theory;

    @Schema(description = "政策理解维度：章节测试平均正确率")
    private PolicyDimension policyComprehension;

    @Data
    @Builder
    @Schema(description = "理论维度")
    public static class TheoryDimension {
        @Schema(description = "课程学习总时长（秒）", example = "3600")
        private Integer totalDuration;

        @Schema(description = "章节完成率", example = "0.8")
        private Double completionRate;
    }

    @Data
    @Builder
    @Schema(description = "政策理解维度")
    public static class PolicyDimension {
        @Schema(description = "章节测试平均正确率", example = "0.85")
        private Double avgCorrectRate;

        @Schema(description = "总答题数", example = "50")
        private Integer totalAnswers;
    }
}
