package com.rauio.smartdangjian.server.social.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
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

@ExtendWith(MockitoExtension.class)
class LikeServiceTest {

    @Mock
    private UserLikeMapper userLikeMapper;

    @Mock
    private CommentMapper commentMapper;

    @Mock
    private ArticleMapper articleMapper;

    @Mock
    private CourseMapper courseMapper;

    @Spy
    @InjectMocks
    private LikeService likeService;

    @BeforeEach
    void resetSpy() {
        reset(likeService);
    }

    @Test
    @DisplayName("toggle - 点赞（无现有记录）")
    void toggleLikeWhenNoExistingRecord() {
        doReturn(null).when(likeService).getOne(any(LambdaQueryWrapper.class));
        doReturn(true).when(likeService).save(any(UserLike.class));
        when(commentMapper.update(any(), any())).thenReturn(1);

        LikeStatusResponse result = likeService.toggle(1L, "comment", 100L);

        assertThat(result.isLiked()).isTrue();
        assertThat(result.getTargetType()).isEqualTo("comment");
        assertThat(result.getTargetId()).isEqualTo(100L);
        verify(likeService).save(any(UserLike.class));
        verify(commentMapper).update(any(), any());
    }

    @Test
    @DisplayName("toggle - 取消点赞（有现有记录）")
    void toggleUnlikeWhenExistingRecord() {
        UserLike existing = UserLike.builder()
                .id(1L)
                .userId(1L)
                .targetType("comment")
                .targetId(100L)
                .build();

        doReturn(existing).when(likeService).getOne(any(LambdaQueryWrapper.class));
        doReturn(true).when(likeService).removeById(1L);
        when(commentMapper.update(any(), any())).thenReturn(1);

        LikeStatusResponse result = likeService.toggle(1L, "comment", 100L);

        assertThat(result.isLiked()).isFalse();
        assertThat(result.getTargetType()).isEqualTo("comment");
        assertThat(result.getTargetId()).isEqualTo(100L);
        verify(likeService).removeById(1L);
        verify(commentMapper).update(any(), any());
    }

    @Test
    @DisplayName("toggle - 点赞时 like_count +1")
    void toggleLikeIncrementsLikeCount() {
        doReturn(null).when(likeService).getOne(any(LambdaQueryWrapper.class));
        doReturn(true).when(likeService).save(any(UserLike.class));
        when(commentMapper.update(any(), any())).thenReturn(1);

        likeService.toggle(1L, "comment", 100L);

        verify(commentMapper).update(any(), any());
    }

    @Test
    @DisplayName("toggle - 取消点赞时 like_count -1")
    void toggleUnlikeDecrementsLikeCount() {
        UserLike existing = UserLike.builder()
                .id(1L)
                .userId(1L)
                .targetType("comment")
                .targetId(100L)
                .build();

        doReturn(existing).when(likeService).getOne(any(LambdaQueryWrapper.class));
        doReturn(true).when(likeService).removeById(1L);
        when(commentMapper.update(any(), any())).thenReturn(1);

        likeService.toggle(1L, "comment", 100L);

        verify(commentMapper).update(any(), any());
    }

    @Test
    @DisplayName("toggle - 非 comment 类型不操作 like_count")
    void toggleNonCommentTypeDoesNotTouchLikeCount() {
        doReturn(null).when(likeService).getOne(any(LambdaQueryWrapper.class));
        doReturn(true).when(likeService).save(any(UserLike.class));

        LikeStatusResponse result = likeService.toggle(1L, "article", 100L);

        assertThat(result.isLiked()).isTrue();
        verify(commentMapper, never()).update(any(), any());
    }

    @Test
    @DisplayName("getStatus - 已点赞状态")
    void getStatusWhenLiked() {
        doReturn(1L).when(likeService).count(any(LambdaQueryWrapper.class));

        Comment comment = Comment.builder().id(100L).likeCount(42).build();
        when(commentMapper.selectById(100L)).thenReturn(comment);

        LikeStatusResponse result = likeService.getStatus(1L, "comment", 100L);

        assertThat(result.isLiked()).isTrue();
        assertThat(result.getLikeCount()).isEqualTo(42);
        assertThat(result.getTargetType()).isEqualTo("comment");
        assertThat(result.getTargetId()).isEqualTo(100L);
    }

    @Test
    @DisplayName("getStatus - 未点赞状态")
    void getStatusWhenNotLiked() {
        doReturn(0L).when(likeService).count(any(LambdaQueryWrapper.class));

        Comment comment = Comment.builder().id(100L).likeCount(0).build();
        when(commentMapper.selectById(100L)).thenReturn(comment);

        LikeStatusResponse result = likeService.getStatus(1L, "comment", 100L);

        assertThat(result.isLiked()).isFalse();
        assertThat(result.getLikeCount()).isEqualTo(0);
    }

