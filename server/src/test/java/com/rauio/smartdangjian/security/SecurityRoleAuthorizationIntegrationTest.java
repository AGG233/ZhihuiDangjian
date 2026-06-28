package com.rauio.smartdangjian.security;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.MockedStatic;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rauio.smartdangjian.server.category.pojo.response.CategoryResponse;
import com.rauio.smartdangjian.server.chapter.pojo.response.ChapterResponse;
import com.rauio.smartdangjian.server.graph.pojo.response.KnowledgeGraphResponse;
import com.rauio.smartdangjian.server.learning.pojo.response.UserLearningRecordResponse;
import com.rauio.smartdangjian.server.search.pojo.response.LearningTrendResponse;
import com.rauio.smartdangjian.server.search.pojo.response.UserProfileResponse;
import com.rauio.smartdangjian.server.social.pojo.response.LikeStatusResponse;

import cn.dev33.satoken.stp.StpUtil;

@DisplayName("真实角色鉴权拦截链测试")
class SecurityRoleAuthorizationIntegrationTest extends AbstractSecurityAuthorizationIntegrationTest {

    @ParameterizedTest(name = "{0} 需要 {2} 角色，低权限用户返回 403")
    @MethodSource("forbiddenEndpointProvider")
    @DisplayName("低权限角色访问受限接口返回 403")
    void forbiddenRoleReturns403(String path, String method, String requiredRole) throws Exception {
        try (MockedStatic<StpUtil> stpUtil = mockStatic(StpUtil.class);
                AnnotationHandlerScope ignored = annotationHandlerScope(
                        allowingLoginHandler(), rejectingRoleHandler(requiredRole), allowingPermissionHandler(null))) {
            stpUtil.when(StpUtil::checkLogin).thenAnswer(invocation -> null);

            perform(path, method)
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.code").value("403"))
                    .andExpect(jsonPath("$.message").value("无权限访问该资源"));
        }
    }

