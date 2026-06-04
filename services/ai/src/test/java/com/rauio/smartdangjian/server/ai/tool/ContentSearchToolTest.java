package com.rauio.smartdangjian.server.ai.tool;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rauio.smartdangjian.exception.BusinessException;
import com.rauio.smartdangjian.server.content.api.ArticleQueryFacade;
import com.rauio.smartdangjian.server.chapter.api.ChapterQueryFacade;
import com.rauio.smartdangjian.server.course.api.CourseQueryFacade;
import com.rauio.smartdangjian.server.content.api.dto.ArticleSummary;
import com.rauio.smartdangjian.server.chapter.api.dto.ChapterSummary;
import com.rauio.smartdangjian.server.chapter.pojo.response.ChapterResponse;
import com.rauio.smartdangjian.server.content.pojo.response.ContentBlockResponse;
import com.rauio.smartdangjian.server.course.pojo.response.CourseResponse;
import com.rauio.smartdangjian.server.content.service.ChapterContentBlockService;

@ExtendWith(MockitoExtension.class)
class ContentSearchToolTest {

    @Mock
    private CourseQueryFacade courseQueryFacade;

    @Mock
    private ArticleQueryFacade articleQueryFacade;

    @Mock
    private ChapterQueryFacade chapterQueryFacade;

    @Mock
    private ChapterContentBlockService chapterContentBlockService;

    @InjectMocks
    private ContentSearchTool contentSearchTool;

    @Nested
    @DisplayName("searchCourses 方法")
    class SearchCoursesTest {

        @Test
        @DisplayName("使用 like 查询匹配标题并返回映射列表")
        void returnsMappedResults() {
            CourseResponse course1 = CourseResponse.builder()
                    .id(1L)
                    .title("Java Basics")
                    .description("Intro to Java")
                    .build();
            CourseResponse course2 = CourseResponse.builder()
                    .id(2L)
                    .title("Advanced Java")
                    .description("Deep dive")
                    .build();

            Page<CourseResponse> page = new Page<>(1, 1000);
            page.setRecords(List.of(course1, course2));
            when(courseQueryFacade.searchPublishedCourses("Java", null, null, 1, 1000))
                    .thenReturn(page);

            List<Map<String, Object>> result = contentSearchTool.searchCourses("Java");

            assertThat(result).hasSize(2);
            assertThat(result.get(0))
                    .containsEntry("id", 1L)
                    .containsEntry("title", "Java Basics")
                    .containsEntry("description", "Intro to Java");
            assertThat(result.get(1))
                    .containsEntry("id", 2L)
                    .containsEntry("title", "Advanced Java")
                    .containsEntry("description", "Deep dive");
            verify(courseQueryFacade, times(1)).searchPublishedCourses("Java", null, null, 1, 1000);
        }

