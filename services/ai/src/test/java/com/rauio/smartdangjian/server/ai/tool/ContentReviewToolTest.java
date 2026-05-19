package com.rauio.smartdangjian.server.ai.tool;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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
import com.rauio.smartdangjian.server.content.pojo.vo.ChapterVO;
import com.rauio.smartdangjian.server.content.pojo.vo.ContentBlockVO;
import com.rauio.smartdangjian.server.content.service.ContentBlockService;
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
    private ContentBlockService contentBlockService;

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
                .id("course-1")
                .title("党建课程")
                .description("课程描述")
                .difficulty("easy")
                .build();
        ChapterVO chapter = ChapterVO.builder()
                .id("ch-1")
                .title("第一章")
                .description("章节描述")
                .orderIndex(1)
                .build();
        ContentBlockVO block = new ContentBlockVO();

        when(courseService.getById("course-1")).thenReturn(course);
        when(chapterService.getByCourseId("course-1")).thenReturn(List.of(chapter));
        when(contentBlockService.getByParentId("ch-1")).thenReturn(List.of(block));

        Map<String, Object> result = contentReviewTool.reviewCourseContent("course-1");

        assertThat(result).containsEntry("id", "course-1");
        assertThat(result).containsEntry("title", "党建课程");
        assertThat(result).containsEntry("difficulty", "easy");
        assertThat(result).containsKey("chapters");
    }

    @Test
    @DisplayName("reviewCourseContent 课程不存在时抛出 BusinessException")
    void reviewCourseContentNotFound() {
        when(courseService.getById("nonexistent")).thenReturn(null);

        assertThatThrownBy(() -> contentReviewTool.reviewCourseContent("nonexistent"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("课程不存在");
    }

    @Test
    @DisplayName("reviewQuizQuality 返回题目详情及选项")
    void reviewQuizQuality() {
        Quiz quiz = Quiz.builder()
                .id("quiz-1")
                .question("测试题目")
                .questionType("single_choice")
                .difficulty("easy")
                .score(5)
                .explanation("解析内容")
                .build();
        QuizOption option = QuizOption.builder()
                .id("opt-1")
                .quizId("quiz-1")
                .optionText("选项A")
                .isCorrect(true)
                .build();

        when(quizService.getById("quiz-1")).thenReturn(quiz);
        when(quizOptionService.getByQuizId("quiz-1")).thenReturn(List.of(option));

        Map<String, Object> result = contentReviewTool.reviewQuizQuality("quiz-1");

        assertThat(result).containsEntry("question", "测试题目");
        assertThat(result).containsEntry("questionType", "single_choice");
        assertThat(result).containsEntry("difficulty", "easy");
        assertThat(result).containsKey("options");
    }

    @Test
    @DisplayName("reviewQuizQuality 题目不存在时抛出 BusinessException")
    void reviewQuizQualityNotFound() {
        when(quizService.getById("nonexistent")).thenReturn(null);

        assertThatThrownBy(() -> contentReviewTool.reviewQuizQuality("nonexistent"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("题目不存在");
    }
}
