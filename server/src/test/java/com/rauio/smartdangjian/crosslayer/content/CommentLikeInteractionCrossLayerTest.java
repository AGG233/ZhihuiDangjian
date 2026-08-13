package com.rauio.smartdangjian.crosslayer.content;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionStatus;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rauio.smartdangjian.crosslayer.CrossLayerTestBase;
import com.rauio.smartdangjian.exception.BusinessException;
import com.rauio.smartdangjian.server.content.comment.constants.ContentInteractionErrorConstants;
import com.rauio.smartdangjian.server.content.comment.mapper.CommentMapper;
import com.rauio.smartdangjian.server.content.comment.mapper.InteractionTargetMapper;
import com.rauio.smartdangjian.server.content.comment.mapper.LikeRecordMapper;
import com.rauio.smartdangjian.server.content.comment.pojo.convertor.CommentConvertor;
import com.rauio.smartdangjian.server.content.comment.pojo.entity.Comment;
import com.rauio.smartdangjian.server.content.comment.pojo.entity.LikeRecord;
import com.rauio.smartdangjian.server.content.comment.pojo.request.CommentCreateRequest;
import com.rauio.smartdangjian.server.content.comment.pojo.response.CommentPageResponse;
import com.rauio.smartdangjian.server.content.comment.pojo.response.CommentResponse;
import com.rauio.smartdangjian.server.content.comment.pojo.response.LikeStatusResponse;
import com.rauio.smartdangjian.server.content.comment.service.CommentService;
import com.rauio.smartdangjian.server.content.comment.service.LikeService;

/**
 * 评论与点赞跨层回归测试。
 *
 * <p>装配真实 CommentService 与 LikeService，CommentMapper/LikeRecordMapper/
 * InteractionTargetMapper 以 {@link MockitoBean} 提供（Spring 在用例之间自动
 * 重置，沿用既有 CrossLayerTestBase 约定：H2 URL + Flyway 禁用 + 真实 Service）。
 * 通过捕获真实 save()/removeById() 调用与分页查询返回，断言发表/删除权限、
 * target 不存在报错与 toggle 幂等行为。
 */
@SpringBootTest(classes = CommentLikeInteractionCrossLayerTest.TestConfig.class)
class CommentLikeInteractionCrossLayerTest extends CrossLayerTestBase {

    private static final Long COURSE_ID = 1L;
    private static final Long OWNER_ID = 100L;

    @MockitoBean
    private CommentMapper commentMapper;

    @MockitoBean
    private LikeRecordMapper likeRecordMapper;

    @MockitoBean
    private InteractionTargetMapper interactionTargetMapper;

    @MockitoBean
    private CommentConvertor commentConvertor;

    @Autowired
    private CommentService commentService;

    @Autowired
    private LikeService likeService;

    @BeforeEach
    void stubTargetsExist() {
        when(interactionTargetMapper.countCourseById(COURSE_ID)).thenReturn(1L);
        when(commentMapper.insert(any(Comment.class))).thenReturn(1);
        when(likeRecordMapper.insert(any(LikeRecord.class))).thenReturn(1);
        when(commentConvertor.toResponse(any(Comment.class)))
                .thenReturn(CommentResponse.builder().id(1L).userId(OWNER_ID).build());
    }

    @SpringBootConfiguration
    static class TestConfig extends CrossLayerTestConfig {

        @Bean
        CommentService commentService(
                InteractionTargetMapper targetMapper, CommentConvertor convertor, CommentMapper commentMapper) {
            CommentService service = new CommentService(targetMapper, convertor);
            injectBaseMapper(service, commentMapper);
            return service;
        }

        @Bean
        LikeService likeService(InteractionTargetMapper targetMapper, LikeRecordMapper likeRecordMapper) {
            LikeService service = new LikeService(targetMapper);
            injectBaseMapper(service, likeRecordMapper);
            return service;
        }

