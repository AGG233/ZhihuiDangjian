package com.rauio.smartdangjian.server.social.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rauio.smartdangjian.exception.BusinessException;
import com.rauio.smartdangjian.pojo.response.SensitiveWordMatchResult;
import com.rauio.smartdangjian.server.social.constants.SocialErrorConstants;
import com.rauio.smartdangjian.server.social.mapper.CommentMapper;
import com.rauio.smartdangjian.server.social.pojo.entity.Comment;
import com.rauio.smartdangjian.server.social.pojo.request.CommentRequest;
import com.rauio.smartdangjian.server.social.pojo.response.CommentResponse;
import com.rauio.smartdangjian.server.user.pojo.entity.User;
import com.rauio.smartdangjian.server.user.service.UserService;
import com.rauio.smartdangjian.service.SensitiveWordService;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @Mock
    private CommentMapper commentMapper;

    @Mock
    private UserService userService;

    @Mock
    private SensitiveWordService sensitiveWordService;

    @Spy
    @InjectMocks
    private CommentService commentService;

    @BeforeEach
    void resetSpy() {
        reset(commentService);
    }

    @Captor
    private ArgumentCaptor<Comment> commentCaptor;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(commentService, "baseMapper", commentMapper);
    }

    @Test
    @DisplayName("create - 正常创建评论（无敏感词）")
    void createWithoutSensitiveWords() {
        CommentRequest request = CommentRequest.builder()
                .content("这是一条正常评论")
                .targetType("article")
                .targetId(1L)
                .build();

        when(sensitiveWordService.check("这是一条正常评论"))
                .thenReturn(SensitiveWordMatchResult.builder().matched(false).build());
        doReturn(true).when(commentService).save(any(Comment.class));

        User user = User.builder().id(1L).username("testuser").realName("测试用户").build();
        when(userService.getById(1L)).thenReturn(user);

        CommentResponse result = commentService.create(1L, request);

        assertThat(result.getContent()).isEqualTo("这是一条正常评论");
        assertThat(result.getStatus()).isEqualTo("published");
        assertThat(result.getUserId()).isEqualTo(1L);
        assertThat(result.getTargetType()).isEqualTo("article");
        assertThat(result.getTargetId()).isEqualTo(1L);
        assertThat(result.getUsername()).isEqualTo("testuser");
        assertThat(result.getRealName()).isEqualTo("测试用户");
        verify(commentService).save(commentCaptor.capture());
        assertThat(commentCaptor.getValue().getStatus()).isEqualTo("published");
    }

    @Test
    @DisplayName("create - 包含敏感词创建 pending_review 状态")
    void createWithSensitiveWords() {
        CommentRequest request = CommentRequest.builder()
                .content("这条评论包含敏感词")
                .targetType("article")
                .targetId(1L)
                .build();

        when(sensitiveWordService.check("这条评论包含敏感词"))
                .thenReturn(SensitiveWordMatchResult.builder()
                        .matched(true)
                        .words(List.of("敏感词"))
                        .build());
        doReturn(true).when(commentService).save(any(Comment.class));

        User user = User.builder().id(1L).username("testuser").realName("测试用户").build();
        when(userService.getById(1L)).thenReturn(user);

        CommentResponse result = commentService.create(1L, request);

        assertThat(result.getStatus()).isEqualTo("pending_review");
        verify(commentService).save(commentCaptor.capture());
        assertThat(commentCaptor.getValue().getStatus()).isEqualTo("pending_review");
    }

    @Test
    @DisplayName("create - 回复评论时 parent 的 reply_count +1")
    void createReplyIncrementsParentReplyCount() {
        CommentRequest request = CommentRequest.builder()
                .content("这是一条回复")
                .targetType("article")
                .targetId(1L)
                .parentId(100L)
                .build();

        when(sensitiveWordService.check("这是一条回复"))
                .thenReturn(SensitiveWordMatchResult.builder().matched(false).build());
        doReturn(true).when(commentService).save(any(Comment.class));
        when(commentMapper.update(any(), any())).thenReturn(1);

        User user = User.builder().id(1L).username("testuser").realName("测试用户").build();
        when(userService.getById(1L)).thenReturn(user);

        CommentResponse result = commentService.create(1L, request);

        assertThat(result.getContent()).isEqualTo("这是一条回复");
        assertThat(result.getParentId()).isEqualTo(100L);
        verify(commentMapper).update(any(), any());
    }

    @Test
    @DisplayName("delete - 正常删除自己的评论")
    void deleteOwnComment() {
        Comment comment = Comment.builder()
                .id(1L)
                .userId(1L)
                .content("测试评论")
                .status("published")
                .build();

        doReturn(comment).when(commentService).getById(1L);
        doReturn(true).when(commentService).updateById(any(Comment.class));

        commentService.delete(1L, 1L);

        verify(commentService).updateById(commentCaptor.capture());
        assertThat(commentCaptor.getValue().getStatus()).isEqualTo("deleted");
    }

    @Test
    @DisplayName("delete - 删除他人评论抛权限异常")
    void deleteOthersCommentThrowsPermissionException() {
        Comment comment = Comment.builder()
                .id(1L)
                .userId(2L)
                .content("他人评论")
                .status("published")
                .build();

        doReturn(comment).when(commentService).getById(1L);

        assertThatThrownBy(() -> commentService.delete(1L, 1L))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> {
                    BusinessException be = (BusinessException) ex;
                    assertThat(be.getCode()).isEqualTo(SocialErrorConstants.COMMENT_PERMISSION_DENIED);
                });
    }

    @Test
    @DisplayName("delete - 评论不存在抛异常")
    void deleteNonExistentCommentThrowsException() {
        doReturn(null).when(commentService).getById(999L);

        assertThatThrownBy(() -> commentService.delete(999L, 1L))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> {
                    BusinessException be = (BusinessException) ex;
                    assertThat(be.getCode()).isEqualTo(SocialErrorConstants.COMMENT_NOT_FOUND);
                });
    }

    @Test
    @DisplayName("getPage - 分页查询（最新排序）")
    void getPageSortedByLatest() {
        Comment comment1 = Comment.builder()
                .id(1L)
                .targetType("article")
                .targetId(1L)
                .userId(1L)
                .content("评论1")
                .status("published")
                .likeCount(5)
                .replyCount(0)
                .createdAt(LocalDateTime.now().minusHours(1))
                .build();
        Comment comment2 = Comment.builder()
                .id(2L)
                .targetType("article")
                .targetId(1L)
                .userId(2L)
                .content("评论2")
                .status("published")
                .likeCount(10)
                .replyCount(0)
                .createdAt(LocalDateTime.now())
                .build();

        Page<Comment> commentPage = new Page<>(1, 10, 2);
        commentPage.setRecords(List.of(comment2, comment1));

        doReturn(commentPage).when(commentService).page(any(Page.class), any(LambdaQueryWrapper.class));

        User user1 = User.builder().id(1L).username("user1").realName("用户1").build();
        User user2 = User.builder().id(2L).username("user2").realName("用户2").build();
        when(userService.listByIds(Set.of(1L, 2L))).thenReturn(List.of(user1, user2));

        Page<CommentResponse> result = commentService.getPage("article", 1L, null, 1, 10, "latest");

        assertThat(result.getTotal()).isEqualTo(2);
        assertThat(result.getRecords()).hasSize(2);
        assertThat(result.getRecords().get(0).getId()).isEqualTo(2L);
        assertThat(result.getRecords().get(1).getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("getPage - 分页查询（最热排序）")
    void getPageSortedByHot() {
        Comment comment1 = Comment.builder()
                .id(1L)
                .targetType("article")
                .targetId(1L)
                .userId(1L)
                .content("评论1")
                .status("published")
                .likeCount(100)
                .replyCount(0)
                .createdAt(LocalDateTime.now())
                .build();
        Comment comment2 = Comment.builder()
                .id(2L)
                .targetType("article")
                .targetId(1L)
                .userId(2L)
                .content("评论2")
                .status("published")
                .likeCount(50)
                .replyCount(0)
                .createdAt(LocalDateTime.now().minusHours(1))
                .build();

        Page<Comment> commentPage = new Page<>(1, 10, 2);
        commentPage.setRecords(List.of(comment1, comment2));

        doReturn(commentPage).when(commentService).page(any(Page.class), any(LambdaQueryWrapper.class));

        User user1 = User.builder().id(1L).username("user1").realName("用户1").build();
        User user2 = User.builder().id(2L).username("user2").realName("用户2").build();
        when(userService.listByIds(Set.of(1L, 2L))).thenReturn(List.of(user1, user2));

        Page<CommentResponse> result = commentService.getPage("article", 1L, null, 1, 10, "hot");

        assertThat(result.getTotal()).isEqualTo(2);
        assertThat(result.getRecords()).hasSize(2);
        assertThat(result.getRecords().get(0).getLikeCount()).isEqualTo(100);
        assertThat(result.getRecords().get(1).getLikeCount()).isEqualTo(50);
    }

    @Test
    @DisplayName("getPage - 只返回非 deleted/hidden 状态")
    void getPageFiltersDeletedAndHidden() {
        Comment visibleComment = Comment.builder()
                .id(1L)
                .targetType("article")
                .targetId(1L)
                .userId(1L)
                .content("可见评论")
                .status("published")
                .likeCount(0)
                .replyCount(0)
                .createdAt(LocalDateTime.now())
                .build();

        Page<Comment> commentPage = new Page<>(1, 10, 1);
        commentPage.setRecords(List.of(visibleComment));

        doReturn(commentPage).when(commentService).page(any(Page.class), any(LambdaQueryWrapper.class));

        User user = User.builder().id(1L).username("user1").realName("用户1").build();
        when(userService.listByIds(Set.of(1L))).thenReturn(List.of(user));

        Page<CommentResponse> result = commentService.getPage("article", 1L, null, 1, 10, "latest");

        assertThat(result.getRecords()).hasSize(1);
        assertThat(result.getRecords().get(0).getStatus()).isEqualTo("published");
        assertThat(result.getRecords().get(0).getContent()).isEqualTo("可见评论");
    }

    @Test
    @DisplayName("create - DB 保存异常抛 RuntimeException")
    void createWhenDbSaveThrowsException() {
        CommentRequest request = CommentRequest.builder()
                .content("正常评论")
                .targetType("article")
                .targetId(1L)
                .build();

        when(sensitiveWordService.check("正常评论"))
                .thenReturn(SensitiveWordMatchResult.builder().matched(false).build());
        doThrow(new RuntimeException("DB连接失败")).when(commentService).save(any(Comment.class));

        assertThatThrownBy(() -> commentService.create(1L, request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("DB连接失败");
    }

    @Test
    @DisplayName("delete - 查询异常抛 RuntimeException")
    void deleteWhenGetByIdThrowsException() {
        doThrow(new RuntimeException("DB查询失败")).when(commentService).getById(1L);

        assertThatThrownBy(() -> commentService.delete(1L, 1L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("DB查询失败");
    }

    @Test
    @DisplayName("create - null targetId")
    void createWithNullTargetId() {
        CommentRequest request = CommentRequest.builder()
                .content("测试评论")
                .targetType("article")
                .targetId(null)
                .build();

        when(sensitiveWordService.check("测试评论"))
                .thenReturn(SensitiveWordMatchResult.builder().matched(false).build());
        doReturn(true).when(commentService).save(any(Comment.class));

        User user = User.builder().id(1L).username("testuser").realName("测试用户").build();
        when(userService.getById(1L)).thenReturn(user);

        CommentResponse result = commentService.create(1L, request);

        assertThat(result.getTargetId()).isNull();
        verify(commentService).save(commentCaptor.capture());
        assertThat(commentCaptor.getValue().getTargetId()).isNull();
    }

    @Test
    @DisplayName("create - 空 content")
    void createWithEmptyContent() {
        CommentRequest request = CommentRequest.builder()
                .content("")
                .targetType("article")
                .targetId(1L)
                .build();

        when(sensitiveWordService.check(""))
                .thenReturn(SensitiveWordMatchResult.builder().matched(false).build());
        doReturn(true).when(commentService).save(any(Comment.class));

        User user = User.builder().id(1L).username("testuser").realName("测试用户").build();
        when(userService.getById(1L)).thenReturn(user);

        CommentResponse result = commentService.create(1L, request);

        assertThat(result.getContent()).isEmpty();
        verify(commentService).save(commentCaptor.capture());
        assertThat(commentCaptor.getValue().getContent()).isEmpty();
    }

    @Test
    @DisplayName("create - 超长 content（10000 字符）")
    void createWithVeryLongContent() {
        String longContent = "a".repeat(10000);
        CommentRequest request = CommentRequest.builder()
                .content(longContent)
                .targetType("article")
                .targetId(1L)
                .build();

        when(sensitiveWordService.check(longContent))
                .thenReturn(SensitiveWordMatchResult.builder().matched(false).build());
        doReturn(true).when(commentService).save(any(Comment.class));

        User user = User.builder().id(1L).username("testuser").realName("测试用户").build();
        when(userService.getById(1L)).thenReturn(user);

        CommentResponse result = commentService.create(1L, request);

        assertThat(result.getContent()).hasSize(10000);
        verify(commentService).save(commentCaptor.capture());
        assertThat(commentCaptor.getValue().getContent()).hasSize(10000);
    }

    @Test
    @DisplayName("getPage - 指定 parentId 过滤")
    void getPageWithParentIdFilter() {
        Comment replyComment = Comment.builder()
                .id(2L)
                .targetType("article")
                .targetId(1L)
                .userId(1L)
                .parentId(100L)
                .content("回复")
                .status("published")
                .likeCount(0)
                .replyCount(0)
                .createdAt(LocalDateTime.now())
                .build();

        Page<Comment> commentPage = new Page<>(1, 10, 1);
        commentPage.setRecords(List.of(replyComment));

        doReturn(commentPage).when(commentService).page(any(Page.class), any(LambdaQueryWrapper.class));

        User user = User.builder().id(1L).username("user1").realName("用户1").build();
        when(userService.listByIds(Set.of(1L))).thenReturn(List.of(user));

        Page<CommentResponse> result = commentService.getPage("article", 1L, 100L, 1, 10, "latest");

        assertThat(result.getRecords()).hasSize(1);
        assertThat(result.getRecords().get(0).getParentId()).isEqualTo(100L);
    }

    @Test
    @DisplayName("getPage - 空结果")
    void getPageWithEmptyResult() {
        Page<Comment> emptyPage = new Page<>(1, 10, 0);
        emptyPage.setRecords(Collections.emptyList());

        doReturn(emptyPage).when(commentService).page(any(Page.class), any(LambdaQueryWrapper.class));

        Page<CommentResponse> result = commentService.getPage("article", 1L, null, 1, 10, "latest");

        assertThat(result.getRecords()).isEmpty();
        assertThat(result.getTotal()).isZero();
    }

    @Test
    @DisplayName("getPage - 用户不存在时 username/realName 为空字符串")
    void getPageWhenUserNotFound() {
        Comment comment = Comment.builder()
                .id(1L)
                .targetType("article")
                .targetId(1L)
                .userId(999L)
                .content("幽灵用户评论")
                .status("published")
                .likeCount(0)
                .replyCount(0)
                .createdAt(LocalDateTime.now())
                .build();

        Page<Comment> commentPage = new Page<>(1, 10, 1);
        commentPage.setRecords(List.of(comment));

        doReturn(commentPage).when(commentService).page(any(Page.class), any(LambdaQueryWrapper.class));
        when(userService.listByIds(Set.of(999L))).thenReturn(Collections.emptyList());

        Page<CommentResponse> result = commentService.getPage("article", 1L, null, 1, 10, "latest");

        assertThat(result.getRecords()).hasSize(1);
        assertThat(result.getRecords().get(0).getUsername()).isEmpty();
        assertThat(result.getRecords().get(0).getRealName()).isEmpty();
    }
}
