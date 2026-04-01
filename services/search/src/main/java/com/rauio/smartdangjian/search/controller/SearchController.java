package com.rauio.smartdangjian.search.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rauio.smartdangjian.aop.annotation.PermissionAccess;
import com.rauio.smartdangjian.pojo.response.Result;
import com.rauio.smartdangjian.search.pojo.vo.UserProfileVO;
import com.rauio.smartdangjian.search.service.RecommendService;
import com.rauio.smartdangjian.search.service.SearchService;
import com.rauio.smartdangjian.search.service.UserProfileService;
import com.rauio.smartdangjian.server.content.pojo.vo.CourseVO;
import com.rauio.smartdangjian.utils.spec.UserType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "搜索与推荐接口", description = "课程搜索、个性化推荐、用户画像")
@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
public class SearchController {

    private final SearchService searchService;
    private final RecommendService recommendService;
    private final UserProfileService userProfileService;

    @Operation(summary = "搜索课程", description = "支持关键词全文检索，可按分类和难度过滤")
    @GetMapping("/courses")
    @PermissionAccess(UserType.STUDENT)
    public Result<Page<CourseVO>> searchCourses(
            @Parameter(description = "搜索关键词") @RequestParam(required = false) String keyword,
            @Parameter(description = "分类ID") @RequestParam(required = false) String categoryId,
            @Parameter(description = "难度") @RequestParam(required = false) String difficulty,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int pageNum,
            @Parameter(description = "每页条数") @RequestParam(defaultValue = "10") int pageSize) {
        return Result.ok(searchService.searchCourses(keyword, categoryId, difficulty, pageNum, pageSize));
    }

    @Operation(summary = "混合搜索", description = "全文检索 + 个性化推荐补充")
    @GetMapping("/hybrid")
    @PermissionAccess(UserType.STUDENT)
    public Result<Page<CourseVO>> hybridSearch(
            @Parameter(description = "搜索关键词") @RequestParam String keyword,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int pageNum,
            @Parameter(description = "每页条数") @RequestParam(defaultValue = "10") int pageSize) {
        return Result.ok(searchService.hybridSearch(keyword, pageNum, pageSize));
    }

    @Operation(summary = "获取个性化推荐课程", description = "融合协同过滤、知识图谱和画像的综合推荐")
    @GetMapping("/recommend")
    @PermissionAccess(UserType.STUDENT)
    public Result<Page<String>> recommend(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int pageNum,
            @Parameter(description = "每页条数") @RequestParam(defaultValue = "10") int pageSize) {
        String userId = userProfileService.getCurrentUserProfile().getUserId();
        return Result.ok(recommendService.recommend(userId, pageNum, pageSize));
    }

    @Operation(summary = "获取当前用户画像", description = "返回用户学习统计、知识掌握、兴趣分类、答题统计")
    @GetMapping("/profile")
    @PermissionAccess(UserType.STUDENT)
    public Result<UserProfileVO> getProfile() {
        return Result.ok(userProfileService.getCurrentUserProfile());
    }
}
