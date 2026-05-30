package com.rauio.smartdangjian.server.social.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
}
