package com.rauio.smartdangjian.server.learning.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.rauio.smartdangjian.server.learning.pojo.dto.LearningRecordDto;
import com.rauio.smartdangjian.server.learning.pojo.entity.UserLearningRecord;
import com.rauio.smartdangjian.server.learning.service.UserLearningRecordService;

@ExtendWith(MockitoExtension.class)
@DisplayName("LearningQueryFacadeImpl")
class LearningQueryFacadeImplTest {

    @Mock
    private UserLearningRecordService userLearningRecordService;

    @InjectMocks
    private LearningQueryFacadeImpl facade;

    private UserLearningRecord sampleRecord() {
        return UserLearningRecord.builder()
                .id(1L)
                .userId(100L)
                .chapterId(200L)
                .duration(1800)
                .deviceType("web")
                .createdAt(LocalDateTime.of(2026, 1, 1, 10, 0))
                .build();
    }

    @Nested
    @DisplayName("getRecentLearningRecords 方法")
    class GetRecentLearningRecords {

        @Test
        @DisplayName("调用 service 的 getRecentByUserId 并转换返回 DTO 列表")
        void delegatesAndConverts() {
            List<UserLearningRecord> records = List.of(sampleRecord());
            when(userLearningRecordService.getRecentByUserId("100", 7)).thenReturn(records);

            List<LearningRecordDto> result = facade.getRecentLearningRecords(100L, 7);

            assertThat(result).hasSize(1);
            LearningRecordDto dto = result.get(0);
            assertThat(dto.getId()).isEqualTo(1L);
            assertThat(dto.getUserId()).isEqualTo(100L);
            assertThat(dto.getChapterId()).isEqualTo(200L);
            assertThat(dto.getDuration()).isEqualTo(1800);
            assertThat(dto.getDeviceType()).isEqualTo("web");
            assertThat(dto.getCreatedAt()).isEqualTo(LocalDateTime.of(2026, 1, 1, 10, 0));
        }

        @Test
        @DisplayName("service 返回空列表时返回空列表")
        void emptyRecordsReturnsEmptyList() {
            when(userLearningRecordService.getRecentByUserId("100", 7)).thenReturn(Collections.emptyList());

            List<LearningRecordDto> result = facade.getRecentLearningRecords(100L, 7);

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("userId 为 null 返回空列表")
        void nullUserIdReturnsEmptyList() {
            List<LearningRecordDto> result = facade.getRecentLearningRecords(null, 7);

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("getByUserIdAndCourseId 方法")
    class GetByUserIdAndCourseId {

        @Test
        @DisplayName("调用 service 的 getByUserIdAndCourseId 并转换返回 DTO 列表")
        void delegatesAndConverts() {
            List<UserLearningRecord> records = List.of(sampleRecord());
            when(userLearningRecordService.getByUserIdAndCourseId(100L, 300L)).thenReturn(records);

            List<LearningRecordDto> result = facade.getByUserIdAndCourseId(100L, 300L);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("service 返回空列表时返回空列表")
        void emptyRecordsReturnsEmptyList() {
            when(userLearningRecordService.getByUserIdAndCourseId(100L, 300L)).thenReturn(Collections.emptyList());

            List<LearningRecordDto> result = facade.getByUserIdAndCourseId(100L, 300L);

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("userId 为 null 返回空列表")
        void nullUserIdReturnsEmptyList() {
            List<LearningRecordDto> result = facade.getByUserIdAndCourseId(null, 300L);

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("courseId 为 null 返回空列表")
        void nullCourseIdReturnsEmptyList() {
            List<LearningRecordDto> result = facade.getByUserIdAndCourseId(100L, null);

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("getByUserIdAndCourseIdAndChapterId 方法")
    class GetByUserIdAndCourseIdAndChapterId {

        @Test
        @DisplayName("调用 service 的 getByUserIdAndCourseIdAndChapterId 并转换返回 DTO 列表")
        void delegatesAndConverts() {
            List<UserLearningRecord> records = List.of(sampleRecord());
            when(userLearningRecordService.getByUserIdAndCourseIdAndChapterId(100L, 300L, 200L))
                    .thenReturn(records);

            List<LearningRecordDto> result = facade.getByUserIdAndCourseIdAndChapterId(100L, 300L, 200L);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("service 返回空列表时返回空列表")
        void emptyRecordsReturnsEmptyList() {
            when(userLearningRecordService.getByUserIdAndCourseIdAndChapterId(100L, 300L, 200L))
                    .thenReturn(Collections.emptyList());

            List<LearningRecordDto> result = facade.getByUserIdAndCourseIdAndChapterId(100L, 300L, 200L);

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("userId 为 null 返回空列表")
        void nullUserIdReturnsEmptyList() {
            List<LearningRecordDto> result = facade.getByUserIdAndCourseIdAndChapterId(null, 300L, 200L);

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("courseId 为 null 返回空列表")
        void nullCourseIdReturnsEmptyList() {
            List<LearningRecordDto> result = facade.getByUserIdAndCourseIdAndChapterId(100L, null, 200L);

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("chapterId 为 null 返回空列表")
        void nullChapterIdReturnsEmptyList() {
            List<LearningRecordDto> result = facade.getByUserIdAndCourseIdAndChapterId(100L, 300L, null);

            assertThat(result).isEmpty();
        }
    }
}
