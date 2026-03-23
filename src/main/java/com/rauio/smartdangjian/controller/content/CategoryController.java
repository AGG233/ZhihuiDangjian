package com.rauio.smartdangjian.controller.content;

import com.rauio.smartdangjian.aop.annotation.PermissionAccess;
import com.rauio.smartdangjian.content.pojo.CategoryArticle;
import com.rauio.smartdangjian.content.pojo.CategoryCourse;
import com.rauio.smartdangjian.content.pojo.dto.CategoryDto;
import com.rauio.smartdangjian.pojo.response.Result;
import com.rauio.smartdangjian.content.pojo.vo.CategoryVO;
import com.rauio.smartdangjian.content.service.ArticleService;
import com.rauio.smartdangjian.content.service.CategoryService;
import com.rauio.smartdangjian.content.service.CourseService;
import com.rauio.smartdangjian.utils.spec.UserType;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/category")
@RequiredArgsConstructor
@PermissionAccess(UserType.SCHOOL)
public class CategoryController {
    private final CategoryService categoryService;
    private final CourseService courseService;
    private final ArticleService articleService;


    @Operation(
            summary = "获取目录",
            description = "根据目录id获取目录"
    )
    @GetMapping("/{id}")
    @PermissionAccess(UserType.STUDENT)
    public Result<CategoryVO> get(@PathVariable String id){
        CategoryVO result = categoryService.get(id);
        return Result.ok(result);
    }

    @Operation(
            summary = "获取所有目录",
            description = "获取所有目录"
    )
    @GetMapping("/all")
    @PermissionAccess(UserType.STUDENT)
    public Result<List<CategoryVO>> getList(){
        List<CategoryVO> result = categoryService.getRootList();
        return Result.ok(result);
    }

    @Operation(
            summary = "获取子目录",
            description = "根据目录id获取这个目录的子目录"
    )
    @GetMapping("/{id}/children")
    @PermissionAccess(UserType.STUDENT)
    public Result<List<CategoryVO>> getByParentId(@PathVariable String id){
        List<CategoryVO> result = categoryService.getByParentId(id);
        return Result.ok(result);
    }

    @Operation(
            summary = "获取所有根目录",
            description = "获取所有根目录"
    )
    @GetMapping("/rootNodes")
    @PermissionAccess(UserType.STUDENT)
    public Result<List<CategoryVO>> getRootList(){
        List<CategoryVO> result = categoryService.getRootList();
        return Result.ok(result);
    }
    @Operation(
            summary = "添加根目录",
            description = "添加根目录"
    )
    @PostMapping("/rootNode")
    public Result<Boolean> create(CategoryDto dto){
        Boolean result = categoryService.create(dto);
        return Result.ok(result);
    }
    @Operation(
            summary = "添加子目录",
            description = "向根目录id为路径参数的id添加子目录"
    )
    @PostMapping("/{id}/addChildren")
    public Result<Boolean> createByParentId(List<CategoryDto> children, @PathVariable String id){
        Boolean result = categoryService.createByParentId(children, id);
        return Result.ok(result);
    }
    @Operation(
            summary = "修改目录",
            description = "修改目录"
    )
    @PostMapping("/{id}")
    public Result<Boolean> update(CategoryDto dto, @PathVariable String id){
        Boolean result = categoryService.update(dto, id);
        return Result.ok(result);
    }
    @Operation(
            summary = "删除目录",
            description = "如果删除的目录没有子目录，则删除成功，有子目录则无法删除"
    )
    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable String id){
            Boolean result = categoryService.delete(id);
            return Result.ok(result);
    }
    @Operation(
            summary = "删除目录和它的子目录",
            description = "删除目录的同时也将删除它的子目录"
    )
    @DeleteMapping("/{id}/all")
    public Result<Boolean> deleteByIdWithChildren(@PathVariable String id){
        Boolean result = categoryService.deleteByIdWithChildren(id);
        return Result.ok(result);
    }

    @Operation(
            summary = "获取目录下的所有课程ID",
            description = "获取目录下的所有课程ID"
    )
    @GetMapping("/{categoryId}/courses")
    public Result<List<CategoryCourse>> getByCategoryIdCourses(@PathVariable String categoryId){
        List<CategoryCourse> result = courseService.getByCategoryId(categoryId);
        return Result.ok(result);
    }
    @Operation(
            summary = "获取目录下的所有文章ID",
            description = "获取目录下的所有文章ID"
    )
    @GetMapping("/{categoryId}/articles")
    public Result<List<CategoryArticle>> getByCategoryIdArticles(@PathVariable String categoryId) {
        List<CategoryArticle> result = articleService.getByCategoryId(categoryId);
        return Result.ok(result);
    }
}
