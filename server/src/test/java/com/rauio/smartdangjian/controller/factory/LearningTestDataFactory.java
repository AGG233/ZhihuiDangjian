package com.rauio.smartdangjian.controller.factory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.rauio.smartdangjian.server.learning.pojo.dto.UserChapterProgressDto;
import com.rauio.smartdangjian.server.learning.pojo.dto.UserLearningRecordDto;
import com.rauio.smartdangjian.server.learning.pojo.vo.UserChapterProgressVO;
import com.rauio.smartdangjian.server.learning.pojo.vo.UserLearningRecordVO;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

/**
 * Static factory for learning module test data — produces UserChapterProgressDto,
 * UserChapterProgressVO, UserLearningRecordDto, UserLearningRecordVO, and JSON helpers.
 */
public final class LearningTestDataFactory {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    private LearningTestDataFactory() {
    }

    // ── UserChapterProgressDto ─────────────────────────────────────

    public static UserChapterProgressDto createChapterProgressDto() {
        return UserChapterProgressDto.builder()
                .userId("user-001")
                .chapterId("ch-001")
                .progress(50)
                .status("in_progress")
                .firstViewedAt(LocalDateTime.now())
                .build();
    }

    public static UserChapterProgressDto createChapterProgressDto(String userId, String chapterId) {
        return UserChapterProgressDto.builder()
                .userId(userId)
                .chapterId(chapterId)
                .progress(75)
                .status("in_progress")
                .firstViewedAt(LocalDateTime.now())
                .build();
    }

    public static UserChapterProgressDto createChapterProgressUpdateDto(String id) {
        return UserChapterProgressDto.builder()
                .id(id)
                .progress(100)
                .status("completed")
                .build();
    }

    // ── UserChapterProgressVO ──────────────────────────────────────

    public static UserChapterProgressVO createChapterProgressVO(String id) {
        return UserChapterProgressVO.builder()
                .id(id)
                .userId("user-001")
                .chapterId("ch-001")
                .progress(50)
                .status("in_progress")
                .firstViewedAt(new Date())
                .build();
    }

    public static UserChapterProgressVO createChapterProgressVO(String id, String userId, String chapterId) {
        return UserChapterProgressVO.builder()
                .id(id)
                .userId(userId)
                .chapterId(chapterId)
                .progress(75)
                .status("in_progress")
                .firstViewedAt(new Date())
                .build();
    }

    // ── UserLearningRecordDto ──────────────────────────────────────

    public static UserLearningRecordDto createLearningRecordDto() {
        return UserLearningRecordDto.builder()
                .userId("user-001")
                .chapterId("ch-001")
                .startTime(LocalDateTime.now().minusHours(1))
                .endTime(LocalDateTime.now())
                .duration(3600)
                .deviceType("web")
                .build();
    }

    public static UserLearningRecordDto createLearningRecordDto(String userId, String chapterId) {
        return UserLearningRecordDto.builder()
                .userId(userId)
                .chapterId(chapterId)
                .startTime(LocalDateTime.now().minusMinutes(30))
                .endTime(LocalDateTime.now())
                .duration(1800)
                .deviceType("mobile")
                .build();
    }

    public static UserLearningRecordDto createLearningRecordUpdateDto(String id) {
        return UserLearningRecordDto.builder()
                .id(id)
                .duration(7200)
                .deviceType("tablet")
                .build();
    }

    // ── UserLearningRecordVO ───────────────────────────────────────

    public static UserLearningRecordVO createLearningRecordVO(String id) {
        return UserLearningRecordVO.builder()
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

    public static UserLearningRecordVO createLearningRecordVO(String id, String userId, String chapterId) {
        return UserLearningRecordVO.builder()
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
