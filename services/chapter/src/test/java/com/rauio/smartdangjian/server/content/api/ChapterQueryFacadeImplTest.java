package com.rauio.smartdangjian.server.content.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.rauio.smartdangjian.exception.BusinessException;
import com.rauio.smartdangjian.server.content.api.dto.ChapterSummary;
import com.rauio.smartdangjian.server.content.pojo.entity.Chapter;
import com.rauio.smartdangjian.server.content.pojo.response.ChapterResponse;
import com.rauio.smartdangjian.server.content.service.chapter.ChapterService;

@ExtendWith(MockitoExtension.class)
@DisplayName("ChapterQueryFacadeImpl")
class ChapterQueryFacadeImplTest {

    @Mock
    private ChapterService chapterService;

    @InjectMocks
    private ChapterQueryFacadeImpl facade;

    private ChapterResponse sampleResponse(int index) {
        return ChapterResponse.builder()
                .id((long) index)
                .courseId(10L)
                .title("第" + index + "章")
                .description("描述" + index)
                .orderIndex(index)
                .build();
    }

    @Nested
    @DisplayName("get 方法")
    class Get {

        @Test
        @DisplayName("存在章节返回 ChapterResponse")
        void existingReturnsResponse() {
            when(chapterService.get(1L)).thenReturn(sampleResponse(1));

            ChapterResponse result = facade.get(1L);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getCourseId()).isEqualTo(10L);
            assertThat(result.getTitle()).isEqualTo("第1章");
        }

        @Test
        @DisplayName("章节不存在传播异常")
        void notFoundPropagatesException() {
            when(chapterService.get(99L)).thenThrow(new BusinessException(3101, "章节不存在"));

            assertThatThrownBy(() -> facade.get(99L))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("章节不存在");
        }
    }

    @Nested
    @DisplayName("委托方法")
    class DelegationMethods {

        @Test
        @DisplayName("getByCourseId 委托 chapterService")
        void getByCourseIdDelegates() {
            when(chapterService.getByCourseId(10L)).thenReturn(List.of(sampleResponse(1), sampleResponse(2)));

            List<ChapterResponse> result = facade.getByCourseId(10L);

            assertThat(result).hasSize(2);
        }

        @Test
        @DisplayName("listCourseIdsByChapterIds 委托 chapterService")
        void listCourseIdsByChapterIdsDelegates() {
            when(chapterService.listCourseIdsByChapterIds(List.of(1L, 2L))).thenReturn(List.of(10L));

            List<Long> result = facade.listCourseIdsByChapterIds(List.of(1L, 2L));

            assertThat(result).containsExactly(10L);
        }

        @Test
        @DisplayName("getCourseIdMapByChapterIds 委托 chapterService")
        void getCourseIdMapByChapterIdsDelegates() {
            when(chapterService.getCourseIdMapByChapterIds(List.of(1L, 2L))).thenReturn(Map.of(1L, 10L, 2L, 10L));

            Map<Long, Long> result = facade.getCourseIdMapByChapterIds(List.of(1L, 2L));

            assertThat(result).hasSize(2);
            assertThat(result.get(1L)).isEqualTo(10L);
        }
    }

    @Nested
    @DisplayName("searchByTitle 方法")
    class SearchByTitle {

        @Test
        @DisplayName("关键词匹配返回对应章节摘要")
        void matchingKeywordsReturnsSummaries() {
            Chapter ch1 = Chapter.builder()
                    .id(1L)
                    .courseId(10L)
                    .title("第一章")
                    .description("基础")
                    .orderIndex(1)
                    .build();
            Chapter ch2 = Chapter.builder()
                    .id(2L)
                    .courseId(10L)
                    .title("第二章")
                    .description("进阶")
                    .orderIndex(2)
                    .build();

            when(chapterService.searchByTitle("章")).thenReturn(List.of(ch1, ch2));

            List<ChapterSummary> result = facade.searchByTitle("章");

            assertThat(result).hasSize(2);
            assertThat(result.get(0).getId()).isEqualTo(1L);
            assertThat(result.get(0).getTitle()).isEqualTo("第一章");
            assertThat(result.get(0).getCourseId()).isEqualTo(10L);
            assertThat(result.get(1).getId()).isEqualTo(2L);
            assertThat(result.get(1).getTitle()).isEqualTo("第二章");
        }

        @Test
        @DisplayName("无匹配返回空列表")
        void noMatchReturnsEmpty() {
            when(chapterService.searchByTitle("不存在")).thenReturn(List.of());

            List<ChapterSummary> result = facade.searchByTitle("不存在");

            assertThat(result).isEmpty();
        }
    }
}
