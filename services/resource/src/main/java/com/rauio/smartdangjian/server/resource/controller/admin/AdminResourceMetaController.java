package com.rauio.smartdangjian.server.resource.controller.admin;

import com.rauio.smartdangjian.aop.annotation.PermissionAccess;
import com.rauio.smartdangjian.pojo.response.Result;
import com.rauio.smartdangjian.server.resource.pojo.entity.ResourceMeta;
import com.rauio.smartdangjian.server.resource.pojo.request.ResourceMetaCreateRequest;
import com.rauio.smartdangjian.server.resource.pojo.request.ResourceMetaUpdateRequest;
import com.rauio.smartdangjian.server.resource.service.ResourceMetaService;
import com.rauio.smartdangjian.utils.spec.UserType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "管理员资源接口", description = "文件上传删除接口")
@RestController
@RequestMapping("/api/admin/resource/files")
@RequiredArgsConstructor
@PermissionAccess(UserType.MANAGER)
public class AdminResourceMetaController {

    private final ResourceMetaService resourceMetaService;

    @Operation(summary = "创建资源元数据", description = "由管理员创建资源元数据，不处理文件上传")
    @PostMapping
    public Result<ResourceMeta> create(@RequestBody @Valid ResourceMetaCreateRequest request) {
        return Result.ok(resourceMetaService.create(request));
    }

    @Operation(summary = "获取资源元数据", description = "根据资源ID获取资源元数据详情")
    @GetMapping("/{id}")
    public Result<ResourceMeta> get(@PathVariable String id) {
        return Result.ok(resourceMetaService.get(id));
    }

    @Operation(summary = "查询资源元数据", description = "按上传人、原始文件名、哈希、资源类型、状态筛选")
    @GetMapping
    public Result<List<ResourceMeta>> list(
            @Parameter(description = "上传人ID") @RequestParam(required = false) String uploaderId,
            @Parameter(description = "原始文件名") @RequestParam(required = false) String originalName,
            @Parameter(description = "文件哈希") @RequestParam(required = false) String hash,
            @Parameter(description = "资源类型") @RequestParam(required = false) String resourceType,
            @Parameter(description = "资源状态") @RequestParam(required = false) Integer status
    ) {
        return Result.ok(resourceMetaService.list(uploaderId, originalName, hash, resourceType, status));
    }

    @Operation(summary = "更新资源元数据", description = "根据资源ID更新原始文件名、对象存储键、资源类型、状态")
    @PutMapping("/{id}")
    public Result<Boolean> update(@PathVariable String id, @RequestBody @Valid ResourceMetaUpdateRequest request) {
        return Result.ok(resourceMetaService.update(id, request));
    }

    @Operation(summary = "删除资源元数据", description = "根据资源ID删除资源元数据、关联内容块及COS对象")
    @DeleteMapping("/{id}")
    public Result<Boolean> deleteById(@PathVariable String id) {
        return Result.ok(resourceMetaService.delete(id));
    }

    @Operation(summary = "删除单个文件", description = "根据文件hash值删除")
    @DeleteMapping("/by-hash/{hash}")
    public Result<Boolean> delete(@PathVariable String hash){
        return Result.ok(resourceMetaService.deleteByHash(hash));
    }

    @Operation(summary = "批量删除文件", description = "根据hash值批量删除文件")
    @DeleteMapping
    public Result<Boolean> delete(@RequestParam String[] hash){
        return Result.ok(resourceMetaService.deleteByHashes(List.of(hash)));
    }
}
