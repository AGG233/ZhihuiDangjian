package com.rauio.smartdangjian.server.search.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rauio.smartdangjian.pojo.response.Result;
import com.rauio.smartdangjian.server.search.pojo.response.DynamicProfileResponse;
import com.rauio.smartdangjian.server.search.pojo.response.LearningSummaryResponse;
import com.rauio.smartdangjian.server.search.service.UserProfileService;

import cn.dev33.satoken.annotation.SaCheckRole;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "用户画像接口", description = "动态画像与学习情况汇总")
@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final UserProfileService userProfileService;

    @Operation(summary = "获取学习情况汇总", description = "理论维度（课程学习时长+完成率）与政策理解维度（章节测试平均正确率）")
    @GetMapping("/learning-summary")
    @SaCheckRole("STUDENT")
    public Result<LearningSummaryResponse> getLearningSummary() {
        return Result.ok(userProfileService.getCurrentUserLearningSummary());
    }

    @Operation(summary = "获取动态画像", description = "近期热点标签、近8周成长趋势与薄弱知识域")
    @GetMapping("/dynamic")
    @SaCheckRole("STUDENT")
    public Result<DynamicProfileResponse> getDynamicProfile() {
        return Result.ok(userProfileService.getCurrentUserDynamicProfile());
    }
}
