package com.rauio.smartdangjian.server.ai.tool;

import static com.rauio.smartdangjian.constants.ErrorConstants.RESOURCE_NOT_EXISTS;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import com.rauio.smartdangjian.common.utils.IdUtil;
import com.rauio.smartdangjian.exception.BusinessException;
import com.rauio.smartdangjian.server.chapter.api.ChapterQueryFacade;
import com.rauio.smartdangjian.server.chapter.pojo.response.ChapterResponse;
import com.rauio.smartdangjian.server.content.api.ContentQueryFacade;
import com.rauio.smartdangjian.server.content.api.dto.ContentBlockSummary;
import com.rauio.smartdangjian.server.course.api.CourseQueryFacade;
import com.rauio.smartdangjian.server.course.pojo.response.CourseResponse;
import com.rauio.smartdangjian.server.quiz.api.QuizDataFacade;
import com.rauio.smartdangjian.server.quiz.pojo.dto.QuizOptionReviewDto;
import com.rauio.smartdangjian.server.quiz.pojo.dto.QuizSummary;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ContentReviewTool {

    private final CourseQueryFacade courseQueryFacade;
    private final ChapterQueryFacade chapterQueryFacade;
    private final ContentQueryFacade contentQueryFacade;
    private final QuizDataFacade quizDataFacade;

    @Tool(name = "reviewCourseContent", description = "获取课程完整内容（含章节和内容块）用于审查")
    public Map<String, Object> reviewCourseContent(@ToolParam(description = "课程ID") String courseId) {
        CourseResponse course = courseQueryFacade.get(IdUtil.parse(courseId));
        if (course == null) {
            throw new BusinessException(RESOURCE_NOT_EXISTS, "课程不存在");
        }
        List<ChapterResponse> chapters = chapterQueryFacade.getByCourseId(IdUtil.parse(courseId));
        List<Map<String, Object>> chapterData = chapters.stream()
                .map(ch -> {
                    Map<String, Object> chMap = new HashMap<>();
                    chMap.put("id", ch.getId());
                    chMap.put("title", ch.getTitle());
                    chMap.put("description", ch.getDescription());
                    chMap.put("orderIndex", ch.getOrderIndex());
                    List<ContentBlockSummary> blocks = contentQueryFacade.getByChapterId(ch.getId());
                    chMap.put("contentBlocks", blocks);
                    return chMap;
                })
                .collect(Collectors.toList());

        Map<String, Object> result = new HashMap<>();
        result.put("id", course.getId());
        result.put("title", course.getTitle());
        result.put("description", course.getDescription());
        result.put("difficulty", course.getDifficulty());
        result.put("chapters", chapterData);
        return result;
    }

    @Tool(name = "reviewQuizQuality", description = "获取题目详情（含选项）用于审查题目质量")
    public Map<String, Object> reviewQuizQuality(@ToolParam(description = "题目ID") String quizId) {
        QuizSummary quiz = quizDataFacade.getQuiz(IdUtil.parse(quizId));
        if (quiz == null) {
            throw new BusinessException(RESOURCE_NOT_EXISTS, "题目不存在");
        }
        List<QuizOptionReviewDto> options = quizDataFacade.getOptionsByQuizIdForReview(IdUtil.parse(quizId));

        Map<String, Object> result = new HashMap<>();
        result.put("id", quiz.getId());
        result.put("question", quiz.getQuestion());
        result.put("questionType", quiz.getQuestionType());
        result.put("difficulty", quiz.getDifficulty());
        result.put("score", quiz.getScore());
        result.put("explanation", quiz.getExplanation());
        result.put("options", options);
        return result;
    }
}
