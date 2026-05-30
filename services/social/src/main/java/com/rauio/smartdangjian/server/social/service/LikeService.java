package com.rauio.smartdangjian.server.social.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rauio.smartdangjian.exception.BusinessException;
import com.rauio.smartdangjian.server.content.mapper.ArticleMapper;
import com.rauio.smartdangjian.server.content.mapper.CourseMapper;
import com.rauio.smartdangjian.server.content.pojo.entity.Article;
import com.rauio.smartdangjian.server.content.pojo.entity.Course;
import com.rauio.smartdangjian.server.social.constants.SocialErrorConstants;
import com.rauio.smartdangjian.server.social.mapper.CommentMapper;
import com.rauio.smartdangjian.server.social.mapper.UserLikeMapper;
import com.rauio.smartdangjian.server.social.pojo.entity.Comment;
import com.rauio.smartdangjian.server.social.pojo.entity.UserLike;
import com.rauio.smartdangjian.server.social.pojo.response.LikeStatusResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class LikeService extends ServiceImpl<UserLikeMapper, UserLike> {

    private final CommentMapper commentMapper;
    private final ArticleMapper articleMapper;
    private final CourseMapper courseMapper;

    @Transactional
    public LikeStatusResponse toggle(Long userId, String targetType, Long targetId) {
        LambdaQueryWrapper<UserLike> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserLike::getUserId, userId)
                .eq(UserLike::getTargetType, targetType)
                .eq(UserLike::getTargetId, targetId);
        UserLike existing = this.getOne(wrapper);
        if (existing != null) {
            this.removeById(existing.getId());
            decrementLikeCount(targetType, targetId);
            return LikeStatusResponse.builder()
                    .liked(false)
                    .targetType(targetType)
                    .targetId(targetId)
                    .build();
        } else {
            UserLike like = UserLike.builder()
                    .userId(userId)
                    .targetType(targetType)
                    .targetId(targetId)
                    .build();
            this.save(like);
            incrementLikeCount(targetType, targetId);
            return LikeStatusResponse.builder()
                    .liked(true)
                    .targetType(targetType)
                    .targetId(targetId)
                    .build();
        }
    }

    public LikeStatusResponse getStatus(Long userId, String targetType, Long targetId) {
        LambdaQueryWrapper<UserLike> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserLike::getUserId, userId)
                .eq(UserLike::getTargetType, targetType)
                .eq(UserLike::getTargetId, targetId);
        boolean liked = this.count(wrapper) > 0;
        Integer likeCount = getLikeCount(targetType, targetId);
        return LikeStatusResponse.builder()
                .liked(liked)
                .likeCount(likeCount)
                .targetType(targetType)
                .targetId(targetId)
                .build();
    }

    private void incrementLikeCount(String targetType, Long targetId) {
        if ("comment".equals(targetType)) {
            var wrapper = new LambdaUpdateWrapper<Comment>()
                    .eq(Comment::getId, targetId)
                    .setSql("like_count = like_count + 1");
            commentMapper.update(null, wrapper);
        } else if ("article".equals(targetType)) {
            var wrapper = new LambdaUpdateWrapper<Article>()
                    .eq(Article::getId, targetId)
                    .setSql("like_count = like_count + 1");
            articleMapper.update(null, wrapper);
        } else if ("course".equals(targetType)) {
            var wrapper = new LambdaUpdateWrapper<Course>()
                    .eq(Course::getId, targetId)
                    .setSql("like_count = like_count + 1");
            courseMapper.update(null, wrapper);
        }
    }

    private void decrementLikeCount(String targetType, Long targetId) {
        if ("comment".equals(targetType)) {
            var wrapper = new LambdaUpdateWrapper<Comment>()
                    .eq(Comment::getId, targetId)
                    .apply("like_count > 0")
                    .setSql("like_count = like_count - 1");
            commentMapper.update(null, wrapper);
        } else if ("article".equals(targetType)) {
            var wrapper = new LambdaUpdateWrapper<Article>()
                    .eq(Article::getId, targetId)
                    .apply("like_count > 0")
                    .setSql("like_count = like_count - 1");
            articleMapper.update(null, wrapper);
        } else if ("course".equals(targetType)) {
            var wrapper = new LambdaUpdateWrapper<Course>()
                    .eq(Course::getId, targetId)
                    .apply("like_count > 0")
                    .setSql("like_count = like_count - 1");
            courseMapper.update(null, wrapper);
        }
    }

    private Integer getLikeCount(String targetType, Long targetId) {
        if ("comment".equals(targetType)) {
            Comment comment = commentMapper.selectById(targetId);
            if (comment == null) {
                throw new BusinessException(SocialErrorConstants.LIKE_TARGET_NOT_FOUND, "点赞目标不存在");
            }
            return comment.getLikeCount();
        } else if ("article".equals(targetType)) {
            Article article = articleMapper.selectById(targetId);
            if (article == null) {
                throw new BusinessException(SocialErrorConstants.LIKE_TARGET_NOT_FOUND, "点赞目标不存在");
            }
            return article.getLikeCount();
        } else if ("course".equals(targetType)) {
            Course course = courseMapper.selectById(targetId);
            if (course == null) {
                throw new BusinessException(SocialErrorConstants.LIKE_TARGET_NOT_FOUND, "点赞目标不存在");
            }
            return course.getLikeCount();
        }
        return 0;
    }
}
