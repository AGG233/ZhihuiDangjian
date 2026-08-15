package com.rauio.smartdangjian.server.content.comment.service;

import static com.rauio.smartdangjian.constants.ErrorConstants.RESOURCE_NOT_AUTHORIZED;
import static com.rauio.smartdangjian.server.content.comment.constants.ContentInteractionErrorConstants.LIKE_TOGGLE_FAILED;
import static com.rauio.smartdangjian.server.content.comment.constants.ContentInteractionErrorConstants.TARGET_NOT_FOUND;
import static com.rauio.smartdangjian.server.content.comment.constants.ContentInteractionErrorConstants.TARGET_TYPE_INVALID;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rauio.smartdangjian.exception.BusinessException;
import com.rauio.smartdangjian.server.content.comment.constants.InteractionTargetConstants;
import com.rauio.smartdangjian.server.content.comment.mapper.InteractionTargetMapper;
import com.rauio.smartdangjian.server.content.comment.mapper.LikeRecordMapper;
import com.rauio.smartdangjian.server.content.comment.pojo.entity.LikeRecord;
import com.rauio.smartdangjian.server.content.comment.pojo.response.LikeStatusResponse;
import com.rauio.smartdangjian.utils.SecurityUtils;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LikeService extends ServiceImpl<LikeRecordMapper, LikeRecord> {

    private final InteractionTargetMapper interactionTargetMapper;

    /**
     * 切换点赞状态（幂等）：已赞则取消（删除记录），未赞则点赞（插入记录）。
     * 两次点击回到未赞状态，计数归位。
     *
     * @param targetType 目标类型
     * @param targetId   目标ID
     * @return 当前点赞状态 + 点赞总数
     */
    public LikeStatusResponse toggle(String targetType, Long targetId) {
        if (!InteractionTargetConstants.isValid(targetType)) {
            throw new BusinessException(TARGET_TYPE_INVALID, "点赞目标类型非法");
        }
        assertTargetExists(targetType, targetId);

        Long userId = currentUserId();
        LikeRecord existing = this.getOne(new LambdaQueryWrapper<LikeRecord>()
                .eq(LikeRecord::getUserId, userId)
                .eq(LikeRecord::getTargetType, targetType)
                .eq(LikeRecord::getTargetId, targetId));

        boolean liked;
        if (existing != null) {
            // 已点赞 → 取消
            this.removeById(existing.getId());
            liked = false;
        } else {
            // 未点赞 → 点赞
            LikeRecord record = LikeRecord.builder()
                    .userId(userId)
                    .targetType(targetType)
                    .targetId(targetId)
                    .build();
            try {
                if (!this.save(record)) {
                    throw new BusinessException(LIKE_TOGGLE_FAILED, "点赞失败");
                }
                liked = true;
            } catch (org.springframework.dao.DuplicateKeyException e) {
                // 并发双击：唯一约束 uk_like_user_target 冲突视为已点赞，回退为取消
                this.remove(new LambdaQueryWrapper<LikeRecord>()
                        .eq(LikeRecord::getUserId, userId)
                        .eq(LikeRecord::getTargetType, targetType)
                        .eq(LikeRecord::getTargetId, targetId));
                liked = false;
            }
        }
        long count = countByTarget(targetType, targetId);
        return LikeStatusResponse.builder().liked(liked).count(count).build();
    }

    /**
     * 获取点赞总数。
     *
     * @param targetType 目标类型
     * @param targetId   目标ID
     * @return 点赞总数
     */
    public long getCount(String targetType, Long targetId) {
        if (!InteractionTargetConstants.isValid(targetType)) {
            throw new BusinessException(TARGET_TYPE_INVALID, "点赞目标类型非法");
        }
        return countByTarget(targetType, targetId);
    }

    /**
     * 统计指定目标的点赞总数。
     *
     * @param targetType 目标类型
     * @param targetId   目标ID
     * @return 点赞总数
     */
    private long countByTarget(String targetType, Long targetId) {
        return this.count(new LambdaQueryWrapper<LikeRecord>()
                .eq(LikeRecord::getTargetType, targetType)
                .eq(LikeRecord::getTargetId, targetId));
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
            throw new BusinessException(TARGET_NOT_FOUND, "点赞目标不存在");
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
