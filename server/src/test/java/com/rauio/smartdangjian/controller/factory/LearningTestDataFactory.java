package com.rauio.smartdangjian.controller.factory;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.rauio.smartdangjian.server.learning.pojo.request.UserChapterProgressRequest;
import com.rauio.smartdangjian.server.learning.pojo.request.UserLearningRecordRequest;
import com.rauio.smartdangjian.server.learning.pojo.response.UserChapterProgressResponse;
import com.rauio.smartdangjian.server.learning.pojo.response.UserLearningRecordResponse;

/**
 * Static factory for learning module test data — produces UserChapterProgressRequest,
 * UserChapterProgressResponse, UserLearningRecordRequest, UserLearningRecordResponse, and JSON helpers.
 */
public final class LearningTestDataFactory {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper().registerModule(new JavaTimeModule());

    private LearningTestDataFactory() {}

    // ── UserChapterProgressRequest ─────────────────────────────────────

    public static UserChapterProgressRequest createChapterProgressDto() {
        return UserChapterProgressRequest.builder()
                .userId("user-001")
                .chapterId("ch-001")
                .progress(50)
                .status("in_progress")
                .firstViewedAt(LocalDateTime.now())
                .build();
    }

    public static UserChapterProgressRequest createChapterProgressDto(String userId, String chapterId) {
        return UserChapterProgressRequest.builder()
                .userId(userId)
                .chapterId(chapterId)
                .progress(75)
                .status("in_progress")
                .firstViewedAt(LocalDateTime.now())
                .build();
    }

    public static UserChapterProgressRequest createChapterProgressUpdateDto(String id) {
        return UserChapterProgressRequest.builder()
                .id(id)
                .progress(100)
                .status("completed")
                .build();
    }

    // ── UserChapterProgressResponse ──────────────────────────────────────

    public static UserChapterProgressResponse createChapterProgressVO(String id) {
        return UserChapterProgressResponse.builder()
                .id(id)
                .userId("user-001")
                .chapterId("ch-001")
                .progress(50)
                .status("in_progress")
                .firstViewedAt(new Date())
                .build();
    }

    public static UserChapterProgressResponse createChapterProgressVO(String id, String userId, String chapterId) {
        return UserChapterProgressResponse.builder()
                .id(id)
                .userId(userId)
                .chapterId(chapterId)
                .progress(75)
                .status("in_progress")
                .firstViewedAt(new Date())
                .build();
    }

    // ── UserLearningRecordRequest ──────────────────────────────────────

    public static UserLearningRecordRequest createLearningRecordDto() {
        return UserLearningRecordRequest.builder()
                .userId("user-001")
                .chapterId("ch-001")
                .startTime(LocalDateTime.now().minusHours(1))
                .endTime(LocalDateTime.now())
                .duration(3600)
                .deviceType("web")
                .build();
    }

    public static UserLearningRecordRequest createLearningRecordDto(String userId, String chapterId) {
        return UserLearningRecordRequest.builder()
                .userId(userId)
                .chapterId(chapterId)
                .startTime(LocalDateTime.now().minusMinutes(30))
                .endTime(LocalDateTime.now())
                .duration(1800)
                .deviceType("mobile")
                .build();
    }

    public static UserLearningRecordRequest createLearningRecordUpdateDto(String id) {
        return UserLearningRecordRequest.builder()
                .id(id)
                .duration(7200)
                .deviceType("tablet")
                .build();
    }

    // ── UserLearningRecordResponse ───────────────────────────────────────

    public static UserLearningRecordResponse createLearningRecordVO(String id) {
        return UserLearningRecordResponse.builder()
                .id(id)
                .userId("user-001")
                .chapterId("ch-001")
                .startTime(new Date())
                .endTime(new Date())
                .duration(3600)
                .deviceType("web")
                .createdAt(new Date())
                .build();
    }

    public static UserLearningRecordResponse createLearningRecordVO(String id, String userId, String chapterId) {
        return UserLearningRecordResponse.builder()
                .id(id)
                .userId(userId)
                .chapterId(chapterId)
                .startTime(new Date())
                .endTime(new Date())
                .duration(1800)
                .deviceType("mobile")
                .createdAt(new Date())
                .build();
    }

    // ── JSON helpers ───────────────────────────────────────────────

    public static String toJson(Object obj) {
        try {
            return OBJECT_MAPPER.writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize to JSON", e);
        }
    }

    public static String listToJson(List<?> list) {
        try {
            return OBJECT_MAPPER.writeValueAsString(list);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize list to JSON", e);
        }
    }
}
