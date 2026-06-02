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
import com.rauio.smartdangjian.server.social.constants.SocialErrorConstants;
import com.rauio.smartdangjian.server.social.mapper.UserLikeMapper;
import com.rauio.smartdangjian.server.social.pojo.entity.UserLike;
import com.rauio.smartdangjian.server.social.pojo.response.LikeStatusResponse;
import com.rauio.smartdangjian.server.social.support.LikeTargetGateway;
import com.rauio.smartdangjian.server.social.support.LikeTargetType;

@ExtendWith(MockitoExtension.class)
class LikeServiceTest {

    @Mock
    private UserLikeMapper userLikeMapper;

    @Mock
    private LikeTargetGateway likeTargetGateway;

    @Spy
    @InjectMocks
    private LikeService likeService;

    @BeforeEach
    void resetSpy() {
        reset(likeService);
    }

    @Test
    @DisplayName("toggle - 点赞前校验目标存在并增加计数")
    void toggleLikeWhenNoExistingRecord() {
        doReturn(null).when(likeService).getOne(any(LambdaQueryWrapper.class));
        doReturn(true).when(likeService).save(any(UserLike.class));

        LikeStatusResponse result = likeService.toggle(1L, "comment", 100L);

        assertThat(result.isLiked()).isTrue();
        assertThat(result.getTargetType()).isEqualTo("comment");
        assertThat(result.getTargetId()).isEqualTo(100L);
        verify(likeTargetGateway).requireExists(LikeTargetType.COMMENT, 100L);
        verify(likeService).save(any(UserLike.class));
        verify(likeTargetGateway).incrementLikeCount(LikeTargetType.COMMENT, 100L);
    }

    @Test
    @DisplayName("toggle - 取消点赞时减少计数")
    void toggleUnlikeWhenExistingRecord() {
        UserLike existing = UserLike.builder()
                .id(1L)
                .userId(1L)
                .targetType("comment")
                .targetId(100L)
                .build();

        doReturn(existing).when(likeService).getOne(any(LambdaQueryWrapper.class));
        doReturn(true).when(likeService).removeById(1L);

        LikeStatusResponse result = likeService.toggle(1L, "comment", 100L);

        assertThat(result.isLiked()).isFalse();
        assertThat(result.getTargetType()).isEqualTo("comment");
        assertThat(result.getTargetId()).isEqualTo(100L);
        verify(likeTargetGateway).requireExists(LikeTargetType.COMMENT, 100L);
        verify(likeService).removeById(1L);
        verify(likeTargetGateway).decrementLikeCount(LikeTargetType.COMMENT, 100L);
    }

    @Test
    @DisplayName("toggle - targetType 规范化为小写枚举值")
    void toggleNormalizesTargetType() {
        doReturn(null).when(likeService).getOne(any(LambdaQueryWrapper.class));
        doReturn(true).when(likeService).save(any(UserLike.class));

        LikeStatusResponse result = likeService.toggle(1L, "ARTICLE", 100L);

        assertThat(result.isLiked()).isTrue();
        assertThat(result.getTargetType()).isEqualTo("article");
        verify(likeTargetGateway).requireExists(LikeTargetType.ARTICLE, 100L);
        verify(likeTargetGateway).incrementLikeCount(LikeTargetType.ARTICLE, 100L);
    }

    @Test
    @DisplayName("toggle - 非法 targetType 不写入点赞记录")
    void toggleWithInvalidTargetTypeThrowsBeforeSave() {
        assertThatThrownBy(() -> likeService.toggle(1L, "unknown_type", 100L))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getCode())
                        .isEqualTo(SocialErrorConstants.LIKE_TARGET_TYPE_INVALID));

        verify(likeService, never()).save(any(UserLike.class));
        verify(likeTargetGateway, never()).requireExists(any(), any());
    }

    @Test
    @DisplayName("toggle - 不存在目标不写入点赞记录")
    void toggleTargetNotFoundThrowsBeforeSave() {
        doThrow(new BusinessException(SocialErrorConstants.LIKE_TARGET_NOT_FOUND, "点赞目标不存在"))
                .when(likeTargetGateway)
                .requireExists(LikeTargetType.COMMENT, 100L);

        assertThatThrownBy(() -> likeService.toggle(1L, "comment", 100L))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getCode())
                        .isEqualTo(SocialErrorConstants.LIKE_TARGET_NOT_FOUND));

        verify(likeService, never()).save(any(UserLike.class));
    }

    @Test
    @DisplayName("toggle - DB save 异常直接抛出且不增加计数")
    void toggleWhenSaveThrowsException() {
        doReturn(null).when(likeService).getOne(any(LambdaQueryWrapper.class));
        doThrow(new RuntimeException("DB保存失败")).when(likeService).save(any(UserLike.class));

        assertThatThrownBy(() -> likeService.toggle(1L, "comment", 100L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("DB保存失败");

        verify(likeTargetGateway, never()).incrementLikeCount(any(), any());
    }

    @Test
    @DisplayName("toggle - DB removeById 异常直接抛出且不减少计数")
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

        verify(likeTargetGateway, never()).decrementLikeCount(any(), any());
    }

    @Test
    @DisplayName("getStatus - 返回点赞状态和目标计数")
    void getStatusWhenLiked() {
        doReturn(1L).when(likeService).count(any(LambdaQueryWrapper.class));
        when(likeTargetGateway.getLikeCount(LikeTargetType.COMMENT, 100L)).thenReturn(42);

        LikeStatusResponse result = likeService.getStatus(1L, "comment", 100L);

        assertThat(result.isLiked()).isTrue();
        assertThat(result.getLikeCount()).isEqualTo(42);
        assertThat(result.getTargetType()).isEqualTo("comment");
        assertThat(result.getTargetId()).isEqualTo(100L);
    }

    @Test
    @DisplayName("getStatus - 非法 targetType 抛业务异常")
    void getStatusForUnknownTargetTypeThrowsException() {
        assertThatThrownBy(() -> likeService.getStatus(1L, "unknown_type", 100L))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getCode())
                        .isEqualTo(SocialErrorConstants.LIKE_TARGET_TYPE_INVALID));
    }
}
