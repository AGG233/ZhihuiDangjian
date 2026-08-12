package com.rauio.smartdangjian.server.search.controller;

import java.util.List;

import org.springframework.web.bind.annotation.*;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rauio.smartdangjian.common.utils.IdUtil;
import com.rauio.smartdangjian.pojo.response.Result;
import com.rauio.smartdangjian.server.content.pojo.response.CourseResponse;
import com.rauio.smartdangjian.server.search.pojo.response.HotCategoryResponse;
import com.rauio.smartdangjian.server.search.pojo.response.HotCourseResponse;
import com.rauio.smartdangjian.server.search.pojo.response.LearningTrendResponse;
import com.rauio.smartdangjian.server.search.pojo.response.UserProfileResponse;
import com.rauio.smartdangjian.server.search.service.HotSpotService;
import com.rauio.smartdangjian.server.search.service.RecommendService;
import com.rauio.smartdangjian.server.search.service.SearchService;
import com.rauio.smartdangjian.server.search.service.UserProfileService;

import cn.dev33.satoken.annotation.SaCheckRole;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "搜索与推荐接口", description = "课程搜索、个性化推荐、用户画像")
@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
public class SearchController {

    private final SearchService searchService;
    private final RecommendService recommendService;
    private final UserProfileService userProfileService;
    private final HotSpotService hotSpotService;

    @Operation(summary = "搜索课程", description = "支持关键词全文检索，可按分类和难度过滤")
    @GetMapping("/courses")
    @SaCheckRole("STUDENT")
    public Result<Page<CourseResponse>> searchCourses(
            @Parameter(name = "keyword", description = "搜索关键词") @RequestParam(required = false) String keyword,
            @Parameter(name = "categoryId", description = "分类ID") @RequestParam(required = false) String categoryId,
            @Parameter(name = "difficulty", description = "难度") @RequestParam(required = false) String difficulty,
            @Parameter(name = "pageNum", description = "页码") @RequestParam(defaultValue = "1") int pageNum,
            @Parameter(name = "pageSize", description = "每页条数") @RequestParam(defaultValue = "10") int pageSize) {
        return Result.ok(searchService.searchCourses(keyword, categoryId, difficulty, pageNum, pageSize));
    }

    @Operation(summary = "混合搜索", description = "全文检索 + 个性化推荐补充")
    @GetMapping("/hybrid")
    @SaCheckRole("STUDENT")
    public Result<Page<CourseResponse>> hybridSearch(
            @Parameter(name = "keyword", description = "搜索关键词") @RequestParam String keyword,
            @Parameter(name = "pageNum", description = "页码") @RequestParam(defaultValue = "1") int pageNum,
            @Parameter(name = "pageSize", description = "每页条数") @RequestParam(defaultValue = "10") int pageSize) {
        return Result.ok(searchService.hybridSearch(keyword, pageNum, pageSize));
    }

    @Operation(summary = "获取个性化推荐课程", description = "融合协同过滤、知识图谱和画像的综合推荐")
    @GetMapping("/recommend")
    @SaCheckRole("STUDENT")
    public Result<Page<Long>> recommend(
            @Parameter(name = "pageNum", description = "页码") @RequestParam(defaultValue = "1") int pageNum,
            @Parameter(name = "pageSize", description = "每页条数") @RequestParam(defaultValue = "10") int pageSize) {
        String userIdStr = userProfileService.getCurrentUserProfile().getUserId();
        Long userId = IdUtil.parse(userIdStr);
        return Result.ok(recommendService.recommend(userId, pageNum, pageSize));
    }

    @Operation(summary = "获取当前用户画像", description = "返回用户学习统计、知识掌握、兴趣分类、答题统计")
    @GetMapping("/profile")
    @SaCheckRole("STUDENT")
    public Result<UserProfileResponse> getProfile() {
        return Result.ok(userProfileService.getCurrentUserProfile());
    }

    @Operation(summary = "热门课程", description = "按报名人数与近30天学习人数加权排序的热门课程 Top N")
    @GetMapping("/hot/courses")
    @SaCheckRole("STUDENT")
    public Result<List<HotCourseResponse>> hotCourses(
            @Parameter(name = "topN", description = "返回条数，默认10") @RequestParam(defaultValue = "10") int topN) {
        return Result.ok(hotSpotService.getHotCourses(topN));
    }

    @Operation(summary = "热门分类", description = "按关联课程报名人数汇总的热门分类 Top N")
    @GetMapping("/hot/categories")
    @SaCheckRole("STUDENT")
    public Result<List<HotCategoryResponse>> hotCategories(
            @Parameter(name = "topN", description = "返回条数，默认10") @RequestParam(defaultValue = "10") int topN) {
        return Result.ok(hotSpotService.getHotCategories(topN));
    }

    @Operation(summary = "学习趋势", description = "按天返回近N天学习人次与总时长")
    @GetMapping("/trend/learning")
    @SaCheckRole("STUDENT")
    public Result<List<LearningTrendResponse>> learningTrend(
            @Parameter(name = "days", description = "统计天数，默认30") @RequestParam(defaultValue = "30") int days) {
        return Result.ok(hotSpotService.getLearningTrend(days));
    }
}
