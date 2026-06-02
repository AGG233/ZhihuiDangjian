package com.rauio.smartdangjian.server.quiz.controller.user;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rauio.smartdangjian.pojo.response.Result;
import com.rauio.smartdangjian.security.RoleConstants;
import com.rauio.smartdangjian.server.quiz.pojo.response.QuizOptionResponse;
import com.rauio.smartdangjian.server.quiz.pojo.response.QuizResponse;
import com.rauio.smartdangjian.server.quiz.service.QuizOptionService;
import com.rauio.smartdangjian.server.quiz.service.QuizService;

import cn.dev33.satoken.annotation.SaCheckRole;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "用户试题接口", description = "用户查看试题和选项")
@RestController
@RequestMapping("/api/quiz/quizzes")
@RequiredArgsConstructor
@SaCheckRole(RoleConstants.STUDENT)
public class UserQuizController {

    private final QuizService quizService;
    private final QuizOptionService quizOptionService;

    @Operation(summary = "获取试题详情", description = "根据试题ID获取试题详情")
    @GetMapping("/{id}")
    public Result<QuizResponse> getQuiz(@Parameter(name = "id", description = "试题ID") @PathVariable Long id) {
        return Result.ok(QuizResponse.from(quizService.get(id)));
    }

    @Operation(summary = "获取章节下所有试题", description = "根据章节ID获取该章节下的所有试题列表")
    @GetMapping("/by-chapter/{chapterId}")
    public Result<List<QuizResponse>> getQuizOfChapter(
            @Parameter(name = "chapterId", description = "章节ID") @PathVariable Long chapterId) {
        List<QuizResponse> responses = quizService.getByChapterId(chapterId).stream()
                .map(QuizResponse::from)
                .toList();
        return Result.ok(responses);
    }

    @Operation(summary = "获取试题选项列表", description = "根据试题ID获取该试题的所有选项")
    @GetMapping("/{id}/options")
    public Result<List<QuizOptionResponse>> getQuizOption(
            @Parameter(name = "id", description = "试题ID") @PathVariable Long id) {
        List<QuizOptionResponse> responses = quizOptionService.getByQuizId(id).stream()
                .map(QuizOptionResponse::from)
                .toList();
        return Result.ok(responses);
    }

    @Operation(summary = "获取单个选项详情", description = "根据选项ID获取选项详情，学生未答题时隐藏正确答案")
    @GetMapping("/{id}/options/{optionId}")
    public Result<QuizOptionResponse> getByOptionId(
            @Parameter(name = "optionId", description = "选项ID") @PathVariable Long optionId) {
        return Result.ok(QuizOptionResponse.from(quizOptionService.get(optionId)));
    }
}
