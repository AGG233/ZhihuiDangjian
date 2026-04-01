package com.rauio.smartdangjian.server.quiz.controller.user;

import com.rauio.smartdangjian.aop.annotation.PermissionAccess;
import com.rauio.smartdangjian.aop.annotation.ResourceAccess;
import com.rauio.smartdangjian.pojo.response.Result;
import com.rauio.smartdangjian.server.quiz.pojo.entity.UserQuizAnswer;
import com.rauio.smartdangjian.server.quiz.service.UserQuizAnswerService;
import com.rauio.smartdangjian.utils.spec.UserType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "用户答题记录接口", description = "用户查看和提交答题记录")
@RestController
@RequestMapping("/api/quiz/answers")
@RequiredArgsConstructor
@PermissionAccess(UserType.STUDENT)
public class UserQuizAnswerController {

    private final UserQuizAnswerService userQuizAnswerService;

    @Operation(summary = "获取用户全部答题记录", description = "根据用户ID获取该用户的所有答题记录")
    @GetMapping("/users/{id}")
    @ResourceAccess(id = "#id")
    public Result<List<UserQuizAnswer>> getByUserIdQuizAnswers(
            @Parameter(description = "用户ID") @PathVariable String id) {
        return Result.ok(userQuizAnswerService.getByUserId(id));
    }

    @Operation(summary = "获取用户某题答题记录", description = "根据用户ID和试题ID获取该用户在某道题的答题记录")
    @GetMapping("/users/{id}/quizzes/{quizId}")
    @ResourceAccess(id = "#id")
    public Result<List<UserQuizAnswer>> getByQuizIdQuizAnswers(
            @Parameter(description = "用户ID") @PathVariable String id,
            @Parameter(description = "试题ID") @PathVariable String quizId) {
        return Result.ok(userQuizAnswerService.getByUserIdAndQuizId(id, quizId));
    }

    @Operation(summary = "获取指定答题记录", description = "根据用户ID、试题ID、选项ID精确获取一条答题记录")
    @GetMapping("/users/{id}/quizzes/{quizId}/options/{optionId}")
    @ResourceAccess(id = "#id")
    public Result<UserQuizAnswer> getByUserIdAndQuizIdAndOptionIdQuizAnswer(
            @Parameter(description = "用户ID") @PathVariable String id,
            @Parameter(description = "试题ID") @PathVariable String quizId,
            @Parameter(description = "选项ID") @PathVariable String optionId) {
        return Result.ok(userQuizAnswerService.getByUserIdAndQuizIdAndOptionId(id, quizId, optionId));
    }

    @Operation(summary = "提交答题", description = "用户提交一道题的答案")
    @PostMapping("/users/{id}/quizzes/{quizId}/options/{optionId}")
    @ResourceAccess(id = "#id")
    public Result<Boolean> createQuizAnswer(
            @Parameter(description = "用户ID") @PathVariable String id,
            @Parameter(description = "试题ID") @PathVariable String quizId,
            @Parameter(description = "选项ID") @PathVariable String optionId) {
        UserQuizAnswer userQuizAnswer = UserQuizAnswer.builder().build();
        userQuizAnswer.setUserId(id);
        userQuizAnswer.setQuizId(quizId);
        userQuizAnswer.setOptionId(optionId);
        return Result.ok(userQuizAnswerService.create(userQuizAnswer));
    }

    @Operation(summary = "更新答题", description = "用户更新已提交的答案")
    @PutMapping("/users/{id}/quizzes/{quizId}/options/{optionId}")
    @ResourceAccess(id = "#id")
    public Result<Boolean> updateQuizAnswer(
            @Parameter(description = "用户ID") @PathVariable String id,
            @Parameter(description = "试题ID") @PathVariable String quizId,
            @Parameter(description = "选项ID") @PathVariable String optionId) {
        UserQuizAnswer userQuizAnswer = UserQuizAnswer.builder().build();
        userQuizAnswer.setUserId(id);
        userQuizAnswer.setQuizId(quizId);
        userQuizAnswer.setOptionId(optionId);
        return Result.ok(userQuizAnswerService.updateByUserIdAndQuizIdAndOptionId(userQuizAnswer));
    }
}
