package com.rauio.smartdangjian.server.content.comment.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Collections;
import java.util.List;

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
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rauio.smartdangjian.constants.ErrorConstants;
import com.rauio.smartdangjian.exception.BusinessException;
import com.rauio.smartdangjian.server.content.comment.constants.ContentInteractionErrorConstants;
import com.rauio.smartdangjian.server.content.comment.mapper.CommentMapper;
import com.rauio.smartdangjian.server.content.comment.mapper.InteractionTargetMapper;
import com.rauio.smartdangjian.server.content.comment.pojo.convertor.CommentConvertor;
import com.rauio.smartdangjian.server.content.comment.pojo.entity.Comment;
import com.rauio.smartdangjian.server.content.comment.pojo.request.CommentCreateRequest;
import com.rauio.smartdangjian.server.content.comment.pojo.response.CommentPageResponse;
import com.rauio.smartdangjian.server.content.comment.pojo.response.CommentResponse;
import com.rauio.smartdangjian.utils.SecurityUtils;
import com.rauio.smartdangjian.utils.spec.UserType;

@ExtendWith(MockitoExtension.class)
@DisplayName("CommentService 评论服务")
class CommentServiceTest {

    @Mock
    private CommentMapper mapper;

    @Mock
    private InteractionTargetMapper interactionTargetMapper;

    @Mock
    private CommentConvertor commentConvertor;

    @Spy
    @InjectMocks
    private CommentService commentService;

    private MockedStatic<SecurityUtils> securityUtilsMock;

    @BeforeEach
    void setUp() {
        securityUtilsMock = mockStatic(SecurityUtils.class);
    }

    @AfterEach
    void tearDown() {
        securityUtilsMock.close();
    }

    // ==================== create ====================

    @Test
    @DisplayName("发表评论：target 存在且内容合法时落库 userId 来自登录态")
    void createSuccessPersistsCommentFromLoginUser() {
        securityUtilsMock.when(SecurityUtils::getCurrentUserId).thenReturn("100");
        doReturn(1L).when(interactionTargetMapper).countCourseById(1L);
        doReturn(true).when(commentService).save(any(Comment.class));

        CommentResponse response = CommentResponse.builder().build();
        doReturn(response).when(commentConvertor).toResponse(any(Comment.class));

        CommentCreateRequest request = CommentCreateRequest.builder()
                .targetType("course")
                .targetId(1L)
                .content("很有帮助的课程")
                .build();

        CommentResponse result = commentService.create(request);

        assertThat(result).isEqualTo(response);
        ArgumentCaptor<Comment> captor = ArgumentCaptor.forClass(Comment.class);
        verify(commentService).save(captor.capture());
        Comment saved = captor.getValue();
        assertThat(saved.getUserId()).isEqualTo(100L);
        assertThat(saved.getTargetType()).isEqualTo("course");
        assertThat(saved.getTargetId()).isEqualTo(1L);
        assertThat(saved.getContent()).isEqualTo("很有帮助的课程");
        assertThat(saved.getStatus()).isEqualTo(1);
        assertThat(saved.getParentId()).isNull();
    }

