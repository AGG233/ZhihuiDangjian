package com.rauio.smartdangjian.server.resource.controller.user;

import com.rauio.smartdangjian.pojo.response.Result;
import com.rauio.smartdangjian.server.resource.pojo.response.BannerResourceResponse;
import com.rauio.smartdangjian.server.resource.service.BannerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "用户轮播图接口", description = "提供轮播图查询能力")
@RestController
@RequestMapping("/api/resource/banners")
@RequiredArgsConstructor
public class UserBannerController {

    private final BannerService bannerService;

    @Operation(summary = "获取轮播图列表")
    @GetMapping
    public Result<List<BannerResourceResponse>> list() {
        return Result.ok(bannerService.getUserList());
    }

    @Operation(summary = "获取单个轮播图")
    @GetMapping("/{order}")
    public Result<BannerResourceResponse> get(@PathVariable int order) {
        return Result.ok(bannerService.getUser(order));
    }
}
