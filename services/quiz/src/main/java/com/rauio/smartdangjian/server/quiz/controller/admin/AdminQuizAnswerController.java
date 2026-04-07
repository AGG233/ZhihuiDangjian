package com.rauio.smartdangjian.server.quiz.controller.admin;

import com.rauio.smartdangjian.aop.annotation.PermissionAccess;
import com.rauio.smartdangjian.pojo.response.Result;
import com.rauio.smartdangjian.server.quiz.service.UserQuizAnswerService;
import com.rauio.smartdangjian.utils.spec.UserType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "管理员答题记录接口", description = "管理员管理用户答题记录")
@RestController
@RequestMapping("/api/admin/quiz/answers")
@RequiredArgsConstructor
@PermissionAccess(UserType.MANAGER)
public class AdminQuizAnswerController {

    private final UserQuizAnswerService userQuizAnswerService;

    @Operation(summary = "删除答题记录", description = "根据用户ID、试题ID和选项ID删除指定答题记录")
    @DeleteMapping("/users/{id}/quizzes/{quizId}/options/{optionId}")
    public Result<Boolean> deleteQuizAnswer(
            @Parameter(name = "id", description = "用户ID") @PathVariable String id,
            @Parameter(name = "quizId", description = "试题ID") @PathVariable String quizId,
            @Parameter(name = "optionId", description = "选项ID") @PathVariable String optionId) {
        return Result.ok(userQuizAnswerService.deleteByUserIdAndQuizIdAndOptionId(id, quizId, optionId));
    }
}
