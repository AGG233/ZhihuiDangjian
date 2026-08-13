package com.rauio.smartdangjian.server.content.comment.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.rauio.smartdangjian.constants.ErrorConstants;
import com.rauio.smartdangjian.exception.BusinessException;
import com.rauio.smartdangjian.server.content.comment.constants.ContentInteractionErrorConstants;
import com.rauio.smartdangjian.server.content.comment.mapper.InteractionTargetMapper;
import com.rauio.smartdangjian.server.content.comment.mapper.LikeRecordMapper;
import com.rauio.smartdangjian.server.content.comment.pojo.entity.LikeRecord;
import com.rauio.smartdangjian.server.content.comment.pojo.response.LikeStatusResponse;
import com.rauio.smartdangjian.utils.SecurityUtils;

@ExtendWith(MockitoExtension.class)
@DisplayName("LikeService 点赞服务")
class LikeServiceTest {

    @Mock
    private LikeRecordMapper mapper;

    @Mock
    private InteractionTargetMapper interactionTargetMapper;

    @Spy
    @InjectMocks
    private LikeService likeService;

    private MockedStatic<SecurityUtils> securityUtilsMock;

    @BeforeEach
    void setUp() {
        securityUtilsMock = mockStatic(SecurityUtils.class);
    }

    @AfterEach
    void tearDown() {
        securityUtilsMock.close();
    }

    // ==================== toggle ====================

    @Test
    @DisplayName("toggle 未赞过：插入记录返回已赞，计数为1")
    void toggleFirstTimeLikes() {
        securityUtilsMock.when(SecurityUtils::getCurrentUserId).thenReturn("100");
        doReturn(1L).when(interactionTargetMapper).countCourseById(1L);
        doReturn(null).when(likeService).getOne(any(LambdaQueryWrapper.class));
        doReturn(true).when(likeService).save(any(LikeRecord.class));
        doReturn(1L).when(likeService).count(any(LambdaQueryWrapper.class));

        LikeStatusResponse result = likeService.toggle("course", 1L);

        assertThat(result.getLiked()).isTrue();
        assertThat(result.getCount()).isEqualTo(1);
        ArgumentCaptor<LikeRecord> captor = ArgumentCaptor.forClass(LikeRecord.class);
        verify(likeService).save(captor.capture());
        assertThat(captor.getValue().getUserId()).isEqualTo(100L);
        assertThat(captor.getValue().getTargetType()).isEqualTo("course");
        assertThat(captor.getValue().getTargetId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("toggle 已赞过：删除记录返回未赞，计数归位为0")
    void toggleSecondTimeUnlikes() {
        securityUtilsMock.when(SecurityUtils::getCurrentUserId).thenReturn("100");
        doReturn(1L).when(interactionTargetMapper).countCourseById(1L);
        LikeRecord existing = LikeRecord.builder()
                .id(10L)
                .userId(100L)
                .targetType("course")
                .targetId(1L)
                .build();
        doReturn(existing).when(likeService).getOne(any(LambdaQueryWrapper.class));
        doReturn(true).when(likeService).removeById(10L);
        doReturn(0L).when(likeService).count(any(LambdaQueryWrapper.class));

        LikeStatusResponse result = likeService.toggle("course", 1L);

        assertThat(result.getLiked()).isFalse();
        assertThat(result.getCount()).isZero();
        verify(likeService).removeById(10L);
        verify(likeService, never()).save(any(LikeRecord.class));
    }

    @Test
    @DisplayName("toggle 幂等：两次点击后回到未赞状态且计数归位")
    void toggleTwiceIsIdempotent() {
        securityUtilsMock.when(SecurityUtils::getCurrentUserId).thenReturn("100");
        doReturn(1L).when(interactionTargetMapper).countCourseById(1L);
        LikeRecord existing = LikeRecord.builder()
                .id(10L)
                .userId(100L)
                .targetType("course")
                .targetId(1L)
                .build();
        // 第一次：未赞 → 插入
        doReturn(null).doReturn(existing).when(likeService).getOne(any(LambdaQueryWrapper.class));
        doReturn(true).when(likeService).save(any(LikeRecord.class));
        doReturn(true).when(likeService).removeById(10L);
        doReturn(1L).doReturn(0L).when(likeService).count(any(LambdaQueryWrapper.class));

        LikeStatusResponse first = likeService.toggle("course", 1L);
        LikeStatusResponse second = likeService.toggle("course", 1L);

        assertThat(first.getLiked()).isTrue();
        assertThat(first.getCount()).isEqualTo(1);
        assertThat(second.getLiked()).isFalse();
        assertThat(second.getCount()).isZero();
        verify(likeService, times(1)).save(any(LikeRecord.class));
        verify(likeService, times(1)).removeById(10L);
    }

    @Test
    @DisplayName("toggle：目标不存在抛 3302")
    void toggleTargetNotFoundThrows() {
        securityUtilsMock.when(SecurityUtils::getCurrentUserId).thenReturn("100");
        doReturn(0L).when(interactionTargetMapper).countCourseById(999L);

        assertThatThrownBy(() -> likeService.toggle("course", 999L))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getCode())
                        .isEqualTo(ContentInteractionErrorConstants.TARGET_NOT_FOUND));
        verify(likeService, never()).save(any(LikeRecord.class));
        verify(likeService, never()).removeById(any(Long.class));
    }

