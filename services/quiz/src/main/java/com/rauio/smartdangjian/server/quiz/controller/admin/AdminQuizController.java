package com.rauio.smartdangjian.server.quiz.controller.admin;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rauio.smartdangjian.common.utils.IdUtil;
import com.rauio.smartdangjian.pojo.response.Result;
import com.rauio.smartdangjian.security.RoleConstants;
import com.rauio.smartdangjian.server.quiz.pojo.request.QuizOptionRequest;
import com.rauio.smartdangjian.server.quiz.pojo.request.QuizRequest;
import com.rauio.smartdangjian.server.quiz.pojo.response.QuizResponse;
import com.rauio.smartdangjian.server.quiz.service.QuizOptionService;
import com.rauio.smartdangjian.server.quiz.service.QuizService;

import cn.dev33.satoken.annotation.SaCheckRole;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "管理员试题接口", description = "试题和选项的增删改管理，需要校级及以上权限")
@RestController
@RequestMapping("/api/admin/quiz/quizzes")
@RequiredArgsConstructor
@SaCheckRole(RoleConstants.SCHOOL)
@Validated
public class AdminQuizController {

    private static final int PAGE_NUM_MIN = 1;
    private static final int PAGE_SIZE_MIN = 1;
    private static final int PAGE_SIZE_MAX = 100;

    private final QuizService quizService;
    private final QuizOptionService quizOptionService;

    @Operation(summary = "获取试题详情", description = "根据试题ID获取试题详情")
    @GetMapping("/{id}")
    public Result<QuizResponse> getQuiz(@Parameter(name = "id", description = "试题ID") @PathVariable String id) {
        return Result.ok(QuizResponse.from(quizService.get(IdUtil.parse(id))));
    }

    @Operation(summary = "分页查询试题", description = "管理员分页查询试题列表，支持按章节/难度/启用状态/关键字筛选")
    @GetMapping
    public Result<Page<QuizResponse>> getQuizPage(
            @Parameter(name = "chapterId", description = "章节ID") @RequestParam(required = false) Long chapterId,
            @Parameter(name = "difficulty", description = "难度") @RequestParam(required = false) String difficulty,
            @Parameter(name = "isActive", description = "启用状态") @RequestParam(required = false) Boolean isActive,
            @Parameter(name = "keyword", description = "题目内容关键字") @RequestParam(required = false) String keyword,
            @Parameter(name = "pageNum", description = "页码") @RequestParam(defaultValue = "1") @Min(PAGE_NUM_MIN)
                    int pageNum,
            @Parameter(name = "pageSize", description = "每页大小")
                    @RequestParam(defaultValue = "10")
                    @Min(PAGE_SIZE_MIN)
                    @Max(PAGE_SIZE_MAX)
                    int pageSize) {
        return Result.ok(quizService.searchAdminQuizzes(chapterId, difficulty, isActive, keyword, pageNum, pageSize));
    }

    @Operation(summary = "创建试题", description = "创建一道新试题")
    @PostMapping
    public Result<Boolean> createQuiz(@RequestBody @Valid QuizRequest request) {
        return Result.ok(quizService.create(request));
    }

    @Operation(summary = "更新试题", description = "根据ID更新试题信息")
    @PutMapping("/{id}")
    public Result<Boolean> updateQuiz(
            @Parameter(name = "id", description = "试题ID") @PathVariable String id,
            @RequestBody @Valid QuizRequest request) {
        return Result.ok(quizService.update(IdUtil.parse(id), request));
    }

    @Operation(summary = "删除试题", description = "根据ID删除试题")
    @DeleteMapping("/{id}")
    public Result<Boolean> deleteQuiz(@Parameter(name = "id", description = "试题ID") @PathVariable String id) {
        return Result.ok(quizService.delete(IdUtil.parse(id)));
    }

    @Operation(summary = "创建选项", description = "为指定试题创建一个选项")
    @PostMapping("/{id}/options")
    public Result<Boolean> createQuizOption(
            @Parameter(name = "id", description = "试题ID") @PathVariable String id,
            @RequestBody @Valid QuizOptionRequest request) {
        return Result.ok(quizOptionService.create(IdUtil.parse(id), request));
    }

    @Operation(summary = "更新选项", description = "根据选项ID更新选项信息")
    @PutMapping("/{quizId}/options/{optionId}")
    public Result<Boolean> updateQuizOption(
            @Parameter(name = "optionId", description = "选项ID") @PathVariable String optionId,
            @RequestBody @Valid QuizOptionRequest request) {
        return Result.ok(quizOptionService.update(IdUtil.parse(optionId), request));
    }

    @Operation(summary = "删除选项", description = "根据选项ID删除选项")
    @DeleteMapping("/{quizId}/options/{optionId}")
    public Result<Boolean> deleteQuizOption(
            @Parameter(name = "optionId", description = "选项ID") @PathVariable String optionId) {
        return Result.ok(quizOptionService.delete(IdUtil.parse(optionId)));
    }
}
