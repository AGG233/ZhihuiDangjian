package com.rauio.smartdangjian.server.quiz.controller.user;

import java.util.List;

import org.springframework.web.bind.annotation.*;

import com.rauio.smartdangjian.pojo.response.Result;
import com.rauio.smartdangjian.security.CurrentUserProvider;
import com.rauio.smartdangjian.server.quiz.pojo.entity.UserQuizAnswer;
import com.rauio.smartdangjian.server.quiz.pojo.response.UserQuizAnswerResponse;
import com.rauio.smartdangjian.server.quiz.service.UserQuizAnswerService;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.annotation.SaCheckRole;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "用户答题记录接口", description = "用户查看和提交答题记录")
@RestController
@RequestMapping("/api/quiz/answers")
@RequiredArgsConstructor
@SaCheckRole("STUDENT")
public class UserQuizAnswerController {

    private final UserQuizAnswerService userQuizAnswerService;
    private final CurrentUserProvider currentUserProvider;

    @Operation(summary = "获取当前用户全部答题记录", description = "获取当前登录用户的所有答题记录")
    @GetMapping("/me")
    @SaCheckPermission("quiz:read")
    public Result<List<UserQuizAnswerResponse>> getMyQuizAnswers() {
        List<UserQuizAnswerResponse> responses = userQuizAnswerService.getByUserId(currentUserId()).stream()
                .map(this::toUserQuizAnswerResponse)
                .toList();
        return Result.ok(responses);
    }

    @Operation(summary = "获取当前用户某题答题记录", description = "获取当前用户在某道题的答题记录")
    @GetMapping("/me/quizzes/{quizId}")
    @SaCheckPermission("quiz:read")
    public Result<List<UserQuizAnswerResponse>> getMyQuizAnswersByQuizId(
            @Parameter(name = "quizId", description = "试题ID") @PathVariable Long quizId) {
        List<UserQuizAnswerResponse> responses =
                userQuizAnswerService.getByUserIdAndQuizId(currentUserId(), quizId).stream()
                        .map(this::toUserQuizAnswerResponse)
                        .toList();
        return Result.ok(responses);
    }

    @Operation(summary = "获取当前用户指定答题记录", description = "根据试题ID、选项ID精确获取当前用户的一条答题记录")
    @GetMapping("/me/quizzes/{quizId}/options/{optionId}")
    @SaCheckPermission("quiz:read")
    public Result<UserQuizAnswerResponse> getMyQuizAnswer(
            @Parameter(name = "quizId", description = "试题ID") @PathVariable Long quizId,
            @Parameter(name = "optionId", description = "选项ID") @PathVariable Long optionId) {
        return Result.ok(toUserQuizAnswerResponse(
                userQuizAnswerService.getByUserIdAndQuizIdAndOptionId(currentUserId(), quizId, optionId)));
    }

    @Operation(summary = "提交答题", description = "用户提交一道题的答案")
    @PostMapping("/me/quizzes/{quizId}/options/{optionId}")
    @SaCheckPermission("quiz:answer")
    public Result<Boolean> createQuizAnswer(
            @Parameter(name = "quizId", description = "试题ID") @PathVariable Long quizId,
            @Parameter(name = "optionId", description = "选项ID") @PathVariable Long optionId) {
        UserQuizAnswer userQuizAnswer = UserQuizAnswer.builder().build();
        userQuizAnswer.setUserId(currentUserId());
        userQuizAnswer.setQuizId(quizId);
        userQuizAnswer.setOptionId(optionId);
        return Result.ok(userQuizAnswerService.create(userQuizAnswer));
    }

    @Operation(summary = "更新答题", description = "用户更新已提交的答案")
    @PutMapping("/me/quizzes/{quizId}/options/{optionId}")
    @SaCheckPermission("quiz:answer")
    public Result<Boolean> updateQuizAnswer(
            @Parameter(name = "quizId", description = "试题ID") @PathVariable Long quizId,
            @Parameter(name = "optionId", description = "选项ID") @PathVariable Long optionId) {
        UserQuizAnswer userQuizAnswer = UserQuizAnswer.builder().build();
        userQuizAnswer.setUserId(currentUserId());
        userQuizAnswer.setQuizId(quizId);
        userQuizAnswer.setOptionId(optionId);
        return Result.ok(userQuizAnswerService.updateByUserIdAndQuizIdAndOptionId(userQuizAnswer));
    }

    private Long currentUserId() {
        return Long.valueOf(currentUserProvider.getCurrentUserId());
    }

    private UserQuizAnswerResponse toUserQuizAnswerResponse(UserQuizAnswer answer) {
        if (answer == null) {
            return null;
        }
        return UserQuizAnswerResponse.builder()
                .id(answer.getId())
                .userId(answer.getUserId())
                .optionId(answer.getOptionId())
                .quizId(answer.getQuizId())
                .userAnswer(answer.getUserAnswer())
                .isCorrect(answer.getIsCorrect())
                .scoreObtained(answer.getScoreObtained())
                .timeSpent(answer.getTimeSpent())
                .sessionId(answer.getSessionId())
                .answerTime(answer.getAnswerTime())
                .build();
    }
}
