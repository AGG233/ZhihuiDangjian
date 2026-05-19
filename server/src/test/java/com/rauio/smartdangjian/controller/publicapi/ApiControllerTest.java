package com.rauio.smartdangjian.controller.publicapi;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.rauio.smartdangjian.BaseControllerTest;
import com.rauio.smartdangjian.common.controller.publicapi.ApiController;
import com.rauio.smartdangjian.common.pojo.response.SchoolResponse;
import com.rauio.smartdangjian.common.service.UniversitiesService;
import com.rauio.smartdangjian.exception.BusinessException;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK, classes = ApiControllerTest.TestConfig.class)
@DisplayName("公共API接口测试")
class ApiControllerTest extends BaseControllerTest {

    @SpringBootConfiguration
    static class TestConfig extends CommonTestConfig {
        @Bean
        public ApiController apiController(UniversitiesService universitiesService) {
            return new ApiController(universitiesService);
        }
    }

    @MockitoBean
    private UniversitiesService universitiesService;

    @Nested
    @DisplayName("正常场景")
    class NormalTests {

        @Test
        @DisplayName("GET /school/all - 获取学校列表成功")
        void getSchoolListSuccess() throws Exception {
            List<SchoolResponse> list = List.of(createSchoolResponse("1", "北京大学"), createSchoolResponse("2", "清华大学"));
            when(universitiesService.getList()).thenReturn(list);

            mockMvc.perform(get("/api/school/all"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"))
                    .andExpect(jsonPath("$.data.length()").value(2))
                    .andExpect(jsonPath("$.data[0].name").value("北京大学"));
        }
    }

    @Nested
    @DisplayName("异常处理场景")
    class ErrorTests {

        @Test
        @DisplayName("Service 抛出 BusinessException 返回 400")
        void serviceThrowsBusinessException() throws Exception {
            when(universitiesService.getList()).thenThrow(new BusinessException(4000, "获取学校列表失败"));

            mockMvc.perform(get("/api/school/all"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("4000"))
                    .andExpect(jsonPath("$.message").value("获取学校列表失败"));
        }

        @Test
        @DisplayName("Service 抛出 RuntimeException 返回 500")
        void serviceThrowsRuntimeException() throws Exception {
            when(universitiesService.getList()).thenThrow(new RuntimeException("数据库异常"));

            mockMvc.perform(get("/api/school/all"))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.code").value("500"));
        }
    }

    @Nested
    @DisplayName("边界场景")
    class BoundaryTests {

        @Test
        @DisplayName("GET /school/all - 空列表返回空数组")
        void getSchoolListEmpty() throws Exception {
            when(universitiesService.getList()).thenReturn(List.of());

            mockMvc.perform(get("/api/school/all"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"))
                    .andExpect(jsonPath("$.data.length()").value(0));
        }

        @Test
        @DisplayName("GET /school/all - 单个学校")
        void getSchoolListSingle() throws Exception {
            List<SchoolResponse> list = List.of(createSchoolResponse("1", "浙江大学"));
            when(universitiesService.getList()).thenReturn(list);

            mockMvc.perform(get("/api/school/all"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"))
                    .andExpect(jsonPath("$.data.length()").value(1))
                    .andExpect(jsonPath("$.data[0].id").value("1"));
        }

        @Test
        @DisplayName("GET /school/all - 学校名含特殊字符")
        void getSchoolListWithSpecialChars() throws Exception {
            List<SchoolResponse> list = List.of(createSchoolResponse("3", "测试·大学（海淀）"));
            when(universitiesService.getList()).thenReturn(list);

            mockMvc.perform(get("/api/school/all"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"))
                    .andExpect(jsonPath("$.data[0].name").value("测试·大学（海淀）"));
        }
    }

    @Nested
    @DisplayName("安全场景")
    class SecurityTests {

        @Test
        @DisplayName("POST 请求学校列表接口返回 405")
        void getSchoolListWithWrongMethod() throws Exception {
            mockMvc.perform(post("/api/school/all")).andExpect(status().isMethodNotAllowed());
        }

        @Test
        @DisplayName("PUT 请求学校列表接口返回 405")
        void getSchoolListWithPutMethod() throws Exception {
            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put("/api/school/all"))
                    .andExpect(status().isMethodNotAllowed());
        }
    }

    private SchoolResponse createSchoolResponse(String id, String name) {
        return SchoolResponse.builder().id(id).name(name).build();
    }
}
