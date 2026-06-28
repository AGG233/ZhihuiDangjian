package com.rauio.smartdangjian.security;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.MockedStatic;

import com.rauio.smartdangjian.server.graph.pojo.response.KnowledgeGraphResponse;
import com.rauio.smartdangjian.server.resource.pojo.entity.ResourceMeta;

import cn.dev33.satoken.stp.StpUtil;

@DisplayName("真实权限码鉴权拦截链测试")
class SecurityPermissionAuthorizationIntegrationTest extends AbstractSecurityAuthorizationIntegrationTest {

    @ParameterizedTest(name = "{0} 缺少 {2} 权限返回 403")
    @MethodSource("forbiddenPermissionEndpointProvider")
    @DisplayName("缺少权限码访问受限接口返回 403")
    void forbiddenPermissionReturns403(String path, String method, String requiredPermission) throws Exception {
        try (MockedStatic<StpUtil> stpUtil = mockStatic(StpUtil.class);
                AnnotationHandlerScope ignored = annotationHandlerScope(
                        allowingLoginHandler(),
                        allowingRoleHandler(null),
                        rejectingPermissionHandler(requiredPermission))) {
            stpUtil.when(StpUtil::checkLogin).thenAnswer(invocation -> null);
            stpUtil.when(StpUtil::getLoginIdAsString).thenReturn("1");

            perform(path, method)
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.code").value("403"))
                    .andExpect(jsonPath("$.message").value("无权限执行该操作"));
        }
    }

    static Stream<Arguments> forbiddenPermissionEndpointProvider() {
        return Stream.of(
                Arguments.of("/api/resource/files/confirm/1", "POST", "file:write"),
                Arguments.of("/api/resource/files/1", "DELETE", "file:delete"),
                Arguments.of("/api/graph/knowledge-graphs/users/user-1", "GET", "graph:read"),
                Arguments.of("/api/user/users/1", "PUT", "user:update"),
                Arguments.of("/api/content/courses/learned/me", "GET", "course:read"),
                Arguments.of("/api/quiz/answers/me", "GET", "quiz:read"),
                Arguments.of("/api/quiz/answers/me/quizzes/1", "GET", "quiz:read"),
                Arguments.of("/api/quiz/answers/me/quizzes/1/options/1", "GET", "quiz:read"),
                Arguments.of("/api/quiz/answers/me/quizzes/1/options/1", "POST", "quiz:answer"),
                Arguments.of("/api/quiz/answers/me/quizzes/1/options/1", "PUT", "quiz:answer"));
    }

    @ParameterizedTest(name = "{0} 权限满足时返回 200")
    @MethodSource("allowedPermissionEndpointProvider")
    @DisplayName("正确权限码访问受限接口成功")
    void allowedPermissionReturns200(String path, String method, String requiredPermission) throws Exception {
        when(fileService.confirmUpload(1L))
                .thenReturn(ResourceMeta.builder().id(1L).status(1).build());
        when(knowledgeGraphService.getUserGraph("user-1"))
                .thenReturn(KnowledgeGraphResponse.builder()
                        .nodes(java.util.List.of())
                        .edges(java.util.List.of())
                        .build());
        when(userService.getCurrentUserId()).thenReturn("1");
        when(courseService.getByUserId(1L)).thenReturn(java.util.List.of());
        when(userQuizAnswerService.getByUserId(1L)).thenReturn(java.util.List.of());
        when(userQuizAnswerService.getByUserIdAndQuizId(anyLong(), anyLong())).thenReturn(List.of());
        when(userQuizAnswerService.updateByUserIdAndQuizIdAndOptionId(anyLong(), anyLong(), anyLong()))
                .thenReturn(true);

        try (MockedStatic<StpUtil> stpUtil = mockStatic(StpUtil.class);
                AnnotationHandlerScope ignored = annotationHandlerScope(
                        allowingLoginHandler(),
                        allowingRoleHandler(null),
                        allowingPermissionHandler(requiredPermission))) {
            stpUtil.when(StpUtil::checkLogin).thenAnswer(invocation -> null);
            stpUtil.when(StpUtil::isLogin).thenReturn(true);
            stpUtil.when(StpUtil::getLoginIdAsString).thenReturn("1");

            perform(path, method)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"));
        }
    }

    static Stream<Arguments> allowedPermissionEndpointProvider() {
        return Stream.of(
                Arguments.of("/api/resource/files/confirm/1", "POST", "file:write"),
                Arguments.of("/api/resource/files/1", "DELETE", "file:delete"),
                Arguments.of("/api/graph/knowledge-graphs/users/user-1", "GET", "graph:read"),
                Arguments.of("/api/user/users/1", "PUT", "user:update"),
                Arguments.of("/api/content/courses/learned/me", "GET", "course:read"),
                Arguments.of("/api/quiz/answers/me", "GET", "quiz:read"),
                Arguments.of("/api/quiz/answers/me/quizzes/1", "GET", "quiz:read"),
                Arguments.of("/api/quiz/answers/me/quizzes/1/options/1", "GET", "quiz:read"),
                Arguments.of("/api/quiz/answers/me/quizzes/1/options/1", "POST", "quiz:answer"),
                Arguments.of("/api/quiz/answers/me/quizzes/1/options/1", "PUT", "quiz:answer"));
    }

    @ParameterizedTest(name = "{0} 缺少 {2} 权限时不调用业务服务")
    @MethodSource("forbiddenPermissionEndpointProvider")
    @DisplayName("缺少权限码时不会调用业务服务")
    void forbiddenPermissionDoesNotCallService(String path, String method, String requiredPermission) throws Exception {
        try (MockedStatic<StpUtil> stpUtil = mockStatic(StpUtil.class);
                AnnotationHandlerScope ignored = annotationHandlerScope(
                        allowingLoginHandler(),
                        allowingRoleHandler(null),
                        rejectingPermissionHandler(requiredPermission))) {
            stpUtil.when(StpUtil::checkLogin).thenAnswer(invocation -> null);
            stpUtil.when(StpUtil::getLoginIdAsString).thenReturn("1");

            perform(path, method)
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.code").value("403"));

            verifyServiceNotCalled(requiredPermission);
        }
    }

    private void verifyServiceNotCalled(String requiredPermission) {
        switch (requiredPermission) {
            case "file:write" ->
                verify(fileService, org.mockito.Mockito.never()).confirmUpload(org.mockito.ArgumentMatchers.anyLong());
            case "file:delete" ->
                verify(fileService, org.mockito.Mockito.never()).delete(org.mockito.ArgumentMatchers.anyLong());
            case "graph:read" ->
                verify(knowledgeGraphService, org.mockito.Mockito.never())
                        .getUserGraph(org.mockito.ArgumentMatchers.anyString());
            case "user:update" ->
                verify(userService, org.mockito.Mockito.never())
                        .update(
                                org.mockito.ArgumentMatchers.anyLong(),
                                org.mockito.ArgumentMatchers.any(
                                        com.rauio.smartdangjian.server.user.pojo.entity.User.class));
            case "course:read" ->
                verify(courseService, org.mockito.Mockito.never()).getByUserId(org.mockito.ArgumentMatchers.anyLong());
            case "quiz:read" ->
                verify(userQuizAnswerService, org.mockito.Mockito.never())
                        .getByUserId(org.mockito.ArgumentMatchers.anyLong());
            case "quiz:answer" ->
                verify(userQuizAnswerService, org.mockito.Mockito.never()).create(org.mockito.ArgumentMatchers.any());
            default -> throw new IllegalArgumentException("Unsupported permission: " + requiredPermission);
        }
    }
}
