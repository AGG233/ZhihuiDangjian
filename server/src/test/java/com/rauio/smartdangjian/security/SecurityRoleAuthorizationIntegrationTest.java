package com.rauio.smartdangjian.security;

import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.MockedStatic;

import com.rauio.smartdangjian.server.content.pojo.response.CategoryResponse;
import com.rauio.smartdangjian.server.content.pojo.response.ChapterResponse;
import com.rauio.smartdangjian.server.graph.pojo.response.KnowledgeGraphResponse;
import com.rauio.smartdangjian.server.learning.pojo.response.UserLearningRecordResponse;
import com.rauio.smartdangjian.server.search.pojo.response.LearningTrendResponse;
import com.rauio.smartdangjian.server.search.pojo.response.UserProfileResponse;

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
                Arguments.of("/api/admin/users/1", "GET", "SCHOOL"),
                Arguments.of("/api/search/profile", "GET", "STUDENT"),
                Arguments.of("/api/admin/content/content-blocks/carousel/1", "DELETE", "MANAGER"),
                Arguments.of("/api/admin/content/chapters/1", "GET", "SCHOOL"),
                Arguments.of("/api/admin/content/courses/1", "DELETE", "SCHOOL"),
                Arguments.of("/api/admin/quiz/quizzes/1", "DELETE", "SCHOOL"),
                Arguments.of("/api/admin/quiz/answers/users/1/quizzes/1/options/1", "DELETE", "MANAGER"),
                Arguments.of("/api/quiz/quizzes/1", "GET", "STUDENT"),
                Arguments.of("/api/admin/ai/prompts", "GET", "MANAGER"),
                Arguments.of("/api/admin/ai/skills", "GET", "MANAGER"),
                Arguments.of("/api/admin/ai/faqs", "GET", "MANAGER"),
                Arguments.of("/api/admin/learning/records/chapter/1", "GET", "SCHOOL"),
                Arguments.of("/api/admin/learning/progress/chapter/1", "GET", "SCHOOL"),
                Arguments.of("/api/admin/resource/files/1", "GET", "SCHOOL"),
                Arguments.of("/api/admin/resource/banners", "GET", "MANAGER"),
                Arguments.of("/api/user/users/1", "GET", "STUDENT"),
                Arguments.of("/api/content/courses/learned/1", "GET", "STUDENT"),
                Arguments.of("/api/quiz/answers/users/1", "GET", "STUDENT"),
                Arguments.of("/api/ai/chat/session-1/messages", "GET", "STUDENT"),
                Arguments.of("/api/admin/content/categories/root", "POST", "SCHOOL"),
                Arguments.of("/api/content/categories/1", "GET", "STUDENT"),
                Arguments.of("/api/learning/hotspots/trends", "GET", "STUDENT"),
                Arguments.of("/api/graph/party-history/admin/entities/person-1", "DELETE", "MANAGER"),
                Arguments.of("/api/graph/party-history/search?keyword=history", "GET", "STUDENT"),
                Arguments.of("/api/learning/records/1", "GET", "STUDENT"),
                Arguments.of("/api/content/chapters/1", "GET", "STUDENT"),
                Arguments.of("/api/learning/graph/users/1/sync", "POST", "STUDENT"));
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
                Arguments.of("/api/content/courses/learned/1", "GET", "STUDENT"),
                Arguments.of("/api/quiz/answers/users/1", "GET", "STUDENT"),
                Arguments.of("/api/ai/chat/session-1/messages", "GET", "STUDENT"),
                Arguments.of("/api/learning/records/1", "GET", "STUDENT"),
                Arguments.of("/api/content/categories/1", "GET", "STUDENT"),
                Arguments.of("/api/learning/hotspots/trends", "GET", "STUDENT"),
                Arguments.of("/api/content/categories/1/children", "GET", "STUDENT"),
                Arguments.of("/api/graph/party-history/search?keyword=history", "GET", "STUDENT"),
                Arguments.of("/api/learning/records/users/1", "GET", "STUDENT"),
                Arguments.of("/api/content/categories/root", "GET", "STUDENT"),
                Arguments.of("/api/content/chapters/1", "GET", "STUDENT"),
                Arguments.of("/api/learning/graph/users/1/sync", "POST", "STUDENT"));
    }


}