    static Stream<Arguments> forbiddenEndpointProvider() {
        return Stream.of(
                // Admin CRUD GET endpoints
                Arguments.of("/api/admin/users/1", "GET", "SCHOOL"),
                Arguments.of("/api/search/profile", "GET", "STUDENT"),
                Arguments.of("/api/admin/content/chapters/1", "GET", "SCHOOL"),
                Arguments.of("/api/admin/content/courses/1", "GET", "SCHOOL"),
                Arguments.of("/api/admin/quiz/quizzes/1", "GET", "SCHOOL"),
                Arguments.of("/api/admin/ai/prompts", "GET", "MANAGER"),
                Arguments.of("/api/admin/ai/skills", "GET", "MANAGER"),
                Arguments.of("/api/admin/ai/faqs", "GET", "MANAGER"),
                Arguments.of("/api/admin/learning/records/chapter/1", "GET", "SCHOOL"),
                Arguments.of("/api/admin/learning/progress/chapter/1", "GET", "SCHOOL"),
                Arguments.of("/api/admin/resource/files/1", "GET", "SCHOOL"),
                Arguments.of("/api/admin/resource/banners", "GET", "MANAGER"),
                // Admin CRUD POST endpoints
                Arguments.of("/api/admin/users", "POST", "SCHOOL"),
                Arguments.of("/api/admin/users/search", "POST", "SCHOOL"),
                Arguments.of("/api/admin/content/categories/root", "POST", "SCHOOL"),
                Arguments.of("/api/admin/content/categories/1/children", "POST", "SCHOOL"),
                Arguments.of("/api/admin/content/chapters", "POST", "SCHOOL"),
                Arguments.of("/api/admin/content/courses", "POST", "SCHOOL"),
                Arguments.of("/api/admin/resource/files", "POST", "SCHOOL"),
                Arguments.of("/api/admin/resource/banners", "POST", "MANAGER"),
                Arguments.of("/api/admin/ai/prompts", "POST", "MANAGER"),
                Arguments.of("/api/admin/ai/skills", "POST", "MANAGER"),
                Arguments.of("/api/admin/ai/faqs", "POST", "MANAGER"),
                // Admin CRUD PUT endpoints
                Arguments.of("/api/admin/users/1", "PUT", "SCHOOL"),
                Arguments.of("/api/admin/content/categories/1", "PUT", "SCHOOL"),
                Arguments.of("/api/admin/content/chapters", "PUT", "SCHOOL"),
                Arguments.of("/api/admin/content/courses/1", "PUT", "SCHOOL"),
                Arguments.of("/api/admin/resource/files/1", "PUT", "SCHOOL"),
                Arguments.of("/api/admin/resource/banners/1", "PUT", "MANAGER"),
                Arguments.of("/api/admin/ai/prompts/1", "PUT", "MANAGER"),
                Arguments.of("/api/admin/ai/skills/1", "PUT", "MANAGER"),
                Arguments.of("/api/admin/ai/faqs/1", "PUT", "MANAGER"),
                // Admin CRUD DELETE endpoints
                Arguments.of("/api/admin/users/1", "DELETE", "SCHOOL"),
                Arguments.of("/api/admin/content/categories/1", "DELETE", "SCHOOL"),
                Arguments.of("/api/admin/content/categories/1/all", "DELETE", "SCHOOL"),
                Arguments.of("/api/admin/content/chapters/1", "DELETE", "SCHOOL"),
                Arguments.of("/api/admin/content/courses/1", "DELETE", "SCHOOL"),
                Arguments.of("/api/admin/resource/files/1", "DELETE", "SCHOOL"),
                Arguments.of("/api/admin/resource/files/by-hash/test", "DELETE", "SCHOOL"),
                Arguments.of("/api/admin/resource/banners/1", "DELETE", "MANAGER"),
                Arguments.of("/api/admin/ai/prompts/1", "DELETE", "MANAGER"),
                Arguments.of("/api/admin/ai/skills/1", "DELETE", "MANAGER"),
                Arguments.of("/api/admin/ai/faqs/1", "DELETE", "MANAGER"),
                Arguments.of("/api/admin/learning/records/1", "DELETE", "SCHOOL"),
                Arguments.of("/api/admin/learning/progress/1", "DELETE", "SCHOOL"),
                Arguments.of("/api/admin/quiz/answers/users/1/quizzes/1/options/1", "DELETE", "MANAGER"),
                // Quiz endpoints
                Arguments.of("/api/quiz/quizzes/1", "GET", "STUDENT"),
                // User endpoints
                Arguments.of("/api/user/users/1", "GET", "STUDENT"),
                // Content endpoints
                Arguments.of("/api/content/courses/learned/me", "GET", "STUDENT"),
                Arguments.of("/api/content/chapters/1", "GET", "STUDENT"),
                // Graph endpoints
                Arguments.of("/api/graph/party-history/search?keyword=history", "GET", "STUDENT"),
                // Learning endpoints
                Arguments.of("/api/learning/records/me/1", "GET", "STUDENT"),
                Arguments.of("/api/learning/graph/me/sync", "POST", "STUDENT"),
                // UserSocialController endpoints (STUDENT)
                Arguments.of("/api/social/article/1/comments", "GET", "STUDENT"),
                Arguments.of("/api/social/article/1/comments", "POST", "STUDENT"),
                Arguments.of("/api/social/comments/1/replies", "POST", "STUDENT"),
                Arguments.of("/api/social/comments/1", "DELETE", "STUDENT"),
                Arguments.of("/api/social/article/1/like", "POST", "STUDENT"),
                Arguments.of("/api/social/article/1/like/status", "GET", "STUDENT"));
    }

