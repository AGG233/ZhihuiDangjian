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

    @Schema(description = "互动表现维度：评论、点赞与活跃度")
    private InteractionDimension interaction;

    @Data
    @Builder
    @Schema(description = "互动表现维度")
    public static class InteractionDimension {
        @Schema(description = "评论数", example = "5")
        private int commentCount;

        @Schema(description = "获赞数（他人对本人评论的点赞）", example = "12")
        private int likeReceivedCount;

        @Schema(description = "点赞数（本人点赞数）", example = "8")
        private int likeGivenCount;

        @Schema(description = "近8周有互动行为的周数", example = "3")
        private int activeWeeks;
    }

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
