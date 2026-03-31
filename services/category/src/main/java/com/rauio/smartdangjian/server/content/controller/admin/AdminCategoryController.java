package com.rauio.smartdangjian.server.content.controller.admin;

import com.rauio.smartdangjian.aop.annotation.DataScopeAccess;
import com.rauio.smartdangjian.aop.annotation.PermissionAccess;
import com.rauio.smartdangjian.aop.support.DataScopeAction;
import com.rauio.smartdangjian.aop.support.DataScopeResources;
import com.rauio.smartdangjian.pojo.response.Result;
import com.rauio.smartdangjian.server.content.pojo.dto.CategoryDto;
import com.rauio.smartdangjian.server.content.service.category.CategoryService;
import com.rauio.smartdangjian.utils.spec.UserType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/content/categories")
@RequiredArgsConstructor
@PermissionAccess(UserType.SCHOOL)
@Tag(name = "管理员目录接口", description = "系统管理员可维护全部分类；高校管理员仅可维护本校分类。学校归属由当前登录用户自动判定，不从请求参数读取。")
@Validated
public class AdminCategoryController {
    private final CategoryService categoryService;

    @Operation(
            summary = "添加根目录",
            description = "系统管理员调用时默认创建公共分类；高校管理员调用时会根据当前登录用户的 JWT 载荷自动绑定本校。该接口不会接收学校ID，如需代某学校建分类应使用专门管理接口。"
    )
    @PostMapping("/root")
    @DataScopeAccess(resource = DataScopeResources.CATEGORY, action = DataScopeAction.CREATE, body = "#dto")
    public Result<Boolean> create(@Valid @RequestBody CategoryDto dto){
        Boolean result = categoryService.create(dto);
        return Result.ok(result);
    }
    @Operation(
            summary = "添加子目录",
            description = "子目录会自动继承父分类归属。高校管理员只能在本校分类下新增子目录；系统管理员可在公共分类或任意学校分类下新增子目录。"
    )
    @PostMapping("/{id}/children")
    @DataScopeAccess(resource = DataScopeResources.CATEGORY, action = DataScopeAction.CREATE, id = "#id", query = "#children")
    public Result<Boolean> createByParentId(@Valid @RequestBody List<@Valid CategoryDto> children, @PathVariable String id){
        Boolean result = categoryService.createByParentId(children, id);
        return Result.ok(result);
    }
    @Operation(
            summary = "修改目录",
            description = "仅允许修改名称、描述和排序。分类归属不会从请求体读取，而是保留原有归属；公共分类仅系统管理员可维护，高校管理员仅可修改本校分类。"
    )
    @PutMapping("/{id}")
    @DataScopeAccess(resource = DataScopeResources.CATEGORY, action = DataScopeAction.UPDATE, id = "#id", body = "#dto")
    public Result<Boolean> update(@Valid @RequestBody CategoryDto dto, @PathVariable String id){
        Boolean result = categoryService.update(dto, id);
        return Result.ok(result);
    }
    @Operation(
            summary = "删除目录",
            description = "仅允许删除当前角色可维护范围内且没有子目录的分类。公共分类仅系统管理员可删除，高校管理员仅可删除本校分类。"
    )
    @DeleteMapping("/{id}")
    @DataScopeAccess(resource = DataScopeResources.CATEGORY, action = DataScopeAction.DELETE, id = "#id")
    public Result<Boolean> delete(@PathVariable String id){
            Boolean result = categoryService.delete(id);
            return Result.ok(result);
    }
    @Operation(
            summary = "删除目录和它的子目录",
            description = "递归删除当前角色可维护范围内的分类及全部子分类。公共分类仅系统管理员可删除，高校管理员仅可删除本校分类。"
    )
    @DeleteMapping("/{id}/all")
    @DataScopeAccess(resource = DataScopeResources.CATEGORY, action = DataScopeAction.DELETE, id = "#id")
    public Result<Boolean> deleteByIdWithChildren(@PathVariable String id){
        Boolean result = categoryService.deleteByIdWithChildren(id);
        return Result.ok(result);
    }
}
