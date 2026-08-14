package com.rauio.smartdangjian.server.quiz.controller.user;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rauio.smartdangjian.constants.ErrorConstants;
import com.rauio.smartdangjian.exception.BusinessException;
import com.rauio.smartdangjian.pojo.response.Result;
import com.rauio.smartdangjian.server.quiz.constants.QuizErrorConstants;
import com.rauio.smartdangjian.server.quiz.pojo.request.ScormSubmitRequest;
import com.rauio.smartdangjian.server.quiz.pojo.response.ScormSummaryResponse;
import com.rauio.smartdangjian.server.quiz.service.scorm.ScormRegistrationService;
import com.rauio.smartdangjian.utils.SecurityUtils;

import cn.dev33.satoken.annotation.SaCheckRole;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "SCORM 学习进度接口", description = "用户上报 SCORM 学习进度与成绩，并查询个人学习汇总")
@RestController
@RequestMapping("/api/scorm")
@RequiredArgsConstructor
@SaCheckRole("STUDENT")
public class UserScormController {

    private final ScormRegistrationService scormRegistrationService;

    @Operation(
            summary = "上报 SCORM 学习进度",
            description = "上报某学习包内 SCO 的 cmi 数据（lesson_status/score/time），按用户+包+SCO 幂等 upsert")
    @PostMapping("/packages/{packageId}/registration")
    public Result<Boolean> submitRegistration(
            @Parameter(description = "学习包ID") @PathVariable Long packageId, @RequestBody ScormSubmitRequest request) {
        scormRegistrationService.submit(packageId, request);
        return Result.ok(true);
    }

    @Operation(summary = "查询个人 SCORM 学习汇总", description = "按学习包聚合当前用户的注册数、已完成数与平均分")
    @GetMapping("/packages/{packageId}/summary")
    public Result<ScormSummaryResponse> getSummary(@Parameter(description = "学习包ID") @PathVariable Long packageId) {
        String userId = SecurityUtils.getCurrentUserId();
        if (userId == null || userId.isBlank()) {
            throw new BusinessException(ErrorConstants.RESOURCE_NOT_AUTHORIZED, "请先登录");
        }
        List<ScormSummaryResponse> summaries = scormRegistrationService.getSummary(Long.valueOf(userId));
        return Result.ok(summaries.stream()
                .filter(summary -> packageId.equals(summary.getPackageId()))
                .findFirst()
                .orElseThrow(
                        () -> new BusinessException(QuizErrorConstants.SCORM_PACKAGE_NOT_FOUND, "SCORM 学习包不存在或尚未学习")));
    }
}
