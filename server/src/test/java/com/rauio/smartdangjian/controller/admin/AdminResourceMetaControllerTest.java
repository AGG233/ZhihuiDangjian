package com.rauio.smartdangjian.controller.admin;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.rauio.smartdangjian.BaseControllerTest;
import com.rauio.smartdangjian.ControllerTestConfiguration;
import com.rauio.smartdangjian.controller.factory.BannerTestDataFactory;
import com.rauio.smartdangjian.controller.factory.ResourceMetaTestDataFactory;
import com.rauio.smartdangjian.exception.BusinessException;
import com.rauio.smartdangjian.server.resource.constants.ResourceErrorConstants;
import com.rauio.smartdangjian.server.resource.pojo.entity.ResourceMeta;
import com.rauio.smartdangjian.server.resource.service.ResourceMetaService;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK, classes = ControllerTestConfiguration.class)
@DisplayName("管理员资源元数据接口测试")
class AdminResourceMetaControllerTest extends BaseControllerTest {

    @MockitoBean
    private ResourceMetaService resourceMetaService;

    @Nested
    @DisplayName("正常场景")
    class NormalTests {

        @Test
        @DisplayName("POST / - 创建资源元数据成功")
        void createResourceMetaSuccess() throws Exception {
            when(resourceMetaService.create(any())).thenReturn(BannerTestDataFactory.createResourceMeta(1L));

            mockMvc.perform(post("/api/admin/resource/files")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(ResourceMetaTestDataFactory.toJson(
                                    ResourceMetaTestDataFactory.createResourceMetaCreateRequest())))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"))
                    .andExpect(jsonPath("$.data.id").value("1"));
        }

        @Test
        @DisplayName("GET /{id} - 获取资源元数据成功")
        void getResourceMetaSuccess() throws Exception {
            when(resourceMetaService.get(1L)).thenReturn(BannerTestDataFactory.createResourceMeta(1L));

            mockMvc.perform(get("/api/admin/resource/files/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"))
                    .andExpect(jsonPath("$.data.id").value("1"));
        }

