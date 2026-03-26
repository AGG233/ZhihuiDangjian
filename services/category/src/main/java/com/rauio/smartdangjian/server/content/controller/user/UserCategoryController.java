package com.rauio.smartdangjian.server.content.controller.user;

import com.rauio.smartdangjian.aop.annotation.PermissionAccess;
import com.rauio.smartdangjian.pojo.response.Result;
import com.rauio.smartdangjian.server.content.pojo.entity.CategoryArticle;
import com.rauio.smartdangjian.server.content.pojo.entity.CategoryCourse;
import com.rauio.smartdangjian.server.content.pojo.vo.CategoryVO;
import com.rauio.smartdangjian.server.content.service.article.ArticleService;
import com.rauio.smartdangjian.server.content.service.category.CategoryService;
import com.rauio.smartdangjian.server.content.service.course.CourseService;
import com.rauio.smartdangjian.utils.spec.UserType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "用户目录接口")
@RestController
@RequestMapping("/api/content/categories")
@RequiredArgsConstructor
@PermissionAccess(UserType.STUDENT)
public class UserCategoryController {

    private final CategoryService categoryService;
    private final CourseService courseService;
    private final ArticleService articleService;

    @Operation(summary = "获取目录")
    @GetMapping("/{id}")
    public Result<CategoryVO> get(@PathVariable String id) {
        return Result.ok(categoryService.get(id));
    }

    @Operation(summary = "获取根目录列表")
    @GetMapping
    public Result<List<CategoryVO>> getList() {
        return Result.ok(categoryService.getRootList());
    }

    @Operation(summary = "获取所有根目录")
    @GetMapping("/root")
    public Result<List<CategoryVO>> getRootList() {
        return Result.ok(categoryService.getRootList());
    }

    @Operation(summary = "获取子目录")
    @GetMapping("/{id}/children")
    public Result<List<CategoryVO>> getByParentId(@PathVariable String id) {
        return Result.ok(categoryService.getByParentId(id));
    }

    @Operation(summary = "获取目录下的所有课程ID")
    @GetMapping("/{categoryId}/courses")
    public Result<List<CategoryCourse>> getByCategoryIdCourses(@PathVariable String categoryId) {
        return Result.ok(courseService.getByCategoryId(categoryId));
    }

    @Operation(summary = "获取目录下的所有文章ID")
    @GetMapping("/{categoryId}/articles")
    public Result<List<CategoryArticle>> getByCategoryIdArticles(@PathVariable String categoryId) {
        return Result.ok(articleService.getByCategoryId(categoryId));
    }
}
