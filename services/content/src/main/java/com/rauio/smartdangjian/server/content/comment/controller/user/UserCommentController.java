package com.rauio.smartdangjian.server.content.comment.controller.user;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.rauio.smartdangjian.pojo.response.Result;
import com.rauio.smartdangjian.server.content.comment.pojo.request.CommentCreateRequest;
import com.rauio.smartdangjian.server.content.comment.pojo.response.CommentPageResponse;
import com.rauio.smartdangjian.server.content.comment.pojo.response.CommentResponse;
import com.rauio.smartdangjian.server.content.comment.service.CommentService;

import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.annotation.SaMode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "用户评论接口", description = "评论的发表、分页查询与删除")
@RestController
@RequestMapping("/api/content/comments")
@RequiredArgsConstructor
public class UserCommentController {

    private final CommentService commentService;

    @Operation(summary = "发表评论", description = "对课程或文章发表评论，支持回复（parentId）")
    @PostMapping
    @SaCheckRole("STUDENT")
    public Result<CommentResponse> create(@RequestBody CommentCreateRequest request) {
        return Result.ok(commentService.create(request));
    }

    @Operation(summary = "分页查询评论", description = "按目标类型与目标ID分页查询评论，按时间倒序")
    @GetMapping
    @SaCheckRole("STUDENT")
    public Result<CommentPageResponse> getPage(
            @Parameter(name = "targetType", description = "目标类型: course=课程, article=文章") @RequestParam
                    String targetType,
            @Parameter(name = "targetId", description = "目标ID") @RequestParam Long targetId,
            @Parameter(name = "pageNum", description = "页码，默认1") @RequestParam(defaultValue = "1") int pageNum,
            @Parameter(name = "pageSize", description = "每页大小，默认10") @RequestParam(defaultValue = "10") int pageSize) {
        return Result.ok(commentService.getPage(targetType, targetId, pageNum, pageSize));
    }

    @Operation(summary = "删除评论", description = "仅评论作者本人或管理员可删除")
    @DeleteMapping("/{id}")
    @SaCheckRole(
            value = {"STUDENT", "MANAGER"},
            mode = SaMode.OR)
    public Result<Void> delete(@Parameter(name = "id", description = "评论ID") @PathVariable Long id) {
        commentService.delete(id);
        return Result.ok();
    }
}
