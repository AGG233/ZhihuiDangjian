package com.rauio.smartdangjian.server.social.service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rauio.smartdangjian.exception.BusinessException;
import com.rauio.smartdangjian.pojo.response.SensitiveWordMatchResult;
import com.rauio.smartdangjian.server.social.constants.SocialErrorConstants;
import com.rauio.smartdangjian.server.social.mapper.CommentMapper;
import com.rauio.smartdangjian.server.social.pojo.entity.Comment;
import com.rauio.smartdangjian.server.social.pojo.request.CommentRequest;
import com.rauio.smartdangjian.server.social.pojo.response.CommentResponse;
import com.rauio.smartdangjian.server.user.pojo.entity.User;
import com.rauio.smartdangjian.server.user.service.UserService;
import com.rauio.smartdangjian.service.SensitiveWordService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommentService extends ServiceImpl<CommentMapper, Comment> {

    private final UserService userService;
    private final SensitiveWordService sensitiveWordService;

    public Page<CommentResponse> getPage(
            String targetType, Long targetId, Long parentId, int pageNum, int pageSize, String sortBy) {
        LambdaQueryWrapper<Comment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Comment::getTargetType, targetType)
                .eq(Comment::getTargetId, targetId)
                .ne(Comment::getStatus, "deleted")
                .ne(Comment::getStatus, "hidden");
        if (parentId != null) {
            wrapper.eq(Comment::getParentId, parentId);
        } else {
            wrapper.isNull(Comment::getParentId);
        }
        if ("hot".equals(sortBy)) {
            wrapper.orderByDesc(Comment::getLikeCount).orderByDesc(Comment::getCreatedAt);
        } else {
            wrapper.orderByDesc(Comment::getCreatedAt);
        }
        Page<Comment> page = this.page(new Page<>(pageNum, pageSize), wrapper);
        return toCommentResponsePage(page);
    }

    public CommentResponse create(Long userId, CommentRequest request) {
        SensitiveWordMatchResult safetyResult = sensitiveWordService.check(request.getContent());
        String status = safetyResult.isMatched() ? "pending_review" : "published";

        Comment comment = new Comment();
        comment.setTargetType(request.getTargetType());
        comment.setTargetId(request.getTargetId());
        comment.setUserId(userId);
        comment.setParentId(request.getParentId());
        comment.setContent(request.getContent());
        comment.setStatus(status);
        comment.setLikeCount(0);
        comment.setReplyCount(0);
        this.save(comment);

        if (request.getParentId() != null) {
            var updateWrapper = new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<Comment>()
                    .eq(Comment::getId, request.getParentId())
                    .setSql("reply_count = reply_count + 1");
            this.baseMapper.update(null, updateWrapper);
        }

        log.info(
                "评论创建 targetType={} targetId={} userId={} status={}",
                request.getTargetType(),
                request.getTargetId(),
                userId,
                status);
        User user = userService.getById(userId);
        return toCommentResponse(comment, Map.of(userId, user));
    }

    public void delete(Long commentId, Long userId) {
        Comment comment = this.getById(commentId);
        if (comment == null) {
            throw new BusinessException(SocialErrorConstants.COMMENT_NOT_FOUND, "评论不存在");
        }
        if (!comment.getUserId().equals(userId)) {
            throw new BusinessException(SocialErrorConstants.COMMENT_PERMISSION_DENIED, "无权删除他人评论");
        }
        comment.setStatus("deleted");
        this.updateById(comment);
    }

    private CommentResponse toCommentResponse(Comment comment, Map<Long, User> userMap) {
        User user = userMap.get(comment.getUserId());
        return CommentResponse.builder()
                .id(comment.getId())
                .targetType(comment.getTargetType())
                .targetId(comment.getTargetId())
                .userId(comment.getUserId())
                .username(user != null ? user.getUsername() : "")
                .realName(user != null ? user.getRealName() : "")
                .parentId(comment.getParentId())
                .content(comment.getContent())
                .status(comment.getStatus())
                .likeCount(comment.getLikeCount())
                .replyCount(comment.getReplyCount())
                .createdAt(comment.getCreatedAt())
                .replies(List.of())
                .build();
    }

    private Page<CommentResponse> toCommentResponsePage(Page<Comment> page) {
        if (page.getRecords().isEmpty()) {
            Page<CommentResponse> empty = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
            empty.setRecords(List.of());
            return empty;
        }
        Set<Long> userIds = page.getRecords().stream().map(Comment::getUserId).collect(Collectors.toSet());
        Map<Long, User> userMap =
                userService.listByIds(userIds).stream().collect(Collectors.toMap(User::getId, u -> u, (a, b) -> a));
        List<CommentResponse> list = page.getRecords().stream()
                .map(comment -> toCommentResponse(comment, userMap))
                .toList();
        Page<CommentResponse> result = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        result.setRecords(list);
        return result;
    }
}
