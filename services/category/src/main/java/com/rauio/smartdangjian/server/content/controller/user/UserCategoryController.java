package com.rauio.smartdangjian.server.content.controller.user;

import com.rauio.smartdangjian.aop.annotation.DataScopeAccess;
import com.rauio.smartdangjian.aop.annotation.PermissionAccess;
import com.rauio.smartdangjian.aop.support.DataScopeAction;
import com.rauio.smartdangjian.aop.support.DataScopeResources;
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

@Tag(name = "用户目录接口", description = "普通用户和高校管理员可读取公共分类与本校分类；系统管理员可读取全部分类。学校范围由当前登录用户自动判定。")
@RestController
@RequestMapping("/api/content/categories")
@RequiredArgsConstructor
@PermissionAccess(UserType.STUDENT)
public class UserCategoryController {

    private final CategoryService categoryService;
    private final CourseService courseService;
    private final ArticleService articleService;

    @Operation(summary = "获取目录", description = "普通用户和高校管理员只能读取公共分类与本校分类；系统管理员可读取全部分类。")
    @GetMapping("/{id}")
    @DataScopeAccess(resource = DataScopeResources.CATEGORY, action = DataScopeAction.READ, id = "#id")
    public Result<CategoryVO> get(@PathVariable String id) {
        return Result.ok(categoryService.get(id));
    }

    @Operation(summary = "获取根目录列表", description = "返回当前用户可见范围内的根分类。当前接口与 `/root` 保持同样返回结果，保留仅为兼容现有调用方。")
    @GetMapping
    @DataScopeAccess(resource = DataScopeResources.CATEGORY, action = DataScopeAction.SEARCH)
    public Result<List<CategoryVO>> getList() {
        return Result.ok(categoryService.getRootList());
    }

    @Operation(summary = "获取所有根目录", description = "返回当前用户可见范围内的根分类。普通用户和高校管理员可见公共分类加本校分类，系统管理员可见全部根分类。")
    @GetMapping("/root")
    @DataScopeAccess(resource = DataScopeResources.CATEGORY, action = DataScopeAction.SEARCH)
    public Result<List<CategoryVO>> getRootList() {
        return Result.ok(categoryService.getRootList());
    }

    @Operation(summary = "获取子目录", description = "仅返回当前用户有权访问的父分类下的子目录。")
    @GetMapping("/{id}/children")
    @DataScopeAccess(resource = DataScopeResources.CATEGORY, action = DataScopeAction.READ, id = "#id")
    public Result<List<CategoryVO>> getByParentId(@PathVariable String id) {
        return Result.ok(categoryService.getByParentId(id));
    }

    @Operation(summary = "获取目录下的所有课程ID", description = "仅允许访问当前用户可见分类下的课程关联。")
    @GetMapping("/{categoryId}/courses")
    @DataScopeAccess(resource = DataScopeResources.CATEGORY, action = DataScopeAction.READ, id = "#categoryId")
    public Result<List<CategoryCourse>> getByCategoryIdCourses(@PathVariable String categoryId) {
        return Result.ok(courseService.getByCategoryId(categoryId));
    }

    @Operation(summary = "获取目录下的所有文章ID", description = "仅允许访问当前用户可见分类下的文章关联。")
    @GetMapping("/{categoryId}/articles")
    @DataScopeAccess(resource = DataScopeResources.CATEGORY, action = DataScopeAction.READ, id = "#categoryId")
    public Result<List<CategoryArticle>> getByCategoryIdArticles(@PathVariable String categoryId) {
        return Result.ok(articleService.getByCategoryId(categoryId));
    }
}
