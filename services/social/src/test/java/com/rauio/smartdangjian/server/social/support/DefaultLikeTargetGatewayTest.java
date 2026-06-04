package com.rauio.smartdangjian.server.social.support;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.rauio.smartdangjian.exception.BusinessException;
import com.rauio.smartdangjian.server.content.mapper.ArticleMapper;
import com.rauio.smartdangjian.server.content.pojo.entity.Article;
import com.rauio.smartdangjian.server.course.mapper.CourseMapper;
import com.rauio.smartdangjian.server.course.pojo.entity.Course;
import com.rauio.smartdangjian.server.social.constants.SocialErrorConstants;
import com.rauio.smartdangjian.server.social.mapper.CommentMapper;
import com.rauio.smartdangjian.server.social.pojo.entity.Comment;

@ExtendWith(MockitoExtension.class)
class DefaultLikeTargetGatewayTest {

    @Mock
    private CommentMapper commentMapper;

    @Mock
    private ArticleMapper articleMapper;

    @Mock
    private CourseMapper courseMapper;

    @InjectMocks
    private DefaultLikeTargetGateway gateway;

    @Test
    @DisplayName("getLikeCount - comment/article/course 读取对应目标计数")
    void getLikeCount() {
        when(commentMapper.selectById(1L))
                .thenReturn(Comment.builder().id(1L).likeCount(3).build());
        when(articleMapper.selectById(2L))
                .thenReturn(Article.builder().id(2L).likeCount(5).build());
        when(courseMapper.selectById(3L))
                .thenReturn(Course.builder().id(3L).likeCount(7).build());

        assertThat(gateway.getLikeCount(LikeTargetType.COMMENT, 1L)).isEqualTo(3);
        assertThat(gateway.getLikeCount(LikeTargetType.ARTICLE, 2L)).isEqualTo(5);
        assertThat(gateway.getLikeCount(LikeTargetType.COURSE, 3L)).isEqualTo(7);
    }

    @Test
    @DisplayName("requireExists - 不存在目标抛业务异常")
    void requireExistsThrowsWhenTargetNotFound() {
        when(articleMapper.selectById(99L)).thenReturn(null);

        assertThatThrownBy(() -> gateway.requireExists(LikeTargetType.ARTICLE, 99L))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getCode())
                        .isEqualTo(SocialErrorConstants.LIKE_TARGET_NOT_FOUND));
    }

    @Test
    @DisplayName("increment/decrement - 委托对应 mapper 更新计数")
    void updateLikeCount() {
        gateway.incrementLikeCount(LikeTargetType.COMMENT, 1L);
        gateway.decrementLikeCount(LikeTargetType.ARTICLE, 2L);
        gateway.incrementLikeCount(LikeTargetType.COURSE, 3L);

        verify(commentMapper).update(any(), any());
        verify(articleMapper).update(any(), any());
        verify(courseMapper).update(any(), any());
    }
}
