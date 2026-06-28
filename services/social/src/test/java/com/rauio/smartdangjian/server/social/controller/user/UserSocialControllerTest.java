package com.rauio.smartdangjian.server.social.controller.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rauio.smartdangjian.security.CurrentUserProvider;
import com.rauio.smartdangjian.server.social.pojo.request.CommentRequest;
import com.rauio.smartdangjian.server.social.pojo.response.CommentResponse;
import com.rauio.smartdangjian.server.social.pojo.response.LikeStatusResponse;
import com.rauio.smartdangjian.server.social.service.CommentService;
import com.rauio.smartdangjian.server.social.service.LikeService;

@ExtendWith(MockitoExtension.class)
class UserSocialControllerTest {

    @Mock
    private CommentService commentService;

    @Mock
    private LikeService likeService;

    @Mock
    private CurrentUserProvider currentUserProvider;

    @InjectMocks
    private UserSocialController controller;

    @Captor
    private ArgumentCaptor<CommentRequest> requestCaptor;

    private static final Long USER_ID = 1L;

    @Nested
    @DisplayName("评论接口")
    class CommentTests {

        @Test
        @DisplayName("获取评论分页")
        void getComments() {
            Page<CommentResponse> page = new Page<>(1, 10);
            page.setRecords(List.of(CommentResponse.builder()
                    .id(1L)
                    .content("测试评论")
                    .userId(1L)
                    .username("test")
                    .realName("测试用户")
                    .likeCount(0)
                    .replyCount(0)
                    .replies(List.of())
                    .status("published")
                    .createdAt(LocalDateTime.now())
                    .build()));
            page.setTotal(1);
            when(commentService.getPage(anyString(), anyLong(), any(), anyInt(), anyInt(), anyString()))
                    .thenReturn(page);

            var result = controller.getComments("article", 1L, null, 1, 10, "latest");

            assertThat(result.getData().getRecords()).hasSize(1);
        }

        @Test
        @DisplayName("发表评论成功")
        void createComment() {
            when(currentUserProvider.getCurrentUserId()).thenReturn("1");
            CommentResponse response =
                    CommentResponse.builder().id(1L).content("新评论").userId(1L).build();
            when(commentService.create(eq(1L), any(CommentRequest.class))).thenReturn(response);

            CommentRequest request = CommentRequest.builder().content("新评论").build();
            var result = controller.createComment("article", 1L, request);

            assertThat(result.getData().getContent()).isEqualTo("新评论");
            assertThat(request.getTargetType()).isEqualTo("article");
            assertThat(request.getTargetId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("回复评论成功")
        void replyToComment() {
            when(currentUserProvider.getCurrentUserId()).thenReturn("1");
            CommentResponse response = CommentResponse.builder()
                    .id(2L)
                    .content("回复内容")
                    .parentId(1L)
                    .build();
            when(commentService.create(eq(1L), any(CommentRequest.class))).thenReturn(response);

            CommentRequest request = CommentRequest.builder().content("回复内容").build();
            var result = controller.reply(1L, request);

            assertThat(result.getData().getParentId()).isEqualTo(1L);
            assertThat(request.getParentId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("删除评论成功")
        void deleteComment() {
            when(currentUserProvider.getCurrentUserId()).thenReturn("1");
            doNothing().when(commentService).delete(1L, 1L);

            var result = controller.deleteComment(1L);

            assertThat(result.getCode()).isEqualTo("200");
            verify(commentService).delete(1L, 1L);
        }
    }

    @Nested
    @DisplayName("点赞接口")
    class LikeTests {

        @Test
        @DisplayName("查询点赞状态")
        void getLikeStatus() {
            when(currentUserProvider.getCurrentUserId()).thenReturn("1");
            var response = LikeStatusResponse.builder()
                    .liked(false)
                    .likeCount(5)
                    .targetType("comment")
                    .targetId(1L)
                    .build();
            when(likeService.getStatus(anyLong(), anyString(), anyLong())).thenReturn(response);

            var result = controller.getLikeStatus("comment", 1L);

            assertThat(result.getData().getLikeCount()).isEqualTo(5);
        }

        @Test
        @DisplayName("点赞/取消点赞切换")
        void toggleLike() {
            when(currentUserProvider.getCurrentUserId()).thenReturn("1");
            when(currentUserProvider.getCurrentUserId()).thenReturn("1");
            var response = LikeStatusResponse.builder()
                    .liked(true)
                    .likeCount(6)
                    .targetType("article")
                    .targetId(2L)
                    .build();
            when(likeService.toggle(anyLong(), anyString(), anyLong())).thenReturn(response);

            var result = controller.toggleLike("article", 2L);

            assertThat(result.getData().isLiked()).isTrue();
            assertThat(result.getData().getLikeCount()).isEqualTo(6);
            verify(likeService).toggle(1L, "article", 2L);
        }
    }
}
