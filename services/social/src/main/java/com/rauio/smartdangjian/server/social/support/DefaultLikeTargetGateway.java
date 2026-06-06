package com.rauio.smartdangjian.server.social.support;

import org.springframework.stereotype.Component;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.rauio.smartdangjian.exception.BusinessException;
import com.rauio.smartdangjian.server.content.api.ArticleQueryFacade;
import com.rauio.smartdangjian.server.content.mapper.ArticleMapper;
import com.rauio.smartdangjian.server.content.pojo.entity.Article;
import com.rauio.smartdangjian.server.course.api.CourseQueryFacade;
import com.rauio.smartdangjian.server.course.mapper.CourseMapper;
import com.rauio.smartdangjian.server.course.pojo.entity.Course;
import com.rauio.smartdangjian.server.social.constants.SocialErrorConstants;
import com.rauio.smartdangjian.server.social.mapper.CommentMapper;
import com.rauio.smartdangjian.server.social.pojo.entity.Comment;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class DefaultLikeTargetGateway implements LikeTargetGateway {

    private final CommentMapper commentMapper;
    private final ArticleMapper articleMapper;
    private final CourseMapper courseMapper;
    private final ArticleQueryFacade articleQueryFacade;
    private final CourseQueryFacade courseQueryFacade;

    @Override
    public void requireExists(LikeTargetType targetType, Long targetId) {
        getLikeCount(targetType, targetId);
    }

    @Override
    public void incrementLikeCount(LikeTargetType targetType, Long targetId) {
        switch (targetType) {
            case COMMENT ->
                commentMapper.update(
                        null,
                        new LambdaUpdateWrapper<Comment>()
                                .eq(Comment::getId, targetId)
                                .setSql("like_count = like_count + 1"));
            case ARTICLE ->
                articleMapper.update(
                        null,
                        new LambdaUpdateWrapper<Article>()
                                .eq(Article::getId, targetId)
                                .setSql("like_count = like_count + 1"));
            case COURSE ->
                courseMapper.update(
                        null,
                        new LambdaUpdateWrapper<Course>()
                                .eq(Course::getId, targetId)
                                .setSql("like_count = like_count + 1"));
        }
    }

    @Override
    public void decrementLikeCount(LikeTargetType targetType, Long targetId) {
        switch (targetType) {
            case COMMENT ->
                commentMapper.update(
                        null,
                        new LambdaUpdateWrapper<Comment>()
                                .eq(Comment::getId, targetId)
                                .apply("like_count > 0")
                                .setSql("like_count = like_count - 1"));
            case ARTICLE ->
                articleMapper.update(
                        null,
                        new LambdaUpdateWrapper<Article>()
                                .eq(Article::getId, targetId)
                                .apply("like_count > 0")
                                .setSql("like_count = like_count - 1"));
            case COURSE ->
                courseMapper.update(
                        null,
                        new LambdaUpdateWrapper<Course>()
                                .eq(Course::getId, targetId)
                                .apply("like_count > 0")
                                .setSql("like_count = like_count - 1"));
        }
    }

    @Override
    public Integer getLikeCount(LikeTargetType targetType, Long targetId) {
        return switch (targetType) {
            case COMMENT -> likeCountOf(commentMapper.selectById(targetId));
            case ARTICLE -> likeCountOf(articleMapper.selectById(targetId));
            case COURSE -> likeCountOf(courseMapper.selectById(targetId));
        };
    }

    private Integer likeCountOf(Comment comment) {
        if (comment == null) {
            throw targetNotFound();
        }
        return comment.getLikeCount();
    }

    private Integer likeCountOf(Article article) {
        if (article == null) {
            throw targetNotFound();
        }
        return article.getLikeCount();
    }

    private Integer likeCountOf(Course course) {
        if (course == null) {
            throw targetNotFound();
        }
        return course.getLikeCount();
    }

    private BusinessException targetNotFound() {
        return new BusinessException(SocialErrorConstants.LIKE_TARGET_NOT_FOUND, "点赞目标不存在");
    }
}
