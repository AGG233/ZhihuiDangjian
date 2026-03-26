package com.rauio.smartdangjian.server.content.controller.admin;

import com.rauio.smartdangjian.aop.annotation.PermissionAccess;
import com.rauio.smartdangjian.server.content.pojo.dto.CategoryDto;
import com.rauio.smartdangjian.pojo.response.Result;
import com.rauio.smartdangjian.server.content.service.category.CategoryService;
import com.rauio.smartdangjian.utils.spec.UserType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/content/categories")
@RequiredArgsConstructor
@PermissionAccess(UserType.SCHOOL)
@Tag(name = "管理员目录接口")
public class AdminCategoryController {
    private final CategoryService categoryService;

    @Operation(
            summary = "添加根目录",
            description = "添加根目录"
    )
    @PostMapping("/root")
    public Result<Boolean> create(@RequestBody CategoryDto dto){
        Boolean result = categoryService.create(dto);
        return Result.ok(result);
    }
    @Operation(
            summary = "添加子目录",
            description = "向根目录id为路径参数的id添加子目录"
    )
    @PostMapping("/{id}/children")
    public Result<Boolean> createByParentId(@RequestBody List<CategoryDto> children, @PathVariable String id){
        Boolean result = categoryService.createByParentId(children, id);
        return Result.ok(result);
    }
    @Operation(
            summary = "修改目录",
            description = "修改目录"
    )
    @PutMapping("/{id}")
    public Result<Boolean> update(@RequestBody CategoryDto dto, @PathVariable String id){
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
}
