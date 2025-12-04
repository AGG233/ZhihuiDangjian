package com.rauio.ZhihuiDangjian.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rauio.ZhihuiDangjian.aop.annotation.PermissionAccess;
import com.rauio.ZhihuiDangjian.pojo.CategoryArticle;
import com.rauio.ZhihuiDangjian.pojo.CategoryCourse;
import com.rauio.ZhihuiDangjian.pojo.dto.CategoryDto;
import com.rauio.ZhihuiDangjian.pojo.response.ApiResponse;
import com.rauio.ZhihuiDangjian.pojo.vo.CategoryVO;
import com.rauio.ZhihuiDangjian.service.ArticleService;
import com.rauio.ZhihuiDangjian.service.CategoryService;
import com.rauio.ZhihuiDangjian.service.CourseService;
import com.rauio.ZhihuiDangjian.utils.Spec.UserType;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/category")
@RequiredArgsConstructor
@PermissionAccess(UserType.TEACHER)
public class CategoryController {
    private final CategoryService categoryService;
    private final ObjectMapper objectMapper;
    private final CourseService courseService;
    private final ArticleService articleService;


    @Operation(
            summary = "获取目录",
            description = "根据目录id获取目录"
    )
    @GetMapping("/{id}")
    @PermissionAccess(UserType.STUDENT)
    public ApiResponse<CategoryVO> get(@PathVariable Long id) throws JsonProcessingException {
        CategoryVO result = categoryService.getById(id);
        return ApiResponse.ok(result);
    }

    @Operation(
            summary = "获取所有目录",
            description = "获取所有目录"
    )
    @GetMapping("/all")
    @PermissionAccess(UserType.STUDENT)
    public ApiResponse<List<CategoryVO>> getAll() throws JsonProcessingException {
        List<CategoryVO> result = categoryService.getRootNodes();
        return ApiResponse.ok(result);
    }

    @Operation(
            summary = "获取子目录",
            description = "根据目录id获取这个目录的子目录"
    )
    @GetMapping("/{id}/children")
    @PermissionAccess(UserType.STUDENT)
    public ApiResponse<List<CategoryVO>> getChildren(@PathVariable Long id) throws JsonProcessingException {
        List<CategoryVO> result = categoryService.getChildren(id);
        return ApiResponse.ok(result);
    }

    @Operation(
            summary = "获取所有根目录",
            description = "获取所有根目录"
    )
    @GetMapping("/rootNodes")
    @PermissionAccess(UserType.STUDENT)
    public ApiResponse<List<CategoryVO>> getRootNodes() throws JsonProcessingException {
        List<CategoryVO> result = categoryService.getRootNodes();
        return ApiResponse.ok(result);
    }
    @Operation(
            summary = "添加根目录",
            description = "添加根目录"
    )
    @PostMapping("/rootNode")
    public ApiResponse<Boolean> add(CategoryDto dto) throws JsonProcessingException {
        Boolean result = categoryService.add(dto);
        return ApiResponse.ok(result);
    }
    @Operation(
            summary = "添加子目录",
            description = "向根目录id为路径参数的id添加子目录"
    )
    @PostMapping("/{id}/addChildren")
    public ApiResponse<Boolean> addChildren(List<CategoryDto> children, @PathVariable String id) throws JsonProcessingException {
        Boolean result = categoryService.addChildren(children, id);
        return ApiResponse.ok(result);
    }
    @Operation(
            summary = "修改目录",
            description = "修改目录"
    )
    @PostMapping("/{id}")
    public ApiResponse<Boolean> update(CategoryDto dto, @PathVariable Long id){
        Boolean result = categoryService.update(dto, id);
        return ApiResponse.ok(result);
    }
    @Operation(
            summary = "删除目录",
            description = "如果删除的目录没有子目录，则删除成功，有子目录则无法删除"
    )
    @DeleteMapping("/{id}")
    public ApiResponse<Boolean> delete(@PathVariable Long id){
            Boolean result = categoryService.delete(id);
            return ApiResponse.ok(result);
    }
    @Operation(
            summary = "删除目录和它的子目录",
            description = "删除目录的同时也将删除它的子目录"
    )
    @DeleteMapping("/{id}/all")
    public ApiResponse<Boolean> deleteAll(@PathVariable Long id){
        Boolean result = categoryService.deleteAll(id);
        return ApiResponse.ok(result);
    }

    @Operation(
            summary = "获取目录下的所有课程ID",
            description = "获取目录下的所有课程ID"
    )
    @GetMapping("/{categoryId}/courses")
    public ApiResponse<List<CategoryCourse>> getAllCoursesOfCategory(@PathVariable String categoryId){
        List<CategoryCourse> result = courseService.getAllCoursesOfCategory(categoryId);
        return ApiResponse.ok(result);
    }
    @Operation(
            summary = "获取目录下的所有文章ID",
            description = "获取目录下的所有文章ID"
    )
    @GetMapping("/{categoryId}/articles")
    public ApiResponse<List<CategoryArticle>> getAllArticlesOfCategory(@PathVariable String categoryId) {
        List<CategoryArticle> result = articleService.getAllArticlesOfCategory(categoryId);
        return ApiResponse.ok(result);
    }
}