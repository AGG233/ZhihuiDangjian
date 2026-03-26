package com.rauio.smartdangjian.server.resource.controller.admin;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.rauio.smartdangjian.aop.annotation.PermissionAccess;
import com.rauio.smartdangjian.exception.BusinessException;
import com.rauio.smartdangjian.pojo.response.Result;
import com.rauio.smartdangjian.server.resource.pojo.entity.ResourceMeta;
import com.rauio.smartdangjian.server.resource.pojo.request.BannerCreateRequest;
import com.rauio.smartdangjian.server.resource.pojo.request.BannerUpdateRequest;
import com.rauio.smartdangjian.server.resource.pojo.request.MultipartUploadSessionRequest;
import com.rauio.smartdangjian.server.resource.service.BannerService;
import com.rauio.smartdangjian.utils.spec.UserType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "管理员轮播图接口", description = "提供轮播图查询、添加、更新、删除能力")
@RestController
@RequestMapping("/api/admin/resource/banners")
@RequiredArgsConstructor
@PermissionAccess(UserType.MANAGER)
public class AdminBannerController {

    private final BannerService bannerService;

    @Operation(summary = "获取轮播图列表")
    @GetMapping
    public Result<List<ResourceMeta>> list() {
        return Result.ok(bannerService.getList());
    }

    @Operation(summary = "获取单个轮播图")
    @GetMapping("/{order}")
    public Result<ResourceMeta> get(@PathVariable int order) {
        return Result.ok(bannerService.get(order));
    }

    @Operation(summary = "基于已存在资源添加轮播图")
    @PostMapping
    public Result<ResourceMeta> create(@RequestBody @Valid BannerCreateRequest request) {
        return Result.ok(createOrUpdate(request.resourceId(), request.hash(), true, null));
    }

    @Operation(summary = "完成上传并添加轮播图")
    @PostMapping("/complete-upload")
    public Result<ResourceMeta> createByUpload(@RequestBody @Valid MultipartUploadSessionRequest request) {
        return Result.ok(bannerService.create(request));
    }

    @Operation(summary = "更新指定顺序的轮播图")
    @PutMapping("/{order}")
    public Result<ResourceMeta> update(@PathVariable int order, @RequestBody @Valid BannerUpdateRequest request) {
        return Result.ok(createOrUpdate(request.resourceId(), request.hash(), false, order));
    }

    @Operation(summary = "删除轮播图")
    @DeleteMapping("/{order}")
    public Result<Boolean> delete(@PathVariable int order) {
        return Result.ok(bannerService.delete(order));
    }

    private ResourceMeta createOrUpdate(String resourceId, String hash, boolean create, Integer order) {
        if (StringUtils.isNotBlank(resourceId)) {
            return create ? bannerService.create(resourceId) : bannerService.update(order, resourceId);
        }
        if (StringUtils.isNotBlank(hash)) {
            return create ? bannerService.createByHash(hash) : bannerService.updateByHash(order, hash);
        }
        throw new BusinessException(4000, "resourceId和hash不能同时为空");
    }
}