        @Test
        @DisplayName("GET / - 查询资源元数据列表成功")
        void listResourceMetaSuccess() throws Exception {
            List<ResourceMeta> list =
                    List.of(BannerTestDataFactory.createResourceMeta(1L), BannerTestDataFactory.createResourceMeta(2L));
            when(resourceMetaService.list(any(), any(), any(), any(), any())).thenReturn(list);

            mockMvc.perform(get("/api/admin/resource/files"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"))
                    .andExpect(jsonPath("$.data.length()").value(2));
        }

        @Test
        @DisplayName("PUT /{id} - 更新资源元数据成功")
        void updateResourceMetaSuccess() throws Exception {
            when(resourceMetaService.update(anyLong(), any())).thenReturn(true);

            mockMvc.perform(put("/api/admin/resource/files/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(ResourceMetaTestDataFactory.toJson(
                                    ResourceMetaTestDataFactory.createResourceMetaUpdateRequest())))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"))
                    .andExpect(jsonPath("$.data").value(true));
        }

        @Test
        @DisplayName("DELETE /{id} - 删除资源元数据成功")
        void deleteByIdSuccess() throws Exception {
            when(resourceMetaService.delete(1L)).thenReturn(true);

            mockMvc.perform(delete("/api/admin/resource/files/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"))
                    .andExpect(jsonPath("$.data").value(true));
        }

        @Test
        @DisplayName("DELETE /by-hash/{hash} - 按哈希删除资源成功")
        void deleteByHashSuccess() throws Exception {
            when(resourceMetaService.deleteByHash("hash-123")).thenReturn(true);

            mockMvc.perform(delete("/api/admin/resource/files/by-hash/hash-123"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"))
                    .andExpect(jsonPath("$.data").value(true));
        }

        @Test
        @DisplayName("DELETE / - 批量删除资源成功")
        void batchDeleteSuccess() throws Exception {
            when(resourceMetaService.deleteByHashes(any())).thenReturn(true);

            mockMvc.perform(delete("/api/admin/resource/files").param("hash", "hash-1", "hash-2"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"))
                    .andExpect(jsonPath("$.data").value(true));
        }
    }

    @Nested
    @DisplayName("异常处理场景")
    class ErrorTests {

        @Test
        @DisplayName("Service 抛出 BusinessException 返回 400")
        void serviceThrowsBusinessException() throws Exception {
            when(resourceMetaService.get(9999L))
                    .thenThrow(new BusinessException(ResourceErrorConstants.RESOURCE_NOT_FOUND, "资源不存在"));

            mockMvc.perform(get("/api/admin/resource/files/9999"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("5002"))
                    .andExpect(jsonPath("$.message").value("资源不存在"));
        }

        @Test
        @DisplayName("Service 抛出 RuntimeException 返回 500")
        void serviceThrowsRuntimeException() throws Exception {
            when(resourceMetaService.list(any(), any(), any(), any(), any())).thenThrow(new RuntimeException("数据库异常"));

            mockMvc.perform(get("/api/admin/resource/files"))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.code").value("500"));
        }

        @Test
        @DisplayName("POST / - 请求体为空返回 400")
        void createWithEmptyBody() throws Exception {
            mockMvc.perform(post("/api/admin/resource/files")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(""))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("POST / - 缺少必填字段返回 400")
        void createWithMissingRequiredFields() throws Exception {
            String json = "{\"uploaderId\": \"admin1\"}";
            mockMvc.perform(post("/api/admin/resource/files")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("非法 JSON 请求体返回 400")
        void malformedJson() throws Exception {
            mockMvc.perform(post("/api/admin/resource/files")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{invalid json"))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("边界场景")
    class BoundaryTests {

        @Test
        @DisplayName("GET / - 空结果集返回空数组")
        void listEmptyResult() throws Exception {
            when(resourceMetaService.list(any(), any(), any(), any(), any())).thenReturn(List.of());

            mockMvc.perform(get("/api/admin/resource/files"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"))
                    .andExpect(jsonPath("$.data.length()").value(0));
        }

        @Test
        @DisplayName("GET / - 带查询参数筛选")
        void listWithQueryParams() throws Exception {
            List<ResourceMeta> list = List.of(BannerTestDataFactory.createResourceMeta(1L));
            when(resourceMetaService.list(eq(1L), any(), any(), any(), any())).thenReturn(list);

            mockMvc.perform(get("/api/admin/resource/files")
                            .param("uploaderId", "1")
                            .param("resourceType", "0"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"))
                    .andExpect(jsonPath("$.data.length()").value(1));
        }

        @Test
        @DisplayName("PUT /{id} - 空更新请求（所有字段可选）")
        void updateWithEmptyFields() throws Exception {
            when(resourceMetaService.update(anyLong(), any())).thenReturn(true);

            mockMvc.perform(put("/api/admin/resource/files/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(ResourceMetaTestDataFactory.toJson(
                                    ResourceMetaTestDataFactory.createEmptyUpdateRequest())))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"))
                    .andExpect(jsonPath("$.data").value(true));
        }
    }

    @Nested
    @DisplayName("安全场景")
    class SecurityTests {

        @Test
        @DisplayName("XSS 注入在 originalName 字段")
        void xssInOriginalName() throws Exception {
            when(resourceMetaService.create(any())).thenReturn(BannerTestDataFactory.createResourceMeta(1L));

            String json =
                    "{\"uploaderId\":\"admin1\",\"originalName\":\"<script>alert('xss')</script>\",\"hash\":\"h1\",\"objectKey\":\"ok\",\"resourceType\":0}";
            mockMvc.perform(post("/api/admin/resource/files")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"));
        }

        @Test
        @DisplayName("SQL 注入在 hash 字段")
        void sqlInjectionInHash() throws Exception {
            when(resourceMetaService.deleteByHash(anyString())).thenReturn(true);

            mockMvc.perform(delete("/api/admin/resource/files/by-hash/' OR '1'='1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"));
        }

        @Test
        @DisplayName("POST 请求查询接口返回 400")
        void listWithWrongMethod() throws Exception {
            mockMvc.perform(post("/api/admin/resource/files")).andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("PUT 请求创建接口返回 405")
        void createWithWrongMethod() throws Exception {
            mockMvc.perform(put("/api/admin/resource/files")).andExpect(status().isMethodNotAllowed());
        }
    }
}
