package com.rauio.smartdangjian.server.social.support;

public interface LikeTargetGateway {

    void requireExists(LikeTargetType targetType, Long targetId);

    void incrementLikeCount(LikeTargetType targetType, Long targetId);

    void decrementLikeCount(LikeTargetType targetType, Long targetId);

    Integer getLikeCount(LikeTargetType targetType, Long targetId);
}
