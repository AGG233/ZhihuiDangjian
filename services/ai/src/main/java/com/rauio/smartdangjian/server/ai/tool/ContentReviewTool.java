package com.rauio.smartdangjian.server.ai.tool;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
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
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.rauio.smartdangjian.constants.ErrorConstants.RESOURCE_NOT_EXISTS;

@Component
@RequiredArgsConstructor
public class ContentReviewTool {

    private final CourseService courseService;
    private final ChapterService chapterService;
    private final ContentBlockService contentBlockService;
    private final QuizService quizService;
    private final QuizOptionService quizOptionService;

    @Tool(name = "reviewCourseContent", description = "获取课程完整内容（含章节和内容块）用于审查")
    public Map<String, Object> reviewCourseContent(@ToolParam(description = "课程ID") String courseId) {
        Course course = courseService.getById(courseId);
        if (course == null) {
            throw new BusinessException(RESOURCE_NOT_EXISTS, "课程不存在");
        }
        List<ChapterVO> chapters = chapterService.getByCourseId(courseId);
        List<Map<String, Object>> chapterData = chapters.stream().map(ch -> {
            Map<String, Object> chMap = new HashMap<>();
            chMap.put("id", ch.getId());
            chMap.put("title", ch.getTitle());
            chMap.put("description", ch.getDescription());
            chMap.put("orderIndex", ch.getOrderIndex());
            List<ContentBlockVO> blocks = contentBlockService.getByParentId(ch.getId());
            chMap.put("contentBlocks", blocks);
            return chMap;
        }).collect(Collectors.toList());

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
        Quiz quiz = quizService.getById(quizId);
        if (quiz == null) {
            throw new BusinessException(RESOURCE_NOT_EXISTS, "题目不存在");
        }
        List<QuizOption> options = quizOptionService.getByQuizId(quizId);

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
