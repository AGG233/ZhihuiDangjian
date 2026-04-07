package com.rauio.smartdangjian.server.quiz.controller.admin;

import com.rauio.smartdangjian.aop.annotation.DataScopeAccess;
import com.rauio.smartdangjian.aop.annotation.PermissionAccess;
import com.rauio.smartdangjian.aop.support.DataScopeAction;
import com.rauio.smartdangjian.aop.support.DataScopeResources;
import com.rauio.smartdangjian.pojo.response.Result;
import com.rauio.smartdangjian.server.quiz.pojo.entity.Quiz;
import com.rauio.smartdangjian.server.quiz.pojo.entity.QuizOption;
import com.rauio.smartdangjian.server.quiz.service.QuizOptionService;
import com.rauio.smartdangjian.server.quiz.service.QuizService;
import com.rauio.smartdangjian.utils.spec.UserType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "管理员试题接口", description = "试题和选项的增删改管理，需要校级及以上权限")
@RestController
@RequestMapping("/api/admin/quiz/quizzes")
@RequiredArgsConstructor
@PermissionAccess(UserType.SCHOOL)
public class AdminQuizController {

    private final QuizService quizService;
    private final QuizOptionService quizOptionService;

    @Operation(summary = "创建试题", description = "创建一道新试题")
    @PostMapping
    public Result<Boolean> createQuiz(@RequestBody Quiz quiz) {
        return Result.ok(quizService.create(quiz));
    }

    @Operation(summary = "更新试题", description = "根据ID更新试题信息")
    @PutMapping("/{id}")
    @DataScopeAccess(resource = DataScopeResources.QUIZ_ADMIN, action = DataScopeAction.UPDATE, id = "#id", query = "'QUIZ'")
    public Result<Boolean> updateQuiz(
            @Parameter(name = "id", description = "试题ID") @PathVariable String id,
            @RequestBody Quiz quiz) {
        quiz.setId(id);
        return Result.ok(quizService.update(quiz));
    }

    @Operation(summary = "删除试题", description = "根据ID删除试题")
    @DeleteMapping("/{id}")
    @DataScopeAccess(resource = DataScopeResources.QUIZ_ADMIN, action = DataScopeAction.DELETE, id = "#id", query = "'QUIZ'")
    public Result<Boolean> deleteQuiz(
            @Parameter(name = "id", description = "试题ID") @PathVariable String id) {
        return Result.ok(quizService.delete(id));
    }

    @Operation(summary = "创建选项", description = "为指定试题创建一个选项")
    @PostMapping("/{id}/options")
    public Result<Boolean> createQuizOption(
            @Parameter(name = "id", description = "试题ID") @PathVariable String id,
            @RequestBody QuizOption quizOption) {
        return Result.ok(quizOptionService.create(id, quizOption));
    }

    @Operation(summary = "更新选项", description = "根据选项ID更新选项信息")
    @PutMapping("/{quizId}/options/{optionId}")
    @DataScopeAccess(resource = DataScopeResources.QUIZ_ADMIN, action = DataScopeAction.UPDATE, id = "#optionId", query = "'OPTION'")
    public Result<Boolean> updateQuizOption(
            @Parameter(name = "optionId", description = "选项ID") @PathVariable String optionId,
            @RequestBody QuizOption quizOption) {
        return Result.ok(quizOptionService.update(optionId, quizOption));
    }

    @Operation(summary = "删除选项", description = "根据选项ID删除选项")
    @DeleteMapping("/{quizId}/options/{optionId}")
    @DataScopeAccess(resource = DataScopeResources.QUIZ_ADMIN, action = DataScopeAction.DELETE, id = "#optionId", query = "'OPTION'")
    public Result<Boolean> deleteQuizOption(
            @Parameter(name = "optionId", description = "选项ID") @PathVariable String optionId) {
        return Result.ok(quizOptionService.delete(optionId));
    }
}