    @Test
    @DisplayName("toggle：targetType 非法抛 3301")
    void toggleInvalidTargetTypeThrows() {
        assertThatThrownBy(() -> likeService.toggle("video", 1L))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getCode())
                        .isEqualTo(ContentInteractionErrorConstants.TARGET_TYPE_INVALID));
    }

    // ==================== getCount ====================

    @Test
    @DisplayName("getCount 返回点赞总数")
    void getCountReturnsCount() {
        doReturn(5L).when(likeService).count(any(LambdaQueryWrapper.class));

        long result = likeService.getCount("article", 2L);

        assertThat(result).isEqualTo(5);
    }

    @Test
    @DisplayName("getCount 无点赞返回0")
    void getCountZeroWhenNoLikes() {
        doReturn(0L).when(likeService).count(any(LambdaQueryWrapper.class));

        long result = likeService.getCount("article", 2L);

        assertThat(result).isZero();
    }

    @Test
    @DisplayName("getCount targetType 非法抛 3301")
    void getCountInvalidTargetTypeThrows() {
        assertThatThrownBy(() -> likeService.getCount("video", 1L))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getCode())
                        .isEqualTo(ContentInteractionErrorConstants.TARGET_TYPE_INVALID));
    }

    @Test
    @DisplayName("toggle 文章目标：校验文章存在并点赞成功")
    void toggleArticleTargetLikes() {
        securityUtilsMock.when(SecurityUtils::getCurrentUserId).thenReturn("100");
        doReturn(1L).when(interactionTargetMapper).countArticleById(2L);
        doReturn(null).when(likeService).getOne(any(LambdaQueryWrapper.class));
        doReturn(true).when(likeService).save(any(LikeRecord.class));
        doReturn(1L).when(likeService).count(any(LambdaQueryWrapper.class));

        LikeStatusResponse result = likeService.toggle("article", 2L);

        assertThat(result.getLiked()).isTrue();
        assertThat(result.getCount()).isEqualTo(1);
        verify(interactionTargetMapper).countArticleById(2L);
    }

    @Test
    @DisplayName("toggle 点赞落库失败抛 3309")
    void toggleSaveFailsThrows() {
        securityUtilsMock.when(SecurityUtils::getCurrentUserId).thenReturn("100");
        doReturn(1L).when(interactionTargetMapper).countCourseById(1L);
        doReturn(null).when(likeService).getOne(any(LambdaQueryWrapper.class));
        doReturn(false).when(likeService).save(any(LikeRecord.class));

        assertThatThrownBy(() -> likeService.toggle("course", 1L))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getCode())
                        .isEqualTo(ContentInteractionErrorConstants.LIKE_TOGGLE_FAILED));
    }

    @Test
    @DisplayName("toggle 未登录抛 RESOURCE_NOT_AUTHORIZED")
    void toggleNotLoggedInThrows() {
        securityUtilsMock.when(SecurityUtils::getCurrentUserId).thenReturn(null);
        doReturn(1L).when(interactionTargetMapper).countCourseById(1L);

        assertThatThrownBy(() -> likeService.toggle("course", 1L))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getCode())
                        .isEqualTo(ErrorConstants.RESOURCE_NOT_AUTHORIZED));
        verify(likeService, never()).save(any(LikeRecord.class));
    }
}
