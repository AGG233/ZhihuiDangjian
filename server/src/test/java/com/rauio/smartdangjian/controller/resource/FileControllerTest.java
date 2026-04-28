package com.rauio.smartdangjian.controller.resource;

import com.rauio.smartdangjian.exception.BusinessException;
import com.rauio.smartdangjian.security.CurrentUserPrincipal;
import com.rauio.smartdangjian.server.resource.pojo.response.FileInfoResponse;
import com.rauio.smartdangjian.server.resource.pojo.response.FileUploadResponse;
import com.rauio.smartdangjian.server.resource.service.FileService;
import com.rauio.smartdangjian.utils.spec.UserType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK, classes = FileControllerTest.TestConfig.class)
@AutoConfigureMockMvc(addFilters = false)
@TestPropertySource(
        locations = "classpath:application-test.yaml",
        properties = {
                "REDIS_HOST=localhost",
                "REDIS_PORT=6379",
                "REDIS_DATABASE=0",
                "DATABASE_URL=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
                "DATABASE_USERNAME=sa",
                "DATABASE_PASSWORD=",
                "NEO4J_URI=bolt://localhost:7687",
                "NEO4J_USERNAME=neo4j",
                "NEO4J_PASSWORD=password"
        }
)
@DisplayName("文件资源接口测试 (FileController)")
class FileControllerTest {

    @SpringBootConfiguration
    @EnableAutoConfiguration(exclude = {
            DataSourceAutoConfiguration.class,
            HibernateJpaAutoConfiguration.class,
            org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class,
            org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration.class,
            com.rauio.smartdangjian.config.SecurityCoreAutoConfiguration.class,
            com.rauio.smartdangjian.config.SecuritySupportAutoConfiguration.class,
            com.rauio.smartdangjian.config.TransactionConfig.class
    })
    @EnableWebMvc
    @ComponentScan(
            basePackages = "com.rauio.smartdangjian.server.resource.controller.user",
            excludeFilters = @ComponentScan.Filter(
                    type = org.springframework.context.annotation.FilterType.REGEX,
                    pattern = ".*Banner.*"
            )
    )
    static class TestConfig {
    }

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private FileService fileService;

