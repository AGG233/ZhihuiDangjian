package com.rauio.smartdangjian.server.social.controller.user;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rauio.smartdangjian.pojo.response.Result;
import com.rauio.smartdangjian.server.social.pojo.request.CommentRequest;
import com.rauio.smartdangjian.server.social.pojo.response.CommentResponse;
import com.rauio.smartdangjian.server.social.pojo.response.LikeStatusResponse;
import com.rauio.smartdangjian.server.social.service.CommentService;
import com.rauio.smartdangjian.server.social.service.LikeService;
import com.rauio.smartdangjian.server.user.service.UserService;

import cn.dev33.satoken.annotation.SaCheckRole;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "社交互动接口", description = "评论和点赞功能")
@RestController
@RequestMapping("/api/social")
@RequiredArgsConstructor
@SaCheckRole("STUDENT")
public class UserSocialController {

    private final CommentService commentService;
    private final LikeService likeService;
    private final UserService userService;

    @Operation(summary = "获取评论列表")
    @GetMapping("/{targetType}/{targetId}/comments")
    public Result<Page<CommentResponse>> getComments(
            @PathVariable String targetType,
            @PathVariable Long targetId,
            @RequestParam(required = false) Long parentId,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(defaultValue = "latest") String sortBy) {
        return Result.ok(commentService.getPage(targetType, targetId, parentId, pageNum, pageSize, sortBy));
    }

    @Operation(summary = "发表评论")
    @PostMapping("/{targetType}/{targetId}/comments")
    public Result<CommentResponse> createComment(
            @PathVariable String targetType, @PathVariable Long targetId, @RequestBody @Valid CommentRequest request) {
        request.setTargetType(targetType);
        request.setTargetId(targetId);
        Long userId = Long.valueOf(userService.getCurrentUserId());
        return Result.ok(commentService.create(userId, request));
    }

    @Operation(summary = "回复评论")
    @PostMapping("/comments/{commentId}/replies")
    public Result<CommentResponse> reply(@PathVariable Long commentId, @RequestBody @Valid CommentRequest request) {
        request.setParentId(commentId);
        Long userId = Long.valueOf(userService.getCurrentUserId());
        return Result.ok(commentService.create(userId, request));
    }

    @Operation(summary = "删除评论")
    @DeleteMapping("/comments/{commentId}")
    public Result<Void> deleteComment(@PathVariable Long commentId) {
        Long userId = Long.valueOf(userService.getCurrentUserId());
        commentService.delete(commentId, userId);
        return Result.ok();
    }

    @Operation(summary = "点赞/取消点赞")
    @PostMapping("/{targetType}/{targetId}/like")
    public Result<LikeStatusResponse> toggleLike(@PathVariable String targetType, @PathVariable Long targetId) {
        Long userId = Long.valueOf(userService.getCurrentUserId());
        return Result.ok(likeService.toggle(userId, targetType, targetId));
    }

    @Operation(summary = "查询点赞状态")
    @GetMapping("/{targetType}/{targetId}/like/status")
    public Result<LikeStatusResponse> getLikeStatus(@PathVariable String targetType, @PathVariable Long targetId) {
        Long userId = Long.valueOf(userService.getCurrentUserId());
        return Result.ok(likeService.getStatus(userId, targetType, targetId));
    }
}
