package com.rauio.smartdangjian.server.quiz.controller.user;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rauio.smartdangjian.aop.annotation.PermissionAccess;
import com.rauio.smartdangjian.pojo.response.Result;
import com.rauio.smartdangjian.server.quiz.pojo.entity.Quiz;
import com.rauio.smartdangjian.server.quiz.pojo.entity.QuizOption;
import com.rauio.smartdangjian.server.quiz.pojo.response.QuizOptionResponse;
import com.rauio.smartdangjian.server.quiz.pojo.response.QuizResponse;
import com.rauio.smartdangjian.server.quiz.service.QuizOptionService;
import com.rauio.smartdangjian.server.quiz.service.QuizService;
import com.rauio.smartdangjian.utils.spec.UserType;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "用户试题接口", description = "用户查看试题和选项")
@RestController
@RequestMapping("/api/quiz/quizzes")
@RequiredArgsConstructor
@PermissionAccess(UserType.STUDENT)
public class UserQuizController {

    private final QuizService quizService;
    private final QuizOptionService quizOptionService;

    @Operation(summary = "获取试题详情", description = "根据试题ID获取试题详情")
    @GetMapping("/{id}")
    public Result<QuizResponse> getQuiz(@Parameter(name = "id", description = "试题ID") @PathVariable String id) {
        return Result.ok(toQuizResponse(quizService.get(id)));
    }

    @Operation(summary = "获取章节下所有试题", description = "根据章节ID获取该章节下的所有试题列表")
    @GetMapping("/by-chapter/{chapterId}")
    public Result<List<QuizResponse>> getQuizOfChapter(
            @Parameter(name = "chapterId", description = "章节ID") @PathVariable String chapterId) {
        List<QuizResponse> responses = quizService.getByChapterId(chapterId).stream()
                .map(this::toQuizResponse)
                .toList();
        return Result.ok(responses);
    }

    @Operation(summary = "获取试题选项列表", description = "根据试题ID获取该试题的所有选项")
    @GetMapping("/{id}/options")
    public Result<List<QuizOptionResponse>> getQuizOption(
            @Parameter(name = "id", description = "试题ID") @PathVariable String id) {
        List<QuizOptionResponse> responses = quizOptionService.getByQuizId(id).stream()
                .map(this::toQuizOptionResponse)
                .toList();
        return Result.ok(responses);
    }

    @Operation(summary = "获取单个选项详情", description = "根据选项ID获取选项详情，学生未答题时隐藏正确答案")
    @GetMapping("/{id}/options/{optionId}")
    public Result<QuizOptionResponse> getByOptionId(
            @Parameter(name = "optionId", description = "选项ID") @PathVariable String optionId) {
        return Result.ok(toQuizOptionResponse(quizOptionService.get(optionId)));
    }

    private QuizResponse toQuizResponse(Quiz quiz) {
        if (quiz == null) {
            return null;
        }
        return QuizResponse.builder()
                .id(quiz.getId())
                .chapterId(quiz.getChapterId())
                .question(quiz.getQuestion())
                .questionType(quiz.getQuestionType())
                .score(quiz.getScore())
                .difficulty(quiz.getDifficulty())
                .explanation(quiz.getExplanation())
                .isActive(quiz.getIsActive())
                .createdAt(quiz.getCreatedAt())
                .updatedAt(quiz.getUpdatedAt())
                .build();
    }

    private QuizOptionResponse toQuizOptionResponse(QuizOption option) {
        if (option == null) {
            return null;
        }
        return QuizOptionResponse.builder()
                .id(option.getId())
                .quizId(option.getQuizId())
                .optionText(option.getOptionText())
                .orderIndex(option.getOrderIndex())
                .build();
    }
}
