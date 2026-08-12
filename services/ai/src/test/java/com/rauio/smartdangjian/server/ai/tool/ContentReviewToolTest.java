package com.rauio.smartdangjian.server.ai.tool;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.rauio.smartdangjian.exception.BusinessException;
import com.rauio.smartdangjian.server.content.pojo.entity.Course;
import com.rauio.smartdangjian.server.content.pojo.response.ChapterResponse;
import com.rauio.smartdangjian.server.content.pojo.response.ContentBlockResponse;
import com.rauio.smartdangjian.server.content.service.ChapterContentBlockService;
import com.rauio.smartdangjian.server.content.service.chapter.ChapterService;
import com.rauio.smartdangjian.server.content.service.course.CourseService;
import com.rauio.smartdangjian.server.quiz.pojo.entity.Quiz;
import com.rauio.smartdangjian.server.quiz.pojo.entity.QuizOption;
import com.rauio.smartdangjian.server.quiz.service.QuizOptionService;
import com.rauio.smartdangjian.server.quiz.service.QuizService;

@ExtendWith(MockitoExtension.class)
class ContentReviewToolTest {

    @Mock
    private CourseService courseService;

    @Mock
    private ChapterService chapterService;

    @Mock
    private ChapterContentBlockService contentBlockService;

    @Mock
    private QuizService quizService;

    @Mock
    private QuizOptionService quizOptionService;

    @InjectMocks
    private ContentReviewTool contentReviewTool;

    @Test
    @DisplayName("reviewCourseContent 返回课程完整内容")
    void reviewCourseContent() {
        Course course = Course.builder()
                .id(1L)
                .title("党建课程")
                .description("课程描述")
                .difficulty("easy")
                .build();
        ChapterResponse chapter = ChapterResponse.builder()
                .id(1L)
                .title("第一章")
                .description("章节描述")
                .orderIndex(1)
                .build();
        ContentBlockResponse block = new ContentBlockResponse();

        when(courseService.getById(any())).thenReturn(course);
        when(chapterService.getByCourseId(1L)).thenReturn(List.of(chapter));
        when(contentBlockService.getByChapterId(1L)).thenReturn(List.of(block));

        Map<String, Object> result = contentReviewTool.reviewCourseContent("1");

        assertThat(result).containsEntry("id", 1L);
        assertThat(result).containsEntry("title", "党建课程");
        assertThat(result).containsEntry("difficulty", "easy");
        assertThat(result).containsKey("chapters");
    }

    @Test
    @DisplayName("reviewCourseContent 课程不存在时抛出 BusinessException")
    void reviewCourseContentNotFound() {
        when(courseService.getById(any())).thenReturn(null);

        assertThatThrownBy(() -> contentReviewTool.reviewCourseContent("1"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("课程不存在");
    }

    @Test
    @DisplayName("reviewQuizQuality 返回题目详情及选项")
    void reviewQuizQuality() {
        Quiz quiz = Quiz.builder()
                .id(1L)
                .question("测试题目")
                .questionType("single_choice")
                .difficulty("easy")
                .score(5)
                .explanation("解析内容")
                .build();
        QuizOption option = QuizOption.builder()
                .id(1L)
                .quizId(1L)
                .optionText("选项A")
                .isCorrect(true)
                .build();

        when(quizService.getById(any())).thenReturn(quiz);
        when(quizOptionService.getByQuizId(1L)).thenReturn(List.of(option));

        Map<String, Object> result = contentReviewTool.reviewQuizQuality("1");

        assertThat(result).containsEntry("question", "测试题目");
        assertThat(result).containsEntry("questionType", "single_choice");
        assertThat(result).containsEntry("difficulty", "easy");
        assertThat(result).containsKey("options");
    }

    @Test
    @DisplayName("reviewQuizQuality 题目不存在时抛出 BusinessException")
    void reviewQuizQualityNotFound() {
        when(quizService.getById(any())).thenReturn(null);

        assertThatThrownBy(() -> contentReviewTool.reviewQuizQuality("1"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("题目不存在");
    }
}
