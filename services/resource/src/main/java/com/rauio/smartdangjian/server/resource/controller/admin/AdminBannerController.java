package com.rauio.smartdangjian.server.resource.controller.admin;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.*;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.rauio.smartdangjian.exception.BusinessException;
import com.rauio.smartdangjian.pojo.response.Result;
import com.rauio.smartdangjian.security.RoleConstants;
import com.rauio.smartdangjian.server.resource.constants.ResourceErrorConstants;
import com.rauio.smartdangjian.server.resource.pojo.request.BannerCreateRequest;
import com.rauio.smartdangjian.server.resource.pojo.request.BannerUpdateRequest;
import com.rauio.smartdangjian.server.resource.pojo.response.ResourceMetaResponse;
import com.rauio.smartdangjian.server.resource.service.BannerService;

import cn.dev33.satoken.annotation.SaCheckRole;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "管理员轮播图接口", description = "提供轮播图查询、添加、更新、删除能力")
@RestController
@RequestMapping("/api/admin/resource/banners")
@RequiredArgsConstructor
@SaCheckRole(RoleConstants.MANAGER)
public class AdminBannerController {

    private final BannerService bannerService;

    @Operation(summary = "获取轮播图列表")
    @GetMapping
    public Result<List<ResourceMetaResponse>> list() {
        return Result.ok(
                bannerService.getList().stream().map(ResourceMetaResponse::from).toList());
    }

    @Operation(summary = "获取单个轮播图")
    @GetMapping("/{order}")
    public Result<ResourceMetaResponse> get(@PathVariable int order) {
        return Result.ok(ResourceMetaResponse.from(bannerService.get(order)));
    }

    @Operation(summary = "基于已存在资源添加轮播图")
    @PostMapping
    public Result<ResourceMetaResponse> create(@RequestBody @Valid BannerCreateRequest request) {
        return Result.ok(createOrUpdate(request.resourceId(), request.hash(), true, null));
    }

    @Operation(summary = "更新指定顺序的轮播图")
    @PutMapping("/{order}")
    public Result<ResourceMetaResponse> update(
            @PathVariable int order, @RequestBody @Valid BannerUpdateRequest request) {
        return Result.ok(createOrUpdate(request.resourceId(), request.hash(), false, order));
    }

    @Operation(summary = "删除轮播图")
    @DeleteMapping("/{order}")
    public Result<Boolean> delete(@PathVariable int order) {
        return Result.ok(bannerService.delete(order));
    }

    private ResourceMetaResponse createOrUpdate(String resourceId, String hash, boolean create, Integer order) {
        if (StringUtils.isNotBlank(resourceId)) {
            var meta = create ? bannerService.create(resourceId) : bannerService.update(order, resourceId);
            return ResourceMetaResponse.from(meta);
        }
        if (StringUtils.isNotBlank(hash)) {
            var meta = create ? bannerService.createByHash(hash) : bannerService.updateByHash(order, hash);
            return ResourceMetaResponse.from(meta);
        }
        throw new BusinessException(ResourceErrorConstants.BANNER_ID_AND_HASH_EMPTY, "resourceId和hash不能同时为空");
    }
}
