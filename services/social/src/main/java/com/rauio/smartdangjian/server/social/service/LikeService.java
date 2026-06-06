package com.rauio.smartdangjian.server.social.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rauio.smartdangjian.server.social.mapper.UserLikeMapper;
import com.rauio.smartdangjian.server.social.pojo.entity.UserLike;
import com.rauio.smartdangjian.server.social.pojo.response.LikeStatusResponse;
import com.rauio.smartdangjian.server.social.support.LikeTargetGateway;
import com.rauio.smartdangjian.server.social.support.LikeTargetType;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class LikeService extends ServiceImpl<UserLikeMapper, UserLike> {

    private final LikeTargetGateway likeTargetGateway;

    @Transactional
    public LikeStatusResponse toggle(Long userId, String targetType, Long targetId) {
        LikeTargetType type = LikeTargetType.parse(targetType);
        likeTargetGateway.requireExists(type, targetId);
        String targetTypeValue = type.value();

        LambdaQueryWrapper<UserLike> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserLike::getUserId, userId)
                .eq(UserLike::getTargetType, targetTypeValue)
                .eq(UserLike::getTargetId, targetId);
        UserLike existing = this.getOne(wrapper);
        if (existing != null) {
            this.removeById(existing.getId());
            likeTargetGateway.decrementLikeCount(type, targetId);
            return LikeStatusResponse.builder()
                    .liked(false)
                    .targetType(targetTypeValue)
                    .targetId(targetId)
                    .build();
        } else {
            UserLike like = UserLike.builder()
                    .userId(userId)
                    .targetType(targetTypeValue)
                    .targetId(targetId)
                    .build();
            this.save(like);
            likeTargetGateway.incrementLikeCount(type, targetId);
            return LikeStatusResponse.builder()
                    .liked(true)
                    .targetType(targetTypeValue)
                    .targetId(targetId)
                    .build();
        }
    }

    public LikeStatusResponse getStatus(Long userId, String targetType, Long targetId) {
        LikeTargetType type = LikeTargetType.parse(targetType);
        String targetTypeValue = type.value();

        LambdaQueryWrapper<UserLike> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserLike::getUserId, userId)
                .eq(UserLike::getTargetType, targetTypeValue)
                .eq(UserLike::getTargetId, targetId);
        boolean liked = this.count(wrapper) > 0;
        Integer likeCount = likeTargetGateway.getLikeCount(type, targetId);
        return LikeStatusResponse.builder()
                .liked(liked)
                .likeCount(likeCount)
                .targetType(targetTypeValue)
                .targetId(targetId)
                .build();
    }
}