    @ParameterizedTest(name = "{0} 角色满足时返回 200")
    @MethodSource("allowedEndpointProvider")
    @DisplayName("正确角色访问受限接口成功")
    void allowedRoleReturns200(String path, String method, String requiredRole) throws Exception {
        when(userProfileService.getCurrentUserProfile())
                .thenReturn(UserProfileResponse.builder().userId("1").build());
        when(userService.getById(1L)).thenReturn(new com.rauio.smartdangjian.server.user.pojo.entity.User());
        when(userService.getCurrentUserId()).thenReturn("1");
        when(courseService.getByUserId(1L)).thenReturn(java.util.List.of());
        when(userQuizAnswerService.getByUserId(1L)).thenReturn(java.util.List.of());
        when(categoryService.create(org.mockito.ArgumentMatchers.any())).thenReturn(true);
        when(categoryService.get(1L)).thenReturn(new CategoryResponse());
        when(categoryService.getRootList()).thenReturn(java.util.List.of());
        when(categoryService.getByParentId(1L)).thenReturn(java.util.List.of());
        when(chapterService.get(1L)).thenReturn(ChapterResponse.builder().id(1L).build());
        when(learningRecordService.get(1L))
                .thenReturn(UserLearningRecordResponse.builder().id(1L).build());
        when(learningRecordService.getByUserId(1L)).thenReturn(java.util.List.of());
        when(learningRecordService.syncUserLearningGraph(1L)).thenReturn(1);
        when(learningHotspotService.getTrends(org.mockito.ArgumentMatchers.anyInt()))
                .thenReturn(LearningTrendResponse.builder()
                        .days(7)
                        .dailyData(java.util.List.of())
                        .build());
        when(partyHistoryQueryService.searchEntities(
                        org.mockito.ArgumentMatchers.anyString(),
                        org.mockito.ArgumentMatchers.any(),
                        org.mockito.ArgumentMatchers.anyInt(),
                        org.mockito.ArgumentMatchers.anyInt()))
                .thenReturn(KnowledgeGraphResponse.builder()
                        .nodes(java.util.List.of())
                        .edges(java.util.List.of())
                        .build());
        when(commentService.getPage(anyString(), anyLong(), any(), anyInt(), anyInt(), anyString()))
                .thenReturn(new Page<>());
        when(likeService.getStatus(anyLong(), anyString(), anyLong()))
                .thenReturn(LikeStatusResponse.builder().liked(true).build());
        when(likeService.toggle(anyLong(), anyString(), anyLong()))
                .thenReturn(LikeStatusResponse.builder().liked(true).build());

        try (MockedStatic<StpUtil> stpUtil = mockStatic(StpUtil.class);
                AnnotationHandlerScope ignored = annotationHandlerScope(
                        allowingLoginHandler(), allowingRoleHandler(requiredRole), allowingPermissionHandler(null))) {
            stpUtil.when(StpUtil::checkLogin).thenAnswer(invocation -> null);
            stpUtil.when(StpUtil::isLogin).thenReturn(true);
            stpUtil.when(StpUtil::getLoginIdAsString).thenReturn("1");

            perform(path, method)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"));
        }
    }

    static Stream<Arguments> allowedEndpointProvider() {
        return Stream.of(
                Arguments.of("/api/admin/users/1", "GET", "SCHOOL"),
                Arguments.of("/api/search/profile", "GET", "STUDENT"),
                Arguments.of("/api/admin/content/chapters/1", "GET", "SCHOOL"),
                Arguments.of("/api/quiz/quizzes/1", "GET", "STUDENT"),
                Arguments.of("/api/admin/ai/prompts", "GET", "MANAGER"),
                Arguments.of("/api/admin/ai/skills", "GET", "MANAGER"),
                Arguments.of("/api/admin/ai/faqs", "GET", "MANAGER"),
                Arguments.of("/api/admin/learning/records/chapter/1", "GET", "SCHOOL"),
                Arguments.of("/api/admin/learning/progress/chapter/1", "GET", "SCHOOL"),
                Arguments.of("/api/admin/resource/files/1", "GET", "SCHOOL"),
                Arguments.of("/api/admin/resource/banners", "GET", "MANAGER"),
                Arguments.of("/api/user/users/1", "GET", "STUDENT"),
                Arguments.of("/api/content/courses/learned/me", "GET", "STUDENT"),
                Arguments.of("/api/quiz/answers/me", "GET", "STUDENT"),
                Arguments.of("/api/ai/chat/session-1/messages", "GET", "STUDENT"),
                Arguments.of("/api/learning/records/me/1", "GET", "STUDENT"),
                Arguments.of("/api/content/categories/1", "GET", "STUDENT"),
                Arguments.of("/api/learning/hotspots/trends", "GET", "STUDENT"),
                Arguments.of("/api/resource/banners", "GET", "STUDENT"),
                Arguments.of("/api/content/categories/1/children", "GET", "STUDENT"),
                Arguments.of("/api/graph/party-history/search?keyword=history", "GET", "STUDENT"),
                Arguments.of("/api/learning/records/me", "GET", "STUDENT"),
                Arguments.of("/api/content/categories/root", "GET", "STUDENT"),
                Arguments.of("/api/content/chapters/1", "GET", "STUDENT"),
                Arguments.of("/api/learning/graph/me/sync", "POST", "STUDENT"),
                // UserSocialController endpoints (STUDENT)
                Arguments.of("/api/social/article/1/comments", "GET", "STUDENT"),
                Arguments.of("/api/social/article/1/like/status", "GET", "STUDENT"),
                Arguments.of("/api/social/article/1/like", "POST", "STUDENT"),
                Arguments.of("/api/social/comments/1", "DELETE", "STUDENT"));
    }
}