        @Test
        @DisplayName("无匹配结果时返回空列表")
        void returnsEmptyListWhenNoMatch() {
            Page<CourseResponse> page = new Page<>(1, 1000);
            page.setRecords(Collections.emptyList());
            when(courseQueryFacade.searchPublishedCourses("NonExistent", null, null, 1, 1000))
                    .thenReturn(page);

            List<Map<String, Object>> result = contentSearchTool.searchCourses("NonExistent");

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("searchArticles 方法")
    class SearchArticlesTest {

        @Test
        @DisplayName("使用 like 查询匹配标题并返回映射列表")
        void returnsMappedResults() {
            ArticleSummary article1 = ArticleSummary.builder()
                    .id(1L)
                    .title("Article 1")
                    .summary("Summary 1")
                    .build();
            ArticleSummary article2 = ArticleSummary.builder()
                    .id(2L)
                    .title("Article 2")
                    .summary("Summary 2")
                    .build();

            when(articleQueryFacade.searchByKeyword("Article")).thenReturn(List.of(article1, article2));

            List<Map<String, Object>> result = contentSearchTool.searchArticles("Article");

            assertThat(result).hasSize(2);
            assertThat(result.get(0))
                    .containsEntry("id", 1L)
                    .containsEntry("title", "Article 1")
                    .containsEntry("description", "Summary 1");
            assertThat(result.get(1))
                    .containsEntry("id", 2L)
                    .containsEntry("title", "Article 2")
                    .containsEntry("description", "Summary 2");
            verify(articleQueryFacade, times(1)).searchByKeyword("Article");
        }

        @Test
        @DisplayName("无匹配结果时返回空列表")
        void returnsEmptyListWhenNoMatch() {
            when(articleQueryFacade.searchByKeyword("NonExistent")).thenReturn(Collections.emptyList());

            List<Map<String, Object>> result = contentSearchTool.searchArticles("NonExistent");

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("searchChapters 方法")
    class SearchChaptersTest {

        @Test
        @DisplayName("使用 like 查询匹配标题并返回映射列表")
        void returnsMappedResults() {
            ChapterSummary chapter1 = ChapterSummary.builder()
                    .id(1L)
                    .title("Chapter 1")
                    .description("Desc 1")
                    .build();
            ChapterSummary chapter2 = ChapterSummary.builder()
                    .id(2L)
                    .title("Chapter 2")
                    .description("Desc 2")
                    .build();

            when(chapterQueryFacade.searchByTitle("Chapter")).thenReturn(List.of(chapter1, chapter2));

            List<Map<String, Object>> result = contentSearchTool.searchChapters("Chapter");

            assertThat(result).hasSize(2);
            assertThat(result.get(0))
                    .containsEntry("id", 1L)
                    .containsEntry("title", "Chapter 1")
                    .containsEntry("description", "Desc 1");
            assertThat(result.get(1))
                    .containsEntry("id", 2L)
                    .containsEntry("title", "Chapter 2")
                    .containsEntry("description", "Desc 2");
            verify(chapterQueryFacade, times(1)).searchByTitle("Chapter");
        }

        @Test
        @DisplayName("无匹配结果时返回空列表")
        void returnsEmptyListWhenNoMatch() {
            when(chapterQueryFacade.searchByTitle("NonExistent")).thenReturn(Collections.emptyList());

            List<Map<String, Object>> result = contentSearchTool.searchChapters("NonExistent");

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("getCourseDetail 方法")
    class GetCourseDetailTest {

        @Test
        @DisplayName("返回课程详情及其章节列表")
        void returnsCourseDetailWithChapters() {
            CourseResponse course = CourseResponse.builder()
                    .id(1L)
                    .title("Course")
                    .description("Description")
                    .difficulty("easy")
                    .build();
            ChapterResponse chapter = ChapterResponse.builder()
                    .id(10L)
                    .title("Chapter 1")
                    .description("Chapter desc")
                    .orderIndex(1)
                    .build();

            when(courseQueryFacade.get(1L)).thenReturn(course);
            when(chapterQueryFacade.getByCourseId(1L)).thenReturn(List.of(chapter));

            Map<String, Object> result = contentSearchTool.getCourseDetail("1");

            assertThat(result)
                    .containsEntry("id", 1L)
                    .containsEntry("title", "Course")
                    .containsEntry("description", "Description")
                    .containsEntry("difficulty", "easy");
            assertThat(result).containsKey("chapters");

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> chapters = (List<Map<String, Object>>) result.get("chapters");
            assertThat(chapters).hasSize(1);
            assertThat(chapters.get(0))
                    .containsEntry("id", 10L)
                    .containsEntry("title", "Chapter 1")
                    .containsEntry("description", "Chapter desc")
                    .containsEntry("orderIndex", 1);
            verify(chapterQueryFacade, times(1)).getByCourseId(1L);
        }

        @Test
        @DisplayName("课程不存在时抛出 BusinessException")
        void throwsWhenCourseNotFound() {
            assertThatThrownBy(() -> contentSearchTool.getCourseDetail("999"))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("课程不存在");
        }
    }

    @Nested
    @DisplayName("getChapterDetail 方法")
    class GetChapterDetailTest {

        @Test
        @DisplayName("返回章节详情及其内容块")
        void returnsChapterDetailWithBlocks() {
            ChapterResponse chapter = ChapterResponse.builder()
                    .id(1L)
                    .title("Chapter")
                    .description("Desc")
                    .courseId(10L)
                    .orderIndex(1)
                    .build();
            ContentBlockResponse block = new ContentBlockResponse();

            when(chapterQueryFacade.get(1L)).thenReturn(chapter);
            when(chapterContentBlockService.getByChapterId(1L)).thenReturn(List.of(block));

            Map<String, Object> result = contentSearchTool.getChapterDetail("1");

            assertThat(result)
                    .containsEntry("id", 1L)
                    .containsEntry("title", "Chapter")
                    .containsEntry("description", "Desc")
                    .containsEntry("courseId", 10L)
                    .containsEntry("orderIndex", 1);
            assertThat(result).containsKey("contentBlocks");

            @SuppressWarnings("unchecked")
            List<ContentBlockResponse> blocks = (List<ContentBlockResponse>) result.get("contentBlocks");
            assertThat(blocks).hasSize(1);
            verify(chapterContentBlockService, times(1)).getByChapterId(1L);
        }

        @Test
        @DisplayName("章节不存在时抛出 BusinessException")
        void throwsWhenChapterNotFound() {
            assertThatThrownBy(() -> contentSearchTool.getChapterDetail("999"))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("章节不存在");
        }
    }
}
