package com.rauio.smartdangjian.search.pojo.vo;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@Builder
public class UserProfileVO {

    private String userId;
    private LearningStats learning;
    private KnowledgeStats knowledge;
    private List<String> interestCategoryIds;
    private QuizStats quiz;

    @Data
    @Builder
    public static class LearningStats {
        private int totalDuration;
        private double avgDuration;
        private int totalRecords;
        private int completedChapters;
        private String preferredDevice;
    }

    @Data
    @Builder
    public static class KnowledgeStats {
        private double avgProgress;
        private double completionRate;
        private List<String> weakChapterIds;
    }

    @Data
    @Builder
    public static class QuizStats {
        private int totalAnswers;
        private int correctCount;
        private double correctRate;
        private double avgTimeSpent;
        private Map<String, Double> byDifficulty;
    }
}
