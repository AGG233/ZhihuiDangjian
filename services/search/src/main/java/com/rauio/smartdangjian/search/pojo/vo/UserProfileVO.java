package com.rauio.smartdangjian.search.pojo.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@Builder
@Schema(description = "用户画像视图对象")
public class UserProfileVO {

    @Schema(description = "用户ID")
    private String userId;
    @Schema(description = "学习统计信息")
    private LearningStats learning;
    @Schema(description = "知识掌握统计信息")
    private KnowledgeStats knowledge;
    @Schema(description = "兴趣分类ID列表")
    private List<String> interestCategoryIds;
    @Schema(description = "答题统计信息")
    private QuizStats quiz;

    @Data
    @Builder
    @Schema(description = "学习统计")
    public static class LearningStats {
        @Schema(description = "学习总时长（秒）", example = "3600")
        private int totalDuration;
        @Schema(description = "平均学习时长（秒）", example = "600")
        private double avgDuration;
        @Schema(description = "学习记录总数", example = "12")
        private int totalRecords;
        @Schema(description = "已完成章节数", example = "8")
        private int completedChapters;
        @Schema(description = "偏好设备类型", example = "web")
        private String preferredDevice;
    }

    @Data
    @Builder
    @Schema(description = "知识统计")
    public static class KnowledgeStats {
        @Schema(description = "平均学习进度", example = "76.5")
        private double avgProgress;
        @Schema(description = "章节完成率", example = "0.8")
        private double completionRate;
        @Schema(description = "薄弱章节ID列表")
        private List<String> weakChapterIds;
    }

    @Data
    @Builder
    @Schema(description = "答题统计")
    public static class QuizStats {
        @Schema(description = "总答题数", example = "50")
        private int totalAnswers;
        @Schema(description = "答对题目数", example = "42")
        private int correctCount;
        @Schema(description = "正确率", example = "0.84")
        private double correctRate;
        @Schema(description = "平均用时（秒）", example = "45.6")
        private double avgTimeSpent;
        @Schema(description = "按难度统计的正确率")
        private Map<String, Double> byDifficulty;
    }
}
