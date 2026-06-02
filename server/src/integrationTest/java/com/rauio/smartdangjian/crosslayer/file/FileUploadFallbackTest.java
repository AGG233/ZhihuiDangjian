package com.rauio.smartdangjian.crosslayer.file;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.dromara.x.file.storage.core.FileStorageService;
import org.dromara.x.file.storage.core.presigned.GeneratePresignedUrlPretreatment;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.rauio.smartdangjian.crosslayer.CrossLayerTestBase;
import com.rauio.smartdangjian.server.resource.controller.user.FileController;
import com.rauio.smartdangjian.server.resource.pojo.entity.ResourceMeta;
import com.rauio.smartdangjian.server.resource.service.FileService;
import com.rauio.smartdangjian.server.resource.service.ResourceMetaService;
import com.rauio.smartdangjian.server.user.service.UserService;
import com.rauio.smartdangjian.service.PermissionValidator;
import com.rauio.smartdangjian.utils.spec.UserType;

@SpringBootTest(classes = FileUploadFallbackTest.TestConfig.class)
class FileUploadFallbackTest extends CrossLayerTestBase {

    @MockitoBean
    private FileStorageService fileStorageService;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private ResourceMetaService resourceMetaService;

    @SpringBootConfiguration
    static class TestConfig extends CrossLayerTestConfig {

        @Bean
        FileController fileController(
                FileService fileService, com.rauio.smartdangjian.security.CurrentUserProvider currentUserProvider) {
            return new FileController(fileService, currentUserProvider);
        }

        @Bean
        PermissionValidator permissionValidator() {
            return mock(PermissionValidator.class);
        }

        @Bean
        FileService fileService(
                FileStorageService fss, UserService us, ResourceMetaService rms, PermissionValidator pv) {
            return new FileService(fss, us, rms, pv);
        }
    }

    @Test
    @DisplayName("COS不可用时回退到本地中转上传")
    void cosFailureFallsBackToLocalUpload() throws Exception {
        // given - setup security context as STUDENT
        setSecurityContext(UserType.STUDENT, 1L, "uni-001");

        // given - mock GeneratePresignedUrlPretreatment chain
        GeneratePresignedUrlPretreatment pretreatment = mock(GeneratePresignedUrlPretreatment.class);
        doReturn(pretreatment).when(pretreatment).setPlatform(any());
        doReturn(pretreatment).when(pretreatment).setPath(any());
        doReturn(pretreatment).when(pretreatment).setFilename(any());
        doReturn(pretreatment).when(pretreatment).setMethod(any());
        doReturn(pretreatment).when(pretreatment).setExpiration(any());
        doReturn(pretreatment).when(pretreatment).putHeaders(any(), any());
        doReturn(pretreatment).when(pretreatment).putUserMetadata(any(), any());
        when(pretreatment.generatePresignedUrl()).thenThrow(new RuntimeException("COS unavailable"));

        // given - fileStorageService returns the mock pretreatment
        when(fileStorageService.generatePresignedUrl()).thenReturn(pretreatment);

        // given - resourceMetaService.create returns a pre-built ResourceMeta
        ResourceMeta meta = ResourceMeta.builder()
                .id(1L)
                .uploaderId(1L)
                .originalName("test.png")
                .hash("uuid-hash")
                .objectKey("image/uuid-test.png")
                .resourceType(0)
                .status(0)
                .build();
        when(resourceMetaService.create(any())).thenReturn(meta);

        // when
        var result = mockMvc.perform(post("/api/resource/files/upload")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"fileName\":\"生成家具照片 (9).png\",\"mimeType\":\"image/png\"}"));

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.data.resourceId").value("1"))
                .andExpect(jsonPath("$.data.uploadUrl").value("/api/resource/files/upload/callback/1"))
                .andExpect(jsonPath("$.data.expiration").value(-1));
    }
}
