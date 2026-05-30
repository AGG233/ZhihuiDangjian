package com.rauio.smartdangjian.server.search.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.rauio.smartdangjian.pojo.response.Result;
import com.rauio.smartdangjian.server.search.pojo.response.HotCategoryResponse;
import com.rauio.smartdangjian.server.search.pojo.response.HotCourseResponse;
import com.rauio.smartdangjian.server.search.pojo.response.LearningTrendResponse;
import com.rauio.smartdangjian.server.search.service.LearningHotspotService;

import cn.dev33.satoken.annotation.SaCheckRole;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "学习热点接口")
@RestController
@RequestMapping("/api/learning/hotspots")
@RequiredArgsConstructor
@SaCheckRole("STUDENT")
public class LearningHotspotController {

    private final LearningHotspotService learningHotspotService;

    @Operation(summary = "获取热门课程", description = "按学习人数降序返回热门课程列表")
    @GetMapping("/courses")
    public Result<List<HotCourseResponse>> getHotCourses(
            @Parameter(description = "返回数量，默认10，最大50") @RequestParam(defaultValue = "10") int limit) {
        return Result.ok(learningHotspotService.getHotCourses(limit));
    }

    @Operation(summary = "获取热门分类", description = "按学习人数降序返回热门分类列表")
    @GetMapping("/categories")
    public Result<List<HotCategoryResponse>> getHotCategories(
            @Parameter(description = "返回数量，默认10，最大50") @RequestParam(defaultValue = "10") int limit) {
        return Result.ok(learningHotspotService.getHotCategories(limit));
    }

    @Operation(summary = "获取学习趋势", description = "返回指定天数内的每日学习次数统计，缺失日期自动补0")
    @GetMapping("/trends")
    public Result<LearningTrendResponse> getTrends(
            @Parameter(description = "统计天数，默认7天") @RequestParam(defaultValue = "7") int days) {
        return Result.ok(learningHotspotService.getTrends(days));
    }
}
