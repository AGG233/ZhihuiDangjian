package com.rauio.smartdangjian.server.ai.tool;

import static com.rauio.smartdangjian.constants.ErrorConstants.RESOURCE_NOT_EXISTS;

import java.util.List;
import java.util.Map;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import com.rauio.smartdangjian.common.utils.IdUtil;
import com.rauio.smartdangjian.exception.BusinessException;
import com.rauio.smartdangjian.server.quiz.api.QuizDataFacade;
import com.rauio.smartdangjian.server.quiz.pojo.dto.QuizSummary;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class QuizManageTool {

    private final QuizDataFacade quizDataFacade;

    @Tool(name = "getQuiz", description = "根据测验ID获取测验详情")
    public QuizSummary getQuiz(@ToolParam(description = "测验ID") String quizId) {
        QuizSummary quiz = quizDataFacade.getQuiz(IdUtil.parse(quizId));
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
            @ToolParam(description = "选项列表，每个选项包含 optionText / isCorrect / orderIndex")
                    List<Map<String, Object>> options) {
        Long quizId = quizDataFacade.createQuiz(
                IdUtil.parse(chapterId), question, questionType, score, difficulty, explanation, options);
        if (quizId == null) {
            throw new BusinessException(RESOURCE_NOT_EXISTS, "测验创建失败");
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
            @ToolParam(description = "是否启用，可为空") Boolean isActive) {
        return quizDataFacade.updateQuiz(IdUtil.parse(quizId), question, score, difficulty, explanation, isActive);
    }

    @Tool(name = "deleteQuiz", description = "删除指定测验及其选项")
    public Boolean deleteQuiz(@ToolParam(description = "测验ID") String quizId) {
        return quizDataFacade.deleteQuiz(IdUtil.parse(quizId));
    }

    @Tool(name = "searchQuizzesByChapter", description = "根据章节ID搜索该章节下的所有测验")
    public List<QuizSummary> searchQuizzesByChapter(@ToolParam(description = "章节ID") String chapterId) {
        return quizDataFacade.getQuizzesByChapter(IdUtil.parse(chapterId));
    }
}
