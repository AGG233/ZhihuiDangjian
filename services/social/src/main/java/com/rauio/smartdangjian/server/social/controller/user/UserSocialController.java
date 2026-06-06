package com.rauio.smartdangjian.server.social.controller.user;

import static com.rauio.smartdangjian.constants.ValidationConstants.PAGE_NUM_MIN;
import static com.rauio.smartdangjian.constants.ValidationConstants.PAGE_SIZE_MAX;
import static com.rauio.smartdangjian.constants.ValidationConstants.PAGE_SIZE_MIN;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import org.springframework.validation.annotation.Validated;
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
import com.rauio.smartdangjian.security.CurrentUserProvider;
import com.rauio.smartdangjian.security.RoleConstants;
import com.rauio.smartdangjian.server.social.pojo.request.CommentRequest;
import com.rauio.smartdangjian.server.social.pojo.response.CommentResponse;
import com.rauio.smartdangjian.server.social.pojo.response.LikeStatusResponse;
import com.rauio.smartdangjian.server.social.service.CommentService;
import com.rauio.smartdangjian.server.social.service.LikeService;

import cn.dev33.satoken.annotation.SaCheckRole;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "社交互动接口", description = "评论和点赞功能")
@RestController
@RequestMapping("/api/social")
@RequiredArgsConstructor
@SaCheckRole(RoleConstants.STUDENT)
@Validated
public class UserSocialController {

    private final CommentService commentService;
    private final LikeService likeService;
    private final CurrentUserProvider currentUserProvider;

    @Operation(summary = "获取评论列表")
    @GetMapping("/{targetType}/{targetId}/comments")
    public Result<Page<CommentResponse>> getComments(
            @PathVariable String targetType,
            @PathVariable Long targetId,
            @RequestParam(required = false) Long parentId,
            @RequestParam(defaultValue = "1") @Min(PAGE_NUM_MIN) int pageNum,
            @RequestParam(defaultValue = "20") @Min(PAGE_SIZE_MIN) @Max(PAGE_SIZE_MAX) int pageSize,
            @RequestParam(defaultValue = "latest") String sortBy) {
        return Result.ok(commentService.getPage(targetType, targetId, parentId, pageNum, pageSize, sortBy));
    }

    @Operation(summary = "发表评论")
    @PostMapping("/{targetType}/{targetId}/comments")
    public Result<CommentResponse> createComment(
            @PathVariable String targetType, @PathVariable Long targetId, @RequestBody @Valid CommentRequest request) {
        request.setTargetType(targetType);
        request.setTargetId(targetId);
        Long userId = currentUserId();
        return Result.ok(commentService.create(userId, request));
    }

    @Operation(summary = "回复评论")
    @PostMapping("/comments/{commentId}/replies")
    public Result<CommentResponse> reply(@PathVariable Long commentId, @RequestBody @Valid CommentRequest request) {
        request.setParentId(commentId);
        Long userId = currentUserId();
        return Result.ok(commentService.create(userId, request));
    }

    @Operation(summary = "删除评论")
    @DeleteMapping("/comments/{commentId}")
    public Result<Void> deleteComment(@PathVariable Long commentId) {
        Long userId = currentUserId();
        commentService.delete(commentId, userId);
        return Result.ok();
    }

    @Operation(summary = "点赞/取消点赞")
    @PostMapping("/{targetType}/{targetId}/like")
    public Result<LikeStatusResponse> toggleLike(@PathVariable String targetType, @PathVariable Long targetId) {
        Long userId = currentUserId();
        return Result.ok(likeService.toggle(userId, targetType, targetId));
    }

    @Operation(summary = "查询点赞状态")
    @GetMapping("/{targetType}/{targetId}/like/status")
    public Result<LikeStatusResponse> getLikeStatus(@PathVariable String targetType, @PathVariable Long targetId) {
        Long userId = currentUserId();
        return Result.ok(likeService.getStatus(userId, targetType, targetId));
    }

    private Long currentUserId() {
        return Long.valueOf(currentUserProvider.getCurrentUserId());
    }
}
