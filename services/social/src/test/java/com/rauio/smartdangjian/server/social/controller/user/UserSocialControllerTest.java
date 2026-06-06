package com.rauio.smartdangjian.server.social.controller.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rauio.smartdangjian.security.CurrentUserProvider;
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
    }
}
