package com.rauio.smartdangjian.server.content.comment.controller.user;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.rauio.smartdangjian.pojo.response.Result;
import com.rauio.smartdangjian.server.content.comment.pojo.response.LikeStatusResponse;
import com.rauio.smartdangjian.server.content.comment.service.LikeService;

import cn.dev33.satoken.annotation.SaCheckRole;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "用户点赞接口", description = "点赞切换与计数查询")
@RestController
@RequestMapping("/api/content/likes")
@RequiredArgsConstructor
@SaCheckRole("STUDENT")
public class UserLikeController {

    private final LikeService likeService;

    @Operation(summary = "切换点赞状态", description = "已赞则取消，未赞则点赞，返回当前状态与点赞总数")
    @PostMapping("/toggle")
    public Result<LikeStatusResponse> toggle(
            @Parameter(name = "targetType", description = "目标类型: course=课程, article=文章") @RequestParam
                    String targetType,
            @Parameter(name = "targetId", description = "目标ID") @RequestParam Long targetId) {
        return Result.ok(likeService.toggle(targetType, targetId));
    }

    @Operation(summary = "获取点赞总数", description = "查询指定目标的点赞总数")
    @GetMapping("/count")
    public Result<Long> getCount(
            @Parameter(name = "targetType", description = "目标类型: course=课程, article=文章") @RequestParam
                    String targetType,
            @Parameter(name = "targetId", description = "目标ID") @RequestParam Long targetId) {
        return Result.ok(likeService.getCount(targetType, targetId));
    }
}