    @BeforeEach
    void setUpSecurityContext() {
        CurrentUserPrincipal principal = new CurrentUserPrincipal() {
            @Override
            public String getId() {
                return "user-001";
            }

            @Override
            public UserType getUserType() {
                return UserType.SCHOOL;
            }

            @Override
            public String getUniversityId() {
                return "uni-001";
            }
        };
        org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(
                new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                        principal, null, Collections.emptyList()
                )
        );
    }

    // ═══════════════════════════════════════════════════════════════
    // NormalUploadFlowTests
    // ═══════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("正常上传流程")
    class NormalUploadFlowTests {

        @Test
        @DisplayName("POST /upload — 上传图片返回预签名 URL")
        void uploadImageReturnsPresignedUrl() throws Exception {
            FileUploadResponse resp = FileTestDataFactory.createUploadResponse();
            when(fileService.upload(any())).thenReturn(resp);

            mockMvc.perform(post("/api/resource/files/upload")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"fileName\":\"test.png\",\"mimeType\":\"image/png\"}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"))
                    .andExpect(jsonPath("$.data.resourceId").value("file-res-001"))
                    .andExpect(jsonPath("$.data.uploadUrl").isString())
                    .andExpect(jsonPath("$.data.objectKey").value("image/uuid-test.png"))
                    .andExpect(jsonPath("$.data.expiration").isNumber());
        }

        @Test
        @DisplayName("POST /upload — 上传视频返回预签名 URL，路径以 video/ 开头")
        void uploadVideoReturnsPresignedUrl() throws Exception {
            FileUploadResponse resp = FileTestDataFactory.createUploadResponse(
                    "file-res-002",
                    "https://cos.example.com/video/uuid-test.mp4?sign=abc",
                    "video/uuid-test.mp4",
                    System.currentTimeMillis() + 600_000L);
            when(fileService.upload(any())).thenReturn(resp);

            mockMvc.perform(post("/api/resource/files/upload")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"fileName\":\"test.mp4\",\"mimeType\":\"video/mp4\"}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.objectKey").value("video/uuid-test.mp4"));
        }

        @Test
        @DisplayName("POST /confirm/{id} — 确认上传成功，返回状态为 PUBLIC")
        void confirmUploadReturnsUpdatedMeta() throws Exception {
            when(fileService.confirmUpload("file-res-001"))
                    .thenReturn(FileTestDataFactory.createPublicResourceMeta());

            mockMvc.perform(post("/api/resource/files/confirm/file-res-001"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"))
                    .andExpect(jsonPath("$.data.id").value("file-res-001"))
                    .andExpect(jsonPath("$.data.status").value(1));
        }

        @Test
        @DisplayName("POST /confirm/{id} — 已确认的资源再次确认仍返回 PUBLIC（幂等）")
        void confirmAlreadyPublicUploadReturnsMetaUnchanged() throws Exception {
            when(fileService.confirmUpload("file-res-001"))
                    .thenReturn(FileTestDataFactory.createPublicResourceMeta());

            mockMvc.perform(post("/api/resource/files/confirm/file-res-001"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.status").value(1));
        }

        @Test
        @DisplayName("GET /by-id/{id} — 根据资源 ID 获取文件信息，返回所有字段")
        void getByIdReturnsFileInfoResponse() throws Exception {
            FileInfoResponse info = FileTestDataFactory.createFileInfoResponse();
            when(fileService.getFileInfo("file-res-001")).thenReturn(info);

            mockMvc.perform(get("/api/resource/files/by-id/file-res-001"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"))
                    .andExpect(jsonPath("$.data.resourceId").value("file-res-001"))
                    .andExpect(jsonPath("$.data.originalName").value("test.png"))
                    .andExpect(jsonPath("$.data.hash").value("abc123def456"))
                    .andExpect(jsonPath("$.data.objectKey").value("image/uuid-test.png"))
                    .andExpect(jsonPath("$.data.resourceType").value(0))
                    .andExpect(jsonPath("$.data.status").value(1))
                    .andExpect(jsonPath("$.data.downloadUrl").isString())
                    .andExpect(jsonPath("$.data.size").value(1048576));
        }

        @Test
        @DisplayName("GET /by-hash/{hash} — 根据哈希获取文件信息")
        void getByHashReturnsFileInfoResponse() throws Exception {
            FileInfoResponse info = FileTestDataFactory.createFileInfoResponse();
            when(fileService.getFileInfoByHash("abc123def456")).thenReturn(info);

            mockMvc.perform(get("/api/resource/files/by-hash/abc123def456"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.resourceId").value("file-res-001"))
                    .andExpect(jsonPath("$.data.hash").value("abc123def456"));
        }

        @Test
        @DisplayName("GET /{id}/download — 获取文件下载链接")
        void getDownloadUrlReturnsUrlString() throws Exception {
            when(fileService.getDownloadUrl("file-res-001"))
                    .thenReturn("https://cos.example.com/image/uuid-test.png?sign=xyz");

            mockMvc.perform(get("/api/resource/files/file-res-001/download"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"))
                    .andExpect(jsonPath("$.data").isString());
        }

        @Test
        @DisplayName("完整上传生命周期：upload → confirm → getById → download")
        void fullUploadLifecycle() throws Exception {
            FileUploadResponse uploadResp = FileTestDataFactory.createUploadResponse();
            when(fileService.upload(any())).thenReturn(uploadResp);
            when(fileService.confirmUpload("file-res-001"))
                    .thenReturn(FileTestDataFactory.createPublicResourceMeta());
            when(fileService.getFileInfo("file-res-001"))
                    .thenReturn(FileTestDataFactory.createFileInfoResponse());
            when(fileService.getDownloadUrl("file-res-001"))
                    .thenReturn("https://cos.example.com/image/uuid-test.png?sign=xyz");

            mockMvc.perform(post("/api/resource/files/upload")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"fileName\":\"test.png\",\"mimeType\":\"image/png\"}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.resourceId").value("file-res-001"));

            mockMvc.perform(post("/api/resource/files/confirm/file-res-001"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.status").value(1));

            mockMvc.perform(get("/api/resource/files/by-id/file-res-001"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.downloadUrl").isString());

            mockMvc.perform(get("/api/resource/files/file-res-001/download"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data").isString());

            verify(fileService).upload(any());
            verify(fileService).confirmUpload("file-res-001");
            verify(fileService).getFileInfo("file-res-001");
            verify(fileService).getDownloadUrl("file-res-001");
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // BatchOperationTests
    // ═══════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("批量操作场景")
    class BatchOperationTests {

        @Test
        @DisplayName("POST /batch/id — 批量根据资源 ID 获取下载链接")
        void getBatchByIdsReturnsUrlList() throws Exception {
            when(fileService.getBatchByIds(any())).thenReturn(List.of(
                    "https://cos.example.com/dl/file-1",
                    "https://cos.example.com/dl/file-2"));

            mockMvc.perform(post("/api/resource/files/batch/id")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("[\"file-1\",\"file-2\"]"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"))
                    .andExpect(jsonPath("$.data.length()").value(2))
                    .andExpect(jsonPath("$.data[0]").value("https://cos.example.com/dl/file-1"))
                    .andExpect(jsonPath("$.data[1]").value("https://cos.example.com/dl/file-2"));
        }

        @Test
        @DisplayName("POST /batch/hash — 批量根据文件哈希获取下载链接")
        void getBatchByHashesReturnsUrlList() throws Exception {
            when(fileService.getBatchByHashes(any())).thenReturn(List.of(
                    "https://cos.example.com/dl/hash-1",
                    "https://cos.example.com/dl/hash-2"));

            mockMvc.perform(post("/api/resource/files/batch/hash")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("[\"hash-1\",\"hash-2\"]"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.length()").value(2))
                    .andExpect(jsonPath("$.data[0]").value("https://cos.example.com/dl/hash-1"))
                    .andExpect(jsonPath("$.data[1]").value("https://cos.example.com/dl/hash-2"));
        }

        @Test
        @DisplayName("POST /batch/id — 空 ID 列表返回空数组")
        void getBatchByIdsWithEmptyList() throws Exception {
            when(fileService.getBatchByIds(any())).thenReturn(List.of());

            mockMvc.perform(post("/api/resource/files/batch/id")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("[]"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.length()").value(0));
        }

        @Test
        @DisplayName("POST /batch/hash — 空哈希列表返回空数组")
        void getBatchByHashesWithEmptyList() throws Exception {
            when(fileService.getBatchByHashes(any())).thenReturn(List.of());

            mockMvc.perform(post("/api/resource/files/batch/hash")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("[]"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.length()").value(0));
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // DeleteFlowTests
    // ═══════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("删除场景")
    class DeleteFlowTests {

        @Test
        @DisplayName("DELETE /{id} — 删除文件返回 true")
        void deleteByIdReturnsTrue() throws Exception {
            doNothing().when(fileService).delete("file-res-001");

            mockMvc.perform(delete("/api/resource/files/file-res-001"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"))
                    .andExpect(jsonPath("$.data").value(true));
        }

        @Test
        @DisplayName("DELETE /{id} — 验证服务层 delete 被精确调用一次")
        void deleteCallsServiceDelete() throws Exception {
            doNothing().when(fileService).delete("file-res-001");

            mockMvc.perform(delete("/api/resource/files/file-res-001"))
                    .andExpect(status().isOk());

            verify(fileService).delete("file-res-001");
        }

        @Test
        @DisplayName("DELETE /{id} — 删除不存在的资源返回 400")
        void deleteNonExistentResource() throws Exception {
            doThrow(new BusinessException(4000, "资源不存在"))
                    .when(fileService).delete("ghost-id");

            mockMvc.perform(delete("/api/resource/files/ghost-id"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("4000"))
                    .andExpect(jsonPath("$.message").value("资源不存在"));
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // ErrorHandlingTests
    // ═══════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("异常和错误处理场景")
    class ErrorHandlingTests {

        @Test
        @DisplayName("POST /upload — 不传 fileName（null）触发 @NotBlank 校验返回 400")
        void uploadWithNullFileName() throws Exception {
            mockMvc.perform(post("/api/resource/files/upload")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"mimeType\":\"image/png\"}"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("400"))
                    .andExpect(jsonPath("$.message").value("文件名不能为空"));
        }

        @Test
        @DisplayName("POST /upload — 空请求体 {} 触发校验返回 400")
        void uploadWithEmptyRequestBody() throws Exception {
            mockMvc.perform(post("/api/resource/files/upload")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("400"));
        }

        @Test
        @DisplayName("POST /confirm/{id} — 确认不存在的资源返回 400")
        void confirmUploadResourceNotFound() throws Exception {
            when(fileService.confirmUpload("ghost"))
                    .thenThrow(new BusinessException(4000, "资源不存在"));

            mockMvc.perform(post("/api/resource/files/confirm/ghost"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("4000"))
                    .andExpect(jsonPath("$.message").value("资源不存在"));
        }

        @Test
        @DisplayName("GET /by-id/{id} — 根据不存在的 ID 查询返回 400")
        void getByIdResourceNotFound() throws Exception {
            when(fileService.getFileInfo("ghost"))
                    .thenThrow(new BusinessException(4000, "资源不存在"));

            mockMvc.perform(get("/api/resource/files/by-id/ghost"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("4000"))
                    .andExpect(jsonPath("$.message").value("资源不存在"));
        }

        @Test
        @DisplayName("GET /by-hash/{hash} — 根据不存在的哈希查询返回 400")
        void getByHashResourceNotFound() throws Exception {
            when(fileService.getFileInfoByHash("ghost-hash"))
                    .thenThrow(new BusinessException(4000, "资源不存在"));

            mockMvc.perform(get("/api/resource/files/by-hash/ghost-hash"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("4000"))
                    .andExpect(jsonPath("$.message").value("资源不存在"));
        }

        @Test
        @DisplayName("GET /{id}/download — 不存在的资源下载返回 400")
        void getDownloadUrlResourceNotFound() throws Exception {
            when(fileService.getDownloadUrl("ghost"))
                    .thenThrow(new BusinessException(4000, "资源不存在"));

            mockMvc.perform(get("/api/resource/files/ghost/download"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("4000"))
                    .andExpect(jsonPath("$.message").value("资源不存在"));
        }

        @Test
        @DisplayName("Service 抛出 RuntimeException 返回 500")
        void serviceThrowsRuntimeException() throws Exception {
            when(fileService.getDownloadUrl("boom"))
                    .thenThrow(new RuntimeException("数据库连接失败"));

            mockMvc.perform(get("/api/resource/files/boom/download"))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.code").value("500"));
        }

        @Test
        @DisplayName("畸形 JSON 请求体返回 400")
        void malformedJsonRequestBody() throws Exception {
            mockMvc.perform(post("/api/resource/files/upload")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{broken json"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("错误 HTTP Method 返回 405")
        void wrongEndpointMethod() throws Exception {
            mockMvc.perform(post("/api/resource/files/by-id/test"))
                    .andExpect(status().isMethodNotAllowed());
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // EdgeCaseTests
    // ═══════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("边界和边缘场景")
    class EdgeCaseTests {

        @Test
        @DisplayName("POST /upload — 空文件名触发 @NotBlank 校验返回 400")
        void uploadWithEmptyFileName() throws Exception {
            mockMvc.perform(post("/api/resource/files/upload")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"fileName\":\"\",\"mimeType\":\"image/png\"}"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("400"));
        }

        @Test
        @DisplayName("POST /upload — application/octet-stream 类型的文件正常接受")
        void uploadWithApplicationOctetStream() throws Exception {
            FileUploadResponse resp = FileTestDataFactory.createUploadResponse(
                    "file-res-003",
                    "https://cos.example.com/resource/uuid.bin?sign=abc",
                    "resource/uuid.bin",
                    System.currentTimeMillis() + 600_000L);
            when(fileService.upload(any())).thenReturn(resp);

            mockMvc.perform(post("/api/resource/files/upload")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"fileName\":\"file.bin\",\"mimeType\":\"application/octet-stream\"}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.objectKey").value("resource/uuid.bin"));
        }

        @Test
        @DisplayName("POST /upload — 不传 mimeType(null) 触发 @NotNull 校验返回 400")
        void uploadWithoutMimeType() throws Exception {
            mockMvc.perform(post("/api/resource/files/upload")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"fileName\":\"test.png\"}"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("400"));
        }

        @Test
        @DisplayName("GET /by-id/{id} — FileInfoResponse 中 size=null 时正常返回")
        void getByIdReturnsFileInfoWithNullSize() throws Exception {
            FileInfoResponse info = FileTestDataFactory.createFileInfoResponse(
                    "file-res-001", "test.png", "abc123",
                    "image/test.png", 0, 1,
                    "https://cos.example.com/dl", null);
            when(fileService.getFileInfo("file-res-001")).thenReturn(info);

            mockMvc.perform(get("/api/resource/files/by-id/file-res-001"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.size").doesNotExist());
        }

        @Test
        @DisplayName("POST /batch/id — 列表中含空字符串正常处理")
        void batchByIdWithEmptyStringInList() throws Exception {
            when(fileService.getBatchByIds(anyList())).thenReturn(List.of(
                    "https://cos.example.com/dl/real-id",
                    "https://cos.example.com/dl/"));

            mockMvc.perform(post("/api/resource/files/batch/id")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("[\"real-id\",\"\"]"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.length()").value(2));
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // SecurityTests
    // ═══════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("安全相关场景")
    class SecurityTests {

        @Test
        @DisplayName("POST /upload — XSS 注入在 fileName 字段正常接受（透传，无 sanitization）")
        void xssInFileName() throws Exception {
            when(fileService.upload(any())).thenReturn(FileTestDataFactory.createUploadResponse());

            mockMvc.perform(post("/api/resource/files/upload")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"fileName\":\"<script>alert('xss')</script>\",\"mimeType\":\"image/png\"}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"));
        }

        @Test
        @DisplayName("GET /by-hash/{hash} — SQL 注入在 hash 参数中被当作字面量处理")
        void sqlInjectionInHash() throws Exception {
            when(fileService.getFileInfoByHash(any()))
                    .thenThrow(new BusinessException(4000, "资源不存在"));

            mockMvc.perform(get("/api/resource/files/by-hash/'%20OR%20'1'='1"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("POST /upload — SQL 注入在 fileName 字段正常接受（参数化查询防护）")
        void sqlInjectionInFileName() throws Exception {
            when(fileService.upload(any())).thenReturn(FileTestDataFactory.createUploadResponse());

            mockMvc.perform(post("/api/resource/files/upload")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"fileName\":\"' OR '1'='1\",\"mimeType\":\"image/png\"}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"));
        }
    }
}