        @Bean
        AbstractPlatformTransactionManager transactionManager() {
            return new AbstractPlatformTransactionManager() {
                @Override
                protected Object doGetTransaction() {
                    return new Object();
                }

                @Override
                protected void doBegin(Object transaction, TransactionDefinition definition) {}

                @Override
                protected void doCommit(DefaultTransactionStatus status) {}

                @Override
                protected void doRollback(DefaultTransactionStatus status) {}
            };
        }

        private static <T, M> void injectBaseMapper(T service, M mapper) {
            try {
                Field field = findBaseMapperField(service.getClass());
                field.setAccessible(true);
                field.set(service, mapper);
            } catch (Exception e) {
                throw new RuntimeException(
                        "Failed to set baseMapper on " + service.getClass().getSimpleName(), e);
            }
        }

        @SuppressWarnings("PMD.AvoidAccessibilityAlteration")
        private static Field findBaseMapperField(Class<?> clazz) throws NoSuchFieldException {
            Class<?> current = clazz;
            while (current != null) {
                try {
                    return current.getDeclaredField("baseMapper");
                } catch (NoSuchFieldException e) {
                    current = current.getSuperclass();
                }
            }
            throw new NoSuchFieldException("baseMapper");
        }
    }

    // ==================== 发表评论 ====================

    @Test
    @DisplayName("学生发表评论：userId 来自登录态，落库内容正确")
    void studentCreateCommentPersistsWithLoginUserId() {
        setStudentContext(OWNER_ID, "uni1");

        CommentResponse response = commentService.create(CommentCreateRequest.builder()
                .targetType("course")
                .targetId(COURSE_ID)
                .content("这门课程非常实用")
                .build());

        assertThat(response.getUserId()).isEqualTo(OWNER_ID);
        ArgumentCaptor<Comment> captor = ArgumentCaptor.forClass(Comment.class);
        verify(commentMapper).insert(captor.capture());
        Comment saved = captor.getValue();
        assertThat(saved.getUserId()).isEqualTo(OWNER_ID);
        assertThat(saved.getTargetType()).isEqualTo("course");
        assertThat(saved.getTargetId()).isEqualTo(COURSE_ID);
        assertThat(saved.getContent()).isEqualTo("这门课程非常实用");
        assertThat(saved.getStatus()).isEqualTo(1);
    }

