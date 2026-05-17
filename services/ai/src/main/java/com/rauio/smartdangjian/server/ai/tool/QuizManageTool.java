package com.rauio.smartdangjian.server.ai.tool;

import com.rauio.smartdangjian.exception.BusinessException;
import com.rauio.smartdangjian.server.quiz.pojo.entity.Quiz;
import com.rauio.smartdangjian.server.quiz.pojo.entity.QuizOption;
import com.rauio.smartdangjian.server.quiz.service.QuizOptionService;
import com.rauio.smartdangjian.server.quiz.service.QuizService;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.rauio.smartdangjian.constants.ErrorConstants.RESOURCE_NOT_EXISTS;

@Component
@RequiredArgsConstructor
public class QuizManageTool {

    private final QuizService quizService;
    private final QuizOptionService quizOptionService;

    @Tool(name = "getQuiz", description = "根据测验ID获取测验详情及其选项")
    public Quiz getQuiz(@ToolParam(description = "测验ID") String quizId) {
        Quiz quiz = quizService.get(quizId);
        if (quiz == null) {
            throw new BusinessException(RESOURCE_NOT_EXISTS, "测验不存在");
        }
        return quiz;
    }

    @Tool(name = "createQuiz", description = "创建新的测验题目及其选项")
    public Boolean createQuiz(
            @ToolParam(description = "章节ID") String chapterId,
            @ToolParam(description = "题目内容") String question,
            @ToolParam(description = "题目类型：single_choice / multiple_choice / true_false") String questionType,
            @ToolParam(description = "分值") Integer score,
            @ToolParam(description = "难度：easy / medium / hard") String difficulty,
            @ToolParam(description = "题目解析") String explanation,
            @ToolParam(description = "选项列表，每个选项包含 optionText / isCorrect / orderIndex") List<Map<String, Object>> options
    ) {
        Quiz quiz = Quiz.builder()
                .chapterId(chapterId)
                .question(question)
                .questionType(questionType)
                .score(score)
                .difficulty(difficulty)
                .explanation(explanation)
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        Boolean saved = quizService.create(quiz);
        if (!Boolean.TRUE.equals(saved) || quiz.getId() == null) {
            throw new BusinessException(RESOURCE_NOT_EXISTS, "测验创建失败");
        }

        if (options != null) {
            for (Map<String, Object> opt : options) {
                Object optionText = opt.get("optionText");
                Object isCorrect = opt.get("isCorrect");
                Object orderIndex = opt.get("orderIndex");
                QuizOption option = QuizOption.builder()
                        .quizId(quiz.getId())
                        .optionText(optionText != null ? optionText.toString() : null)
                        .isCorrect(isCorrect instanceof Boolean ? (Boolean) isCorrect : null)
                        .orderIndex(orderIndex != null ? orderIndex.toString() : null)
                        .build();
                Boolean optionSaved = quizOptionService.create(quiz.getId(), option);
                if (!Boolean.TRUE.equals(optionSaved)) {
                    throw new BusinessException(RESOURCE_NOT_EXISTS, "选项创建失败");
                }
            }
        }

        return true;
    }

    @Tool(name = "updateQuiz", description = "更新测验题目信息")
    public Boolean updateQuiz(
            @ToolParam(description = "测验ID") String quizId,
            @ToolParam(description = "题目内容，可为空") String question,
            @ToolParam(description = "分值，可为空") Integer score,
            @ToolParam(description = "难度，可为空") String difficulty,
            @ToolParam(description = "解析，可为空") String explanation,
            @ToolParam(description = "是否启用，可为空") Boolean isActive
    ) {
        Quiz existing = quizService.get(quizId);
        if (existing == null) {
            throw new BusinessException(RESOURCE_NOT_EXISTS, "测验不存在");
        }
        if (question != null) {
            existing.setQuestion(question);
        }
        if (score != null) {
            existing.setScore(score);
        }
        if (difficulty != null) {
            existing.setDifficulty(difficulty);
        }
        if (explanation != null) {
            existing.setExplanation(explanation);
        }
        if (isActive != null) {
            existing.setIsActive(isActive);
        }
        existing.setUpdatedAt(LocalDateTime.now());
        return Boolean.TRUE.equals(quizService.update(existing));
    }

    @Tool(name = "deleteQuiz", description = "删除指定测验及其选项")
    public Boolean deleteQuiz(@ToolParam(description = "测验ID") String quizId) {
        Quiz existing = quizService.get(quizId);
        if (existing == null) {
            throw new BusinessException(RESOURCE_NOT_EXISTS, "测验不存在");
        }
        List<QuizOption> options = quizOptionService.getByQuizId(quizId);
        if (options != null) {
            for (QuizOption option : options) {
                quizOptionService.delete(option.getId());
            }
        }
        return Boolean.TRUE.equals(quizService.delete(quizId));
    }

    @Tool(name = "searchQuizzesByChapter", description = "根据章节ID搜索该章节下的所有测验")
    public List<Quiz> searchQuizzesByChapter(@ToolParam(description = "章节ID") String chapterId) {
        return quizService.getByChapterId(chapterId);
    }
}