    @Test
    @DisplayName("发表评论：targetType 非法抛 3301")
    void createInvalidTargetTypeThrows() {
        CommentCreateRequest request = CommentCreateRequest.builder()
                .targetType("video")
                .targetId(1L)
                .content("内容")
                .build();

        assertThatThrownBy(() -> commentService.create(request))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getCode())
                        .isEqualTo(ContentInteractionErrorConstants.TARGET_TYPE_INVALID));
        verify(commentService, never()).save(any(Comment.class));
    }

    @Test
    @DisplayName("发表评论：target 不存在抛 3302 且不落库")
    void createTargetNotFoundThrows() {
        securityUtilsMock.when(SecurityUtils::getCurrentUserId).thenReturn("100");
        doReturn(0L).when(interactionTargetMapper).countCourseById(999L);

        CommentCreateRequest request = CommentCreateRequest.builder()
                .targetType("course")
                .targetId(999L)
                .content("内容")
                .build();

        assertThatThrownBy(() -> commentService.create(request))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getCode())
                        .isEqualTo(ContentInteractionErrorConstants.TARGET_NOT_FOUND));
        verify(commentService, never()).save(any(Comment.class));
    }

    @Test
    @DisplayName("发表评论：文章类型 target 不存在抛 3302")
    void createArticleTargetNotFoundThrows() {
        securityUtilsMock.when(SecurityUtils::getCurrentUserId).thenReturn("100");
        doReturn(0L).when(interactionTargetMapper).countArticleById(999L);

        CommentCreateRequest request = CommentCreateRequest.builder()
                .targetType("article")
                .targetId(999L)
                .content("内容")
                .build();

        assertThatThrownBy(() -> commentService.create(request))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getCode())
                        .isEqualTo(ContentInteractionErrorConstants.TARGET_NOT_FOUND));
    }

    @Test
    @DisplayName("发表评论：内容为空抛 3303")
    void createEmptyContentThrows() {
        securityUtilsMock.when(SecurityUtils::getCurrentUserId).thenReturn("100");
        doReturn(1L).when(interactionTargetMapper).countCourseById(1L);

        CommentCreateRequest request = CommentCreateRequest.builder()
                .targetType("course")
                .targetId(1L)
                .content("   ")
                .build();

        assertThatThrownBy(() -> commentService.create(request))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getCode())
                        .isEqualTo(ContentInteractionErrorConstants.COMMENT_CONTENT_EMPTY));
    }

    @Test
    @DisplayName("发表评论：内容超长抛 3304")
    void createContentTooLongThrows() {
        securityUtilsMock.when(SecurityUtils::getCurrentUserId).thenReturn("100");
        doReturn(1L).when(interactionTargetMapper).countCourseById(1L);

        CommentCreateRequest request = CommentCreateRequest.builder()
                .targetType("course")
                .targetId(1L)
                .content("长".repeat(501))
                .build();

        assertThatThrownBy(() -> commentService.create(request))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getCode())
                        .isEqualTo(ContentInteractionErrorConstants.COMMENT_CONTENT_TOO_LONG));
    }

    @Test
    @DisplayName("发表评论：父评论不存在抛 3308")
    void createParentNotFoundThrows() {
        securityUtilsMock.when(SecurityUtils::getCurrentUserId).thenReturn("100");
        doReturn(1L).when(interactionTargetMapper).countCourseById(1L);
        doReturn(null).when(commentService).getById(999L);

        CommentCreateRequest request = CommentCreateRequest.builder()
                .targetType("course")
                .targetId(1L)
                .content("回复")
                .parentId(999L)
                .build();

        assertThatThrownBy(() -> commentService.create(request))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getCode())
                        .isEqualTo(ContentInteractionErrorConstants.COMMENT_PARENT_NOT_FOUND));
        verify(commentService, never()).save(any(Comment.class));
    }

    @Test
    @DisplayName("发表回复评论：父评论存在时带 parentId 落库")
    void createReplyWithParentPersistsParentId() {
        securityUtilsMock.when(SecurityUtils::getCurrentUserId).thenReturn("100");
        doReturn(1L).when(interactionTargetMapper).countCourseById(1L);
        doReturn(Comment.builder().id(5L).build()).when(commentService).getById(5L);
        doReturn(true).when(commentService).save(any(Comment.class));

        CommentCreateRequest request = CommentCreateRequest.builder()
                .targetType("course")
                .targetId(1L)
                .content("回复内容")
                .parentId(5L)
                .build();

        commentService.create(request);

        ArgumentCaptor<Comment> captor = ArgumentCaptor.forClass(Comment.class);
        verify(commentService).save(captor.capture());
        assertThat(captor.getValue().getParentId()).isEqualTo(5L);
    }

    // ==================== getPage ====================

    @Test
    @DisplayName("分页查询：按目标过滤并按创建时间倒序")
    void getPageFiltersByTargetAndOrdersDesc() {
        Comment c1 = Comment.builder().id(1L).build();
        Comment c2 = Comment.builder().id(2L).build();
        Page<Comment> page = new Page<>(1, 10, 2);
        page.setRecords(List.of(c1, c2));
        doReturn(page).when(commentService).page(any(Page.class), any(LambdaQueryWrapper.class));

        List<CommentResponse> responses = List.of(
                CommentResponse.builder().build(), CommentResponse.builder().build());
        doReturn(responses).when(commentConvertor).toResponseList(any(List.class));

        CommentPageResponse result = commentService.getPage("course", 1L, 1, 10);

        assertThat(result.getTotal()).isEqualTo(2);
        assertThat(result.getRecords()).hasSize(2);
        @SuppressWarnings("unchecked")
        ArgumentCaptor<LambdaQueryWrapper<Comment>> wrapperCaptor = ArgumentCaptor.forClass(LambdaQueryWrapper.class);
        verify(commentService).page(any(Page.class), wrapperCaptor.capture());
        assertThat(wrapperCaptor.getValue()).isNotNull();
    }

    @Test
    @DisplayName("分页查询：无评论返回空列表")
    void getPageEmpty() {
        Page<Comment> page = new Page<>(1, 10, 0);
        page.setRecords(Collections.emptyList());
        doReturn(page).when(commentService).page(any(Page.class), any(LambdaQueryWrapper.class));
        doReturn(Collections.emptyList()).when(commentConvertor).toResponseList(any(List.class));

        CommentPageResponse result = commentService.getPage("article", 1L, 1, 10);

        assertThat(result.getTotal()).isZero();
        assertThat(result.getRecords()).isEmpty();
    }

    @Test
    @DisplayName("分页查询：targetType 非法抛 3301")
    void getPageInvalidTargetTypeThrows() {
        assertThatThrownBy(() -> commentService.getPage("video", 1L, 1, 10))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getCode())
                        .isEqualTo(ContentInteractionErrorConstants.TARGET_TYPE_INVALID));
    }

    // ==================== delete ====================

    @Test
    @DisplayName("删除评论：作者本人可删除")
    void deleteByOwnerSucceeds() {
        doReturn(Comment.builder().id(1L).userId(100L).build())
                .when(commentService)
                .getById(1L);
        securityUtilsMock.when(SecurityUtils::getCurrentUserId).thenReturn("100");
        securityUtilsMock.when(SecurityUtils::getCurrentUserType).thenReturn(UserType.STUDENT);
        doReturn(true).when(commentService).removeById(1L);

        commentService.delete(1L);

        verify(commentService).removeById(1L);
    }

    @Test
    @DisplayName("删除评论：他人评论抛 3306 且不删除")
    void deleteByOtherUserThrows() {
        doReturn(Comment.builder().id(1L).userId(100L).build())
                .when(commentService)
                .getById(1L);
        securityUtilsMock.when(SecurityUtils::getCurrentUserId).thenReturn("200");
        securityUtilsMock.when(SecurityUtils::getCurrentUserType).thenReturn(UserType.STUDENT);

        assertThatThrownBy(() -> commentService.delete(1L))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getCode())
                        .isEqualTo(ContentInteractionErrorConstants.COMMENT_DELETE_FORBIDDEN));
        verify(commentService, never()).removeById(any(Long.class));
    }

    @Test
    @DisplayName("删除评论：MANAGER 可删除他人评论")
    void deleteByManagerSucceeds() {
        doReturn(Comment.builder().id(1L).userId(100L).build())
                .when(commentService)
                .getById(1L);
        securityUtilsMock.when(SecurityUtils::getCurrentUserId).thenReturn("200");
        securityUtilsMock.when(SecurityUtils::getCurrentUserType).thenReturn(UserType.MANAGER);
        doReturn(true).when(commentService).removeById(1L);

        commentService.delete(1L);

        verify(commentService, times(1)).removeById(1L);
    }

    @Test
    @DisplayName("删除评论：评论不存在抛 3305")
    void deleteNotFoundThrows() {
        doReturn(null).when(commentService).getById(999L);

        assertThatThrownBy(() -> commentService.delete(999L))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getCode())
                        .isEqualTo(ContentInteractionErrorConstants.COMMENT_NOT_FOUND));
        verify(commentService, never()).removeById(any(Long.class));
    }

    @Test
    @DisplayName("发表评论：content 为 null 抛 3303")
    void createNullContentThrows() {
        securityUtilsMock.when(SecurityUtils::getCurrentUserId).thenReturn("100");
        doReturn(1L).when(interactionTargetMapper).countCourseById(1L);

        CommentCreateRequest request = CommentCreateRequest.builder()
                .targetType("course")
                .targetId(1L)
                .content(null)
                .build();

        assertThatThrownBy(() -> commentService.create(request))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getCode())
                        .isEqualTo(ContentInteractionErrorConstants.COMMENT_CONTENT_EMPTY));
    }

    @Test
    @DisplayName("发表评论：落库失败抛 3307")
    void createSaveFailsThrows() {
        securityUtilsMock.when(SecurityUtils::getCurrentUserId).thenReturn("100");
        doReturn(1L).when(interactionTargetMapper).countCourseById(1L);
        doReturn(false).when(commentService).save(any(Comment.class));

        CommentCreateRequest request = CommentCreateRequest.builder()
                .targetType("course")
                .targetId(1L)
                .content("内容")
                .build();

        assertThatThrownBy(() -> commentService.create(request))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getCode())
                        .isEqualTo(ContentInteractionErrorConstants.COMMENT_SAVE_FAILED));
    }

    @Test
    @DisplayName("发表评论：未登录抛 RESOURCE_NOT_AUTHORIZED")
    void createNotLoggedInThrows() {
        securityUtilsMock.when(SecurityUtils::getCurrentUserId).thenReturn(null);
        doReturn(1L).when(interactionTargetMapper).countCourseById(1L);

        CommentCreateRequest request = CommentCreateRequest.builder()
                .targetType("course")
                .targetId(1L)
                .content("内容")
                .build();

        assertThatThrownBy(() -> commentService.create(request))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getCode())
                        .isEqualTo(ErrorConstants.RESOURCE_NOT_AUTHORIZED));
        verify(commentService, never()).save(any(Comment.class));
    }

    @Test
    @DisplayName("删除评论：评论 userId 为 null 时无权删除抛 3306")
    void deleteCommentWithNullUserIdThrows() {
        doReturn(Comment.builder().id(1L).userId(null).build())
                .when(commentService)
                .getById(1L);
        securityUtilsMock.when(SecurityUtils::getCurrentUserId).thenReturn("100");
        securityUtilsMock.when(SecurityUtils::getCurrentUserType).thenReturn(UserType.STUDENT);

        assertThatThrownBy(() -> commentService.delete(1L))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getCode())
                        .isEqualTo(ContentInteractionErrorConstants.COMMENT_DELETE_FORBIDDEN));
        verify(commentService, never()).removeById(any(Long.class));
    }

    @Test
    @DisplayName("删除评论：未登录抛 3306")
    void deleteNotLoggedInThrows() {
        doReturn(Comment.builder().id(1L).userId(100L).build())
                .when(commentService)
                .getById(1L);
        securityUtilsMock.when(SecurityUtils::getCurrentUserId).thenReturn(null);
        securityUtilsMock.when(SecurityUtils::getCurrentUserType).thenReturn(null);

        assertThatThrownBy(() -> commentService.delete(1L))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getCode())
                        .isEqualTo(ContentInteractionErrorConstants.COMMENT_DELETE_FORBIDDEN));
        verify(commentService, never()).removeById(any(Long.class));
    }
}
