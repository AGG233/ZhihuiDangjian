package com.rauio.smartdangjian.server.content.comment.service;

import static com.rauio.smartdangjian.constants.ErrorConstants.RESOURCE_NOT_AUTHORIZED;
import static com.rauio.smartdangjian.server.content.comment.constants.ContentInteractionErrorConstants.COMMENT_CONTENT_EMPTY;
import static com.rauio.smartdangjian.server.content.comment.constants.ContentInteractionErrorConstants.COMMENT_CONTENT_TOO_LONG;
import static com.rauio.smartdangjian.server.content.comment.constants.ContentInteractionErrorConstants.COMMENT_DELETE_FORBIDDEN;
import static com.rauio.smartdangjian.server.content.comment.constants.ContentInteractionErrorConstants.COMMENT_NOT_FOUND;
import static com.rauio.smartdangjian.server.content.comment.constants.ContentInteractionErrorConstants.COMMENT_PARENT_NOT_FOUND;
import static com.rauio.smartdangjian.server.content.comment.constants.ContentInteractionErrorConstants.COMMENT_SAVE_FAILED;
import static com.rauio.smartdangjian.server.content.comment.constants.ContentInteractionErrorConstants.TARGET_NOT_FOUND;
import static com.rauio.smartdangjian.server.content.comment.constants.ContentInteractionErrorConstants.TARGET_TYPE_INVALID;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rauio.smartdangjian.exception.BusinessException;
import com.rauio.smartdangjian.server.content.comment.constants.InteractionTargetConstants;
import com.rauio.smartdangjian.server.content.comment.mapper.CommentMapper;
import com.rauio.smartdangjian.server.content.comment.mapper.InteractionTargetMapper;
import com.rauio.smartdangjian.server.content.comment.pojo.convertor.CommentConvertor;
import com.rauio.smartdangjian.server.content.comment.pojo.entity.Comment;
import com.rauio.smartdangjian.server.content.comment.pojo.request.CommentCreateRequest;
import com.rauio.smartdangjian.server.content.comment.pojo.response.CommentPageResponse;
import com.rauio.smartdangjian.server.content.comment.pojo.response.CommentResponse;
import com.rauio.smartdangjian.utils.SecurityUtils;
import com.rauio.smartdangjian.utils.spec.UserType;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CommentService extends ServiceImpl<CommentMapper, Comment> {

    /** 评论内容最大长度 */
    private static final int MAX_CONTENT_LENGTH = 500;

    /** 评论正常状态 */
    private static final int STATUS_NORMAL = 1;

    private final InteractionTargetMapper interactionTargetMapper;
    private final CommentConvertor commentConvertor;

    /**
     * 发表评论：校验目标类型/目标存在性/内容长度/父评论，随后落库。
     *
     * @param request 发表评论请求
     * @return 评论响应
     */
    public CommentResponse create(CommentCreateRequest request) {
        if (!InteractionTargetConstants.isValid(request.getTargetType())) {
            throw new BusinessException(TARGET_TYPE_INVALID, "评论目标类型非法");
        }
        assertTargetExists(request.getTargetType(), request.getTargetId());

        String content = request.getContent();
        if (content == null || content.isBlank()) {
            throw new BusinessException(COMMENT_CONTENT_EMPTY, "评论内容不能为空");
        }
        if (content.length() > MAX_CONTENT_LENGTH) {
            throw new BusinessException(COMMENT_CONTENT_TOO_LONG, "评论内容不能超过" + MAX_CONTENT_LENGTH + "字");
        }
        if (request.getParentId() != null) {
            Comment parent = this.getById(request.getParentId());
            if (parent == null) {
                throw new BusinessException(COMMENT_PARENT_NOT_FOUND, "父评论不存在");
            }
        }

        Comment comment = Comment.builder()
                .userId(currentUserId())
                .targetType(request.getTargetType())
                .targetId(request.getTargetId())
                .content(content)
                .parentId(request.getParentId())
                .status(STATUS_NORMAL)
                .build();
        if (!this.save(comment)) {
            throw new BusinessException(COMMENT_SAVE_FAILED, "评论保存失败");
        }
        return commentConvertor.toResponse(comment);
    }

    /**
     * 分页查询评论，按创建时间倒序。
     *
     * @param targetType 目标类型
     * @param targetId   目标ID
     * @param pageNum    页码（从1开始）
     * @param pageSize   每页大小
     * @return 评论分页响应
     */
    public CommentPageResponse getPage(String targetType, Long targetId, int pageNum, int pageSize) {
        if (!InteractionTargetConstants.isValid(targetType)) {
            throw new BusinessException(TARGET_TYPE_INVALID, "评论目标类型非法");
        }
        Page<Comment> page = this.page(
                new Page<>(pageNum, pageSize),
                new LambdaQueryWrapper<Comment>()
                        .eq(Comment::getTargetType, targetType)
                        .eq(Comment::getTargetId, targetId)
                        .orderByDesc(Comment::getCreatedAt));
        return CommentPageResponse.builder()
                .total(page.getTotal())
                .size(page.getSize())
                .current(page.getCurrent())
                .records(commentConvertor.toResponseList(page.getRecords()))
                .build();
    }

    /**
     * 删除评论：仅评论作者本人或 MANAGER 有权删除。
     *
     * @param id 评论ID
     */
    public void delete(Long id) {
        Comment comment = this.getById(id);
        if (comment == null) {
            throw new BusinessException(COMMENT_NOT_FOUND, "评论不存在");
        }
        String currentUserId = SecurityUtils.getCurrentUserId();
        boolean isOwner = currentUserId != null
                && comment.getUserId() != null
                && String.valueOf(comment.getUserId()).equals(currentUserId);
        boolean isManager = SecurityUtils.getCurrentUserType() == UserType.MANAGER;
        if (!isOwner && !isManager) {
            throw new BusinessException(COMMENT_DELETE_FORBIDDEN, "无权删除他人评论");
        }
        this.removeById(id);
    }

    /**
     * 校验互动目标（课程/文章）是否存在。
     *
     * @param targetType 目标类型
     * @param targetId   目标ID
     */
    private void assertTargetExists(String targetType, Long targetId) {
        long count = InteractionTargetConstants.COURSE.equals(targetType)
                ? interactionTargetMapper.countCourseById(targetId)
                : interactionTargetMapper.countArticleById(targetId);
        if (count <= 0) {
            throw new BusinessException(TARGET_NOT_FOUND, "评论目标不存在");
        }
    }

    /**
     * 获取当前登录用户 ID。
     *
     * @return 用户ID
     */
    private Long currentUserId() {
        String userId = SecurityUtils.getCurrentUserId();
        if (userId == null || userId.isBlank()) {
            throw new BusinessException(RESOURCE_NOT_AUTHORIZED, "请先登录");
        }
        return Long.valueOf(userId);
    }
}
