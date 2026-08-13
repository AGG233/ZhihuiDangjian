package com.rauio.smartdangjian.controller.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.rauio.smartdangjian.BaseControllerTest;
import com.rauio.smartdangjian.constants.ErrorConstants;
import com.rauio.smartdangjian.exception.BusinessException;
import com.rauio.smartdangjian.server.quiz.constants.QuizErrorConstants;
import com.rauio.smartdangjian.server.quiz.controller.user.UserScormController;
import com.rauio.smartdangjian.server.quiz.pojo.entity.ScormRegistration;
import com.rauio.smartdangjian.server.quiz.pojo.request.ScormSubmitRequest;
import com.rauio.smartdangjian.server.quiz.pojo.response.ScormSummaryResponse;
import com.rauio.smartdangjian.server.quiz.service.scorm.ScormRegistrationService;

import cn.dev33.satoken.annotation.SaCheckRole;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK, classes = UserScormControllerTest.TestConfig.class)
@DisplayName("SCORM学习进度接口测试")
class UserScormControllerTest extends BaseControllerTest {

    @SpringBootConfiguration
    static class TestConfig extends CommonTestConfig {
        @Bean
        public UserScormController userScormController(ScormRegistrationService scormRegistrationService) {
            return new UserScormController(scormRegistrationService);
        }
    }

    @MockitoBean
    private ScormRegistrationService scormRegistrationService;

    private static String submitJson(String scoIdentifier, String lessonStatus, String scoreRaw) {
        return "{\"scoIdentifier\":\"" + scoIdentifier
                + "\",\"lessonStatus\":\"" + lessonStatus
                + "\",\"scoreRaw\":" + scoreRaw + "}";
    }

    @Nested
    @DisplayName("正常场景")
    class NormalTests {

        @Test
        @DisplayName("POST /api/scorm/packages/{packageId}/registration - 上报成功")
        void submitRegistrationSuccess() throws Exception {
            ScormRegistration registration = ScormRegistration.builder().id(1L).build();
            when(scormRegistrationService.submit(eq(1L), any(ScormSubmitRequest.class)))
                    .thenReturn(registration);

            mockMvc.perform(post("/api/scorm/packages/1/registration")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(submitJson("sco-1", "completed", "85.00")))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"))
                    .andExpect(jsonPath("$.data").value(true));
            verify(scormRegistrationService).submit(eq(1L), any(ScormSubmitRequest.class));
        }

        @Test
        @DisplayName("GET /api/scorm/packages/{packageId}/summary - 返回个人汇总")
        void getSummarySuccess() throws Exception {
            ScormSummaryResponse summary = ScormSummaryResponse.builder()
                    .packageId(1L)
                    .title("党史学习课程")
                    .registrationCount(2)
                    .completedCount(1)
                    .avgScore(new BigDecimal("85.00"))
                    .build();
            when(scormRegistrationService.getSummary(1L)).thenReturn(List.of(summary));

            mockMvc.perform(get("/api/scorm/packages/1/summary"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"))
                    .andExpect(jsonPath("$.data.packageId").value("1"))
                    .andExpect(jsonPath("$.data.title").value("党史学习课程"))
                    .andExpect(jsonPath("$.data.registrationCount").value(2))
                    .andExpect(jsonPath("$.data.completedCount").value(1))
                    .andExpect(jsonPath("$.data.avgScore").value(85.00));
        }
    }

    @Nested
    @DisplayName("异常处理场景")
    class ErrorTests {

        @Test
        @DisplayName("POST registration - 学习包不存在返回 400 业务错误码")
        void submitPackageNotFoundReturnsBusinessError() throws Exception {
            when(scormRegistrationService.submit(eq(999L), any(ScormSubmitRequest.class)))
                    .thenThrow(new BusinessException(QuizErrorConstants.SCORM_PACKAGE_NOT_FOUND, "SCORM 学习包不存在"));

            mockMvc.perform(post("/api/scorm/packages/999/registration")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(submitJson("sco-1", "completed", "85.00")))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value(String.valueOf(QuizErrorConstants.SCORM_PACKAGE_NOT_FOUND)))
                    .andExpect(jsonPath("$.message").value("SCORM 学习包不存在"));
        }

        @Test
        @DisplayName("POST registration - 成绩上报保存失败返回 400 业务错误码")
        void submitSaveFailedReturnsBusinessError() throws Exception {
            when(scormRegistrationService.submit(eq(1L), any(ScormSubmitRequest.class)))
                    .thenThrow(
                            new BusinessException(QuizErrorConstants.SCORM_REGISTRATION_SAVE_FAILED, "SCORM 成绩上报保存失败"));

            mockMvc.perform(post("/api/scorm/packages/1/registration")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(submitJson("sco-1", "completed", "85.00")))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code")
                            .value(String.valueOf(QuizErrorConstants.SCORM_REGISTRATION_SAVE_FAILED)));
        }

        @Test
        @DisplayName("GET summary - 该包无学习记录返回 400 业务错误码")
        void getSummaryNoRegistrationReturnsBusinessError() throws Exception {
            when(scormRegistrationService.getSummary(1L)).thenReturn(List.of());

            mockMvc.perform(get("/api/scorm/packages/1/summary"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value(String.valueOf(QuizErrorConstants.SCORM_PACKAGE_NOT_FOUND)));
        }

        @Test
        @DisplayName("GET summary - 未登录返回 400 未授权错误码")
        void getSummaryWithoutLoginReturnsNotAuthorized() throws Exception {
            setAnonymousContext();

            mockMvc.perform(get("/api/scorm/packages/1/summary"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value(String.valueOf(ErrorConstants.RESOURCE_NOT_AUTHORIZED)))
                    .andExpect(jsonPath("$.message").value("请先登录"));
        }
    }

    @Nested
    @DisplayName("安全场景")
    class SecurityTests {

        @Test
        @DisplayName("控制器要求 STUDENT 角色")
        void controllerRequiresStudentRole() {
            SaCheckRole role = UserScormController.class.getAnnotation(SaCheckRole.class);
            assertThat(role).as("UserScormController must declare @SaCheckRole").isNotNull();
            assertThat(Arrays.asList(role.value())).contains("STUDENT");
        }

        @Test
        @DisplayName("DELETE 请求上报接口返回 405")
        void submitWithWrongMethod() throws Exception {
            mockMvc.perform(delete("/api/scorm/packages/1/registration")).andExpect(status().isMethodNotAllowed());
        }
    }
}