    @Test
    @DisplayName("发表评论：target 不存在抛 3302 且不落库")
    void createCommentWithMissingTargetThrows() {
        setStudentContext(OWNER_ID, "uni1");
        when(interactionTargetMapper.countCourseById(999L)).thenReturn(0L);

        assertThatThrownBy(() -> commentService.create(CommentCreateRequest.builder()
                        .targetType("course")
                        .targetId(999L)
                        .content("内容")
                        .build()))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getCode())
                        .isEqualTo(ContentInteractionErrorConstants.TARGET_NOT_FOUND));
        verify(commentMapper, never()).insert(any(Comment.class));
    }

    // ==================== 删除权限 ====================

    @Test
    @DisplayName("删除评论：作者本人删除成功")
    void ownerDeletesOwnCommentSucceeds() {
        setStudentContext(OWNER_ID, "uni1");
        when(commentMapper.selectById(1L))
                .thenReturn(Comment.builder().id(1L).userId(OWNER_ID).build());
        when(commentMapper.deleteById(1L)).thenReturn(1);

        commentService.delete(1L);

        verify(commentMapper).deleteById(1L);
    }

    @Test
    @DisplayName("删除评论：他人删除抛 3306 且不删除")
    void otherUserDeletesCommentThrows() {
        setStudentContext(999L, "uni1");
        when(commentMapper.selectById(1L))
                .thenReturn(Comment.builder().id(1L).userId(OWNER_ID).build());

        assertThatThrownBy(() -> commentService.delete(1L))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getCode())
                        .isEqualTo(ContentInteractionErrorConstants.COMMENT_DELETE_FORBIDDEN));
        verify(commentMapper, never()).deleteById(any(Long.class));
    }

    @Test
    @DisplayName("删除评论：MANAGER 可删除他人评论")
    void managerDeletesAnyCommentSucceeds() {
        setManagerContext(999L, "uni1");
        when(commentMapper.selectById(1L))
                .thenReturn(Comment.builder().id(1L).userId(OWNER_ID).build());
        when(commentMapper.deleteById(1L)).thenReturn(1);

        commentService.delete(1L);

        verify(commentMapper).deleteById(1L);
    }

    // ==================== 分页 ====================

    @Test
    @DisplayName("分页查询评论：返回按目标过滤的评论列表")
    void getCommentPageReturnsFilteredRecords() {
        setStudentContext(OWNER_ID, "uni1");
        Comment c1 = Comment.builder().id(1L).build();
        Comment c2 = Comment.builder().id(2L).build();
        Page<Comment> page = new Page<>(1, 10, 2);
        page.setRecords(List.of(c1, c2));
        when(commentMapper.selectPage(any(Page.class), any())).thenReturn(page);
        when(commentConvertor.toResponseList(any(List.class)))
                .thenReturn(List.of(
                        CommentResponse.builder().build(),
                        CommentResponse.builder().build()));

        CommentPageResponse result = commentService.getPage("course", COURSE_ID, 1, 10);

        assertThat(result.getTotal()).isEqualTo(2);
        assertThat(result.getRecords()).hasSize(2);
    }

    // ==================== 点赞 toggle 幂等 ====================

    @Test
    @DisplayName("toggle 第一次点赞：插入记录，返回已赞且计数为1")
    void toggleFirstTimeLikesAndCounts() {
        setStudentContext(OWNER_ID, "uni1");
        when(likeRecordMapper.selectOne(any(), anyBoolean())).thenReturn(null);
        when(likeRecordMapper.selectCount(any())).thenReturn(1L);

        LikeStatusResponse result = likeService.toggle("course", COURSE_ID);

        assertThat(result.getLiked()).isTrue();
        assertThat(result.getCount()).isEqualTo(1);
        verify(likeRecordMapper).insert(any(LikeRecord.class));
    }

    @Test
    @DisplayName("toggle 幂等：两次点击后回到未赞状态且计数归位")
    void toggleTwiceReturnsToUnliked() {
        setStudentContext(OWNER_ID, "uni1");
        LikeRecord existing = LikeRecord.builder()
                .id(10L)
                .userId(OWNER_ID)
                .targetType("course")
                .targetId(COURSE_ID)
                .build();
        // 第一次查询无记录 → 插入；第二次查询有记录 → 删除（getOne 走 selectOne(wrapper, throwEx) 两参重载）
        when(likeRecordMapper.selectOne(any(), anyBoolean())).thenReturn(null, existing);
        when(likeRecordMapper.selectCount(any())).thenReturn(1L, 0L);
        when(likeRecordMapper.deleteById(10L)).thenReturn(1);

        LikeStatusResponse first = likeService.toggle("course", COURSE_ID);
        LikeStatusResponse second = likeService.toggle("course", COURSE_ID);

        assertThat(first.getLiked()).isTrue();
        assertThat(first.getCount()).isEqualTo(1);
        assertThat(second.getLiked()).isFalse();
        assertThat(second.getCount()).isZero();
        verify(likeRecordMapper, times(1)).insert(any(LikeRecord.class));
        verify(likeRecordMapper).deleteById(10L);
    }

    @Test
    @DisplayName("toggle：target 不存在抛 3302 且无任何写操作")
    void toggleWithMissingTargetThrows() {
        setStudentContext(OWNER_ID, "uni1");
        when(interactionTargetMapper.countArticleById(999L)).thenReturn(0L);

        assertThatThrownBy(() -> likeService.toggle("article", 999L))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getCode())
                        .isEqualTo(ContentInteractionErrorConstants.TARGET_NOT_FOUND));
        verify(likeRecordMapper, never()).insert(any(LikeRecord.class));
        verify(likeRecordMapper, never()).deleteById(any(Long.class));
    }
}
