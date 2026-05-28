package com.rauio.smartdangjian.server.quiz.controller.user;

import java.util.List;

import org.springframework.web.bind.annotation.*;

import com.rauio.smartdangjian.pojo.response.Result;
import com.rauio.smartdangjian.server.quiz.pojo.entity.UserQuizAnswer;
import com.rauio.smartdangjian.server.quiz.pojo.response.UserQuizAnswerResponse;
import com.rauio.smartdangjian.server.quiz.service.UserQuizAnswerService;
import com.rauio.smartdangjian.utils.SecurityUtils;

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

    @Operation(summary = "获取用户全部答题记录", description = "根据用户ID获取该用户的所有答题记录")
    @GetMapping("/users/{id}")
    @SaCheckPermission("quiz:read")
    public Result<List<UserQuizAnswerResponse>> getByUserIdQuizAnswers(
            @Parameter(name = "id", description = "用户ID") @PathVariable Long id) {
        String currentUserId = SecurityUtils.getCurrentUserId();
        if (!currentUserId.equals(String.valueOf(id))) {
            return Result.error("403", "无权查看其他用户的答题记录");
        }
        List<UserQuizAnswerResponse> responses = userQuizAnswerService.getByUserId(id).stream()
                .map(this::toUserQuizAnswerResponse)
                .toList();
        return Result.ok(responses);
    }

    @Operation(summary = "获取用户某题答题记录", description = "根据用户ID和试题ID获取该用户在某道题的答题记录")
    @GetMapping("/users/{id}/quizzes/{quizId}")
    @SaCheckPermission("quiz:read")
    public Result<List<UserQuizAnswerResponse>> getByQuizIdQuizAnswers(
            @Parameter(name = "id", description = "用户ID") @PathVariable Long id,
            @Parameter(name = "quizId", description = "试题ID") @PathVariable Long quizId) {
        String currentUserId = SecurityUtils.getCurrentUserId();
        if (!currentUserId.equals(String.valueOf(id))) {
            return Result.error("403", "无权查看其他用户的答题记录");
        }
        List<UserQuizAnswerResponse> responses = userQuizAnswerService.getByUserIdAndQuizId(id, quizId).stream()
                .map(this::toUserQuizAnswerResponse)
                .toList();
        return Result.ok(responses);
    }

    @Operation(summary = "获取指定答题记录", description = "根据用户ID、试题ID、选项ID精确获取一条答题记录")
    @GetMapping("/users/{id}/quizzes/{quizId}/options/{optionId}")
    @SaCheckPermission("quiz:read")
    public Result<UserQuizAnswerResponse> getByUserIdAndQuizIdAndOptionIdQuizAnswer(
            @Parameter(name = "id", description = "用户ID") @PathVariable Long id,
            @Parameter(name = "quizId", description = "试题ID") @PathVariable Long quizId,
            @Parameter(name = "optionId", description = "选项ID") @PathVariable Long optionId) {
        String currentUserId = SecurityUtils.getCurrentUserId();
        if (!currentUserId.equals(String.valueOf(id))) {
            return Result.error("403", "无权查看其他用户的答题记录");
        }
        return Result.ok(
                toUserQuizAnswerResponse(userQuizAnswerService.getByUserIdAndQuizIdAndOptionId(id, quizId, optionId)));
    }

    @Operation(summary = "提交答题", description = "用户提交一道题的答案")
    @PostMapping("/users/{id}/quizzes/{quizId}/options/{optionId}")
    @SaCheckPermission("quiz:answer")
    public Result<Boolean> createQuizAnswer(
            @Parameter(name = "id", description = "用户ID") @PathVariable Long id,
            @Parameter(name = "quizId", description = "试题ID") @PathVariable Long quizId,
            @Parameter(name = "optionId", description = "选项ID") @PathVariable Long optionId) {
        UserQuizAnswer userQuizAnswer = UserQuizAnswer.builder().build();
        userQuizAnswer.setUserId(id);
        userQuizAnswer.setQuizId(quizId);
        userQuizAnswer.setOptionId(optionId);
        return Result.ok(userQuizAnswerService.create(userQuizAnswer));
    }

    @Operation(summary = "更新答题", description = "用户更新已提交的答案")
    @PutMapping("/users/{id}/quizzes/{quizId}/options/{optionId}")
    @SaCheckPermission("quiz:answer")
    public Result<Boolean> updateQuizAnswer(
            @Parameter(name = "id", description = "用户ID") @PathVariable Long id,
            @Parameter(name = "quizId", description = "试题ID") @PathVariable Long quizId,
            @Parameter(name = "optionId", description = "选项ID") @PathVariable Long optionId) {
        UserQuizAnswer userQuizAnswer = UserQuizAnswer.builder().build();
        userQuizAnswer.setUserId(id);
        userQuizAnswer.setQuizId(quizId);
        userQuizAnswer.setOptionId(optionId);
        return Result.ok(userQuizAnswerService.updateByUserIdAndQuizIdAndOptionId(userQuizAnswer));
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
