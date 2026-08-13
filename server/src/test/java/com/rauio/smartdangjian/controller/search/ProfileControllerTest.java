package com.rauio.smartdangjian.controller.search;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.lang.reflect.Method;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.rauio.smartdangjian.BaseControllerTest;
import com.rauio.smartdangjian.exception.BusinessException;
import com.rauio.smartdangjian.server.search.controller.ProfileController;
import com.rauio.smartdangjian.server.search.pojo.response.DynamicProfileResponse;
import com.rauio.smartdangjian.server.search.pojo.response.LearningSummaryResponse;
import com.rauio.smartdangjian.server.search.service.UserProfileService;

import cn.dev33.satoken.annotation.SaCheckRole;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK, classes = ProfileControllerTest.TestConfig.class)
@DisplayName("用户画像接口测试")
class ProfileControllerTest extends BaseControllerTest {

    @SpringBootConfiguration
    static class TestConfig extends CommonTestConfig {

        @Bean
        public ProfileController profileController(UserProfileService userProfileService) {
            return new ProfileController(userProfileService);
        }
    }

    @MockitoBean
    private UserProfileService userProfileService;

    @Test
    @DisplayName("GET /api/profile/learning-summary - 返回理论+政策维度")
    void learningSummaryReturnsDimensions() throws Exception {
        LearningSummaryResponse summary = LearningSummaryResponse.builder()
                .theory(LearningSummaryResponse.TheoryDimension.builder()
                        .totalDuration(900)
                        .completionRate(0.5)
                        .build())
                .policyComprehension(LearningSummaryResponse.PolicyDimension.builder()
                        .avgCorrectRate(2.0 / 3.0)
                        .totalAnswers(3)
                        .build())
                .build();
        when(userProfileService.getCurrentUserLearningSummary()).thenReturn(summary);

        mockMvc.perform(get("/api/profile/learning-summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.data.theory.totalDuration").value(900))
                .andExpect(jsonPath("$.data.theory.completionRate").value(0.5))
                .andExpect(jsonPath("$.data.policyComprehension.totalAnswers").value(3));
    }

    @Test
    @DisplayName("GET /api/profile/dynamic - 返回热点标签/趋势/薄弱域结构")
    void dynamicProfileReturnsStructure() throws Exception {
        DynamicProfileResponse dynamic = DynamicProfileResponse.builder()
                .hotTags(List.of(DynamicProfileResponse.HotTag.builder()
                        .tag("党章学习")
                        .count(2L)
                        .build()))
                .growthTrend(List.of(DynamicProfileResponse.GrowthTrend.builder()
                        .weekStart(LocalDate.of(2026, 8, 10))
                        .studyDuration(400)
                        .quizAccuracy(0.5)
                        .build()))
                .weakDomains(List.of(DynamicProfileResponse.WeakDomain.builder()
                        .type("DIFFICULTY")
                        .name("hard")
                        .accuracy(0.33)
                        .build()))
                .build();
        when(userProfileService.getCurrentUserDynamicProfile()).thenReturn(dynamic);

        mockMvc.perform(get("/api/profile/dynamic"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.data.hotTags[0].tag").value("党章学习"))
                .andExpect(jsonPath("$.data.growthTrend[0].studyDuration").value(400))
                .andExpect(jsonPath("$.data.weakDomains[0].type").value("DIFFICULTY"));
    }

    @Test
    @DisplayName("learning-summary 与 dynamic 端点均声明 @SaCheckRole(STUDENT)")
    void endpointsRequireStudentRole() throws Exception {
        assertStudentRole("getLearningSummary");
        assertStudentRole("getDynamicProfile");
    }

    @Test
    @DisplayName("Service 抛出 BusinessException 返回 400 业务错误码")
    void serviceThrowsBusinessException() throws Exception {
        when(userProfileService.getCurrentUserLearningSummary()).thenThrow(new BusinessException(8000, "画像服务异常"));

        mockMvc.perform(get("/api/profile/learning-summary"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("8000"))
                .andExpect(jsonPath("$.message").value("画像服务异常"));
    }

    private static void assertStudentRole(String methodName) throws Exception {
        Method method = ProfileController.class.getDeclaredMethod(methodName);
        SaCheckRole role = method.getAnnotation(SaCheckRole.class);
        assertThat(role)
                .as("ProfileController#" + methodName + " must declare @SaCheckRole")
                .isNotNull();
        assertThat(Arrays.asList(role.value())).contains("STUDENT");
    }
}
