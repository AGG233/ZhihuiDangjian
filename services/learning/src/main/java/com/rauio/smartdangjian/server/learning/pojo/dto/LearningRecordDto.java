package com.rauio.smartdangjian.server.learning.pojo.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 学习记录摘要 —— 不暴露 UserLearningRecord 实体。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LearningRecordDto {
    private Long id;
    private Long userId;
    private Long chapterId;
    private Integer duration;
    private String deviceType;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private LocalDateTime createdAt;
}
