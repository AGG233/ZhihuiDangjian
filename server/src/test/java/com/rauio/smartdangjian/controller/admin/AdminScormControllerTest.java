package com.rauio.smartdangjian.controller.admin;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.Arrays;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.rauio.smartdangjian.BaseControllerTest;
import com.rauio.smartdangjian.exception.BusinessException;
import com.rauio.smartdangjian.server.quiz.constants.QuizErrorConstants;
import com.rauio.smartdangjian.server.quiz.controller.admin.AdminScormController;
import com.rauio.smartdangjian.server.quiz.pojo.entity.ScormPackage;
import com.rauio.smartdangjian.server.quiz.service.scorm.ScormPackageService;

import cn.dev33.satoken.annotation.SaCheckRole;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.MOCK,
        classes = AdminScormControllerTest.TestConfig.class)
@DisplayName("SCORM学习包管理接口测试")
class AdminScormControllerTest extends BaseControllerTest {

    @SpringBootConfiguration
    static class TestConfig extends CommonTestConfig {
        @Bean
        public AdminScormController adminScormController(ScormPackageService scormPackageService) {
            return new AdminScormController(scormPackageService);
        }
    }

    @MockitoBean
    private ScormPackageService scormPackageService;

    @Nested
    @DisplayName("正常场景")
    class NormalTests {

        @Test
        @DisplayName("POST /api/scorm/admin/packages - 上传 zip 解析成功返回学习包信息")
        void uploadPackageSuccess() throws Exception {
            ScormPackage scormPackage = ScormPackage.builder()
                    .id(1L)
                    .title("党史学习课程")
                    .version("2004")
                    .identifier("party-course-1")
                    .createdAt(LocalDateTime.of(2026, 8, 13, 10, 0))
                    .build();
            when(scormPackageService.parseAndSave(any())).thenReturn(scormPackage);

            MockMultipartFile file =
                    new MockMultipartFile("file", "course.zip", "application/zip", new byte[] {1, 2, 3});

            mockMvc.perform(multipart("/api/scorm/admin/packages").file(file))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"))
                    .andExpect(jsonPath("$.data.id").value("1"))
                    .andExpect(jsonPath("$.data.title").value("党史学习课程"))
                    .andExpect(jsonPath("$.data.version").value("2004"))
                    .andExpect(jsonPath("$.data.identifier").value("party-course-1"));
            verify(scormPackageService).parseAndSave(any());
        }
    }

    @Nested
    @DisplayName("异常处理场景")
    class ErrorTests {

        @Test
        @DisplayName("POST /api/scorm/admin/packages - 非 zip 文件返回 400 业务错误码")
        void uploadNonZipReturnsBusinessError() throws Exception {
            when(scormPackageService.parseAndSave(any()))
                    .thenThrow(
                            new BusinessException(QuizErrorConstants.SCORM_PACKAGE_INVALID, "仅支持 .zip 格式的 SCORM 学习包"));

            MockMultipartFile file = new MockMultipartFile("file", "course.txt", "text/plain", new byte[] {1});

            mockMvc.perform(multipart("/api/scorm/admin/packages").file(file))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value(String.valueOf(QuizErrorConstants.SCORM_PACKAGE_INVALID)))
                    .andExpect(jsonPath("$.message").value("仅支持 .zip 格式的 SCORM 学习包"));
        }

        @Test
        @DisplayName("POST /api/scorm/admin/packages - 解析失败返回 400 业务错误码")
        void uploadParseFailedReturnsBusinessError() throws Exception {
            when(scormPackageService.parseAndSave(any()))
                    .thenThrow(new BusinessException(QuizErrorConstants.SCORM_PARSE_FAILED, "SCORM 包解析失败"));

            MockMultipartFile file = new MockMultipartFile("file", "broken.zip", "application/zip", new byte[] {1, 2});

            mockMvc.perform(multipart("/api/scorm/admin/packages").file(file))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value(String.valueOf(QuizErrorConstants.SCORM_PARSE_FAILED)));
        }

        @Test
        @DisplayName("POST /api/scorm/admin/packages - 缺少 file 请求部分返回 400")
        void uploadWithoutFilePartReturnsBadRequest() throws Exception {
            mockMvc.perform(multipart("/api/scorm/admin/packages")).andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("安全场景")
    class SecurityTests {

        @Test
        @DisplayName("控制器要求 MANAGER 角色")
        void controllerRequiresManagerRole() {
            SaCheckRole role = AdminScormController.class.getAnnotation(SaCheckRole.class);
            assertThat(role)
                    .as("AdminScormController must declare @SaCheckRole")
                    .isNotNull();
            assertThat(Arrays.asList(role.value())).contains("MANAGER");
        }

        @Test
        @DisplayName("GET 请求上传接口返回 405")
        void uploadWithWrongMethod() throws Exception {
            mockMvc.perform(get("/api/scorm/admin/packages")).andExpect(status().isMethodNotAllowed());
        }
    }
}