    @Test
    @DisplayName("getStatus - 目标不存在抛异常")
    void getStatusWhenTargetNotFoundThrowsException() {
        doReturn(0L).when(likeService).count(any(LambdaQueryWrapper.class));
        when(commentMapper.selectById(999L)).thenReturn(null);

        assertThatThrownBy(() -> likeService.getStatus(1L, "comment", 999L))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> {
                    BusinessException be = (BusinessException) ex;
                    assertThat(be.getCode()).isEqualTo(SocialErrorConstants.LIKE_TARGET_NOT_FOUND);
                });
    }

    @Test
    @DisplayName("toggle - DB save 异常抛 RuntimeException")
    void toggleWhenSaveThrowsException() {
        doReturn(null).when(likeService).getOne(any(LambdaQueryWrapper.class));
        doThrow(new RuntimeException("DB保存失败")).when(likeService).save(any(UserLike.class));

        assertThatThrownBy(() -> likeService.toggle(1L, "comment", 100L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("DB保存失败");
    }

    @Test
    @DisplayName("toggle - DB removeById 异常抛 RuntimeException")
    void toggleWhenRemoveThrowsException() {
        UserLike existing = UserLike.builder()
                .id(1L)
                .userId(1L)
                .targetType("comment")
                .targetId(100L)
                .build();

        doReturn(existing).when(likeService).getOne(any(LambdaQueryWrapper.class));
        doThrow(new RuntimeException("DB删除失败")).when(likeService).removeById(1L);

        assertThatThrownBy(() -> likeService.toggle(1L, "comment", 100L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("DB删除失败");
    }

    @Test
    @DisplayName("toggle - null targetId")
    void toggleWithNullTargetId() {
        doReturn(null).when(likeService).getOne(any(LambdaQueryWrapper.class));
        doReturn(true).when(likeService).save(any(UserLike.class));

        LikeStatusResponse result = likeService.toggle(1L, "comment", null);

        assertThat(result.isLiked()).isTrue();
        assertThat(result.getTargetId()).isNull();
    }

    @Test
    @DisplayName("toggle - null targetType")
    void toggleWithNullTargetType() {
        doReturn(null).when(likeService).getOne(any(LambdaQueryWrapper.class));
        doReturn(true).when(likeService).save(any(UserLike.class));

        LikeStatusResponse result = likeService.toggle(1L, null, 100L);

        assertThat(result.isLiked()).isTrue();
        assertThat(result.getTargetType()).isNull();
    }

    @Test
    @DisplayName("toggle - 连续切换验证状态翻转")
    void toggleRepeatedlyFlipsState() {
        // 第一次：不存在 → 点赞
        doReturn(null).when(likeService).getOne(any(LambdaQueryWrapper.class));
        doReturn(true).when(likeService).save(any(UserLike.class));
        when(commentMapper.update(any(), any())).thenReturn(1);

        LikeStatusResponse first = likeService.toggle(1L, "comment", 100L);
        assertThat(first.isLiked()).isTrue();
        verify(likeService).save(any(UserLike.class));

        // 第二次：已存在 → 取消点赞
        UserLike existing = UserLike.builder()
                .id(1L)
                .userId(1L)
                .targetType("comment")
                .targetId(100L)
                .build();
        doReturn(existing).when(likeService).getOne(any(LambdaQueryWrapper.class));
        doReturn(true).when(likeService).removeById(1L);

        LikeStatusResponse second = likeService.toggle(1L, "comment", 100L);
        assertThat(second.isLiked()).isFalse();
        verify(likeService).removeById(1L);
    }

    @Test
    @DisplayName("toggle - article 类型点赞 article like_count +1")
    void toggleLikeArticleIncrementsArticleLikeCount() {
        doReturn(null).when(likeService).getOne(any(LambdaQueryWrapper.class));
        doReturn(true).when(likeService).save(any(UserLike.class));
        when(articleMapper.update(any(), any())).thenReturn(1);

        LikeStatusResponse result = likeService.toggle(1L, "article", 100L);

        assertThat(result.isLiked()).isTrue();
        verify(articleMapper).update(any(), any());
    }

    @Test
    @DisplayName("toggle - article 取消点赞 article like_count -1")
    void toggleUnlikeArticleDecrementsArticleLikeCount() {
        UserLike existing = UserLike.builder()
                .id(1L)
                .userId(1L)
                .targetType("article")
                .targetId(100L)
                .build();

        doReturn(existing).when(likeService).getOne(any(LambdaQueryWrapper.class));
        doReturn(true).when(likeService).removeById(1L);
        when(articleMapper.update(any(), any())).thenReturn(1);

        LikeStatusResponse result = likeService.toggle(1L, "article", 100L);

        assertThat(result.isLiked()).isFalse();
        verify(articleMapper).update(any(), any());
    }

    @Test
    @DisplayName("toggle - course 类型点赞 course like_count +1")
    void toggleLikeCourseIncrementsCourseLikeCount() {
        doReturn(null).when(likeService).getOne(any(LambdaQueryWrapper.class));
        doReturn(true).when(likeService).save(any(UserLike.class));
        when(courseMapper.update(any(), any())).thenReturn(1);

        LikeStatusResponse result = likeService.toggle(1L, "course", 100L);

        assertThat(result.isLiked()).isTrue();
        verify(courseMapper).update(any(), any());
    }

    @Test
    @DisplayName("toggle - course 取消点赞 course like_count -1")
    void toggleUnlikeCourseDecrementsCourseLikeCount() {
        UserLike existing = UserLike.builder()
                .id(1L)
                .userId(1L)
                .targetType("course")
                .targetId(100L)
                .build();

        doReturn(existing).when(likeService).getOne(any(LambdaQueryWrapper.class));
        doReturn(true).when(likeService).removeById(1L);
        when(courseMapper.update(any(), any())).thenReturn(1);

        LikeStatusResponse result = likeService.toggle(1L, "course", 100L);

        assertThat(result.isLiked()).isFalse();
        verify(courseMapper).update(any(), any());
    }

    @Test
    @DisplayName("getStatus - article 类型")
    void getStatusForArticle() {
        doReturn(0L).when(likeService).count(any(LambdaQueryWrapper.class));

        Article article = Article.builder().id(100L).likeCount(10).build();
        when(articleMapper.selectById(100L)).thenReturn(article);

        LikeStatusResponse result = likeService.getStatus(1L, "article", 100L);

        assertThat(result.isLiked()).isFalse();
        assertThat(result.getLikeCount()).isEqualTo(10);
    }

    @Test
    @DisplayName("getStatus - article 目标不存在抛异常")
    void getStatusForArticleWhenNotFoundThrowsException() {
        doReturn(0L).when(likeService).count(any(LambdaQueryWrapper.class));
        when(articleMapper.selectById(999L)).thenReturn(null);

        assertThatThrownBy(() -> likeService.getStatus(1L, "article", 999L))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> {
                    BusinessException be = (BusinessException) ex;
                    assertThat(be.getCode()).isEqualTo(SocialErrorConstants.LIKE_TARGET_NOT_FOUND);
                });
    }

    @Test
    @DisplayName("getStatus - course 类型")
    void getStatusForCourse() {
        doReturn(0L).when(likeService).count(any(LambdaQueryWrapper.class));

        Course course = Course.builder().id(100L).likeCount(5).build();
        when(courseMapper.selectById(100L)).thenReturn(course);

        LikeStatusResponse result = likeService.getStatus(1L, "course", 100L);

        assertThat(result.isLiked()).isFalse();
        assertThat(result.getLikeCount()).isEqualTo(5);
    }

    @Test
    @DisplayName("getStatus - course 目标不存在抛异常")
    void getStatusForCourseWhenNotFoundThrowsException() {
        doReturn(0L).when(likeService).count(any(LambdaQueryWrapper.class));
        when(courseMapper.selectById(999L)).thenReturn(null);

        assertThatThrownBy(() -> likeService.getStatus(1L, "course", 999L))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> {
                    BusinessException be = (BusinessException) ex;
                    assertThat(be.getCode()).isEqualTo(SocialErrorConstants.LIKE_TARGET_NOT_FOUND);
                });
    }

    @Test
    @DisplayName("toggle - 未知 targetType 取消点赞不操作任何 count")
    void toggleUnlikeUnknownType() {
        UserLike existing = UserLike.builder()
                .id(1L)
                .userId(1L)
                .targetType("unknown_type")
                .targetId(100L)
                .build();

        doReturn(existing).when(likeService).getOne(any(LambdaQueryWrapper.class));
        doReturn(true).when(likeService).removeById(1L);

        LikeStatusResponse result = likeService.toggle(1L, "unknown_type", 100L);

        assertThat(result.isLiked()).isFalse();
        verify(commentMapper, never()).update(any(), any());
        verify(articleMapper, never()).update(any(), any());
        verify(courseMapper, never()).update(any(), any());
    }

    @Test
    @DisplayName("getStatus - 未知 targetType 返回 likeCount 0")
    void getStatusForUnknownTargetTypeReturnsZero() {
        doReturn(0L).when(likeService).count(any(LambdaQueryWrapper.class));

        LikeStatusResponse result = likeService.getStatus(1L, "unknown_type", 100L);

        assertThat(result.isLiked()).isFalse();
        assertThat(result.getLikeCount()).isZero();
    }
}
