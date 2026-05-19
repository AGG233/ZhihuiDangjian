package com.rauio.smartdangjian.server.resource.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.dromara.x.file.storage.core.FileStorageService;
import org.dromara.x.file.storage.core.presigned.GeneratePresignedUrlPretreatment;
import org.dromara.x.file.storage.core.presigned.GeneratePresignedUrlResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.rauio.smartdangjian.exception.BusinessException;
import com.rauio.smartdangjian.server.resource.constants.ResourceErrorConstants;
import com.rauio.smartdangjian.server.resource.constants.ResourceStatusConstants;
import com.rauio.smartdangjian.server.resource.pojo.entity.ResourceMeta;
import com.rauio.smartdangjian.server.resource.pojo.request.UploadFileRequest;
import com.rauio.smartdangjian.server.resource.pojo.response.FileUploadResponse;
import com.rauio.smartdangjian.server.user.service.UserService;

@ExtendWith(MockitoExtension.class)
class FileServiceTest {

    @Mock
    private FileStorageService fileStorageService;

    @Mock
    private UserService userService;

    @Mock
    private ResourceMetaService resourceMetaService;

    @InjectMocks
    private FileService fileService;

    private GeneratePresignedUrlPretreatment pretreatment;

    private static final String RESOURCE_ID = "res-001";
    private static final String OBJECT_KEY = "image/test-uuid.png";
    private static final String FILE_NAME = "test.png";
    private static final String MIME_TYPE = "image/png";
    private static final String COS_URL = "https://cos.example.com/image/test-uuid.png?sign=abc";

    @BeforeEach
    void setUp() {
        pretreatment = mock(GeneratePresignedUrlPretreatment.class);
        lenient().when(pretreatment.setPlatform(anyString())).thenReturn(pretreatment);
        lenient().when(pretreatment.setPath(anyString())).thenReturn(pretreatment);
        lenient().when(pretreatment.setFilename(anyString())).thenReturn(pretreatment);
        lenient().when(pretreatment.setMethod(any())).thenReturn(pretreatment);
        lenient().when(pretreatment.setExpiration(any())).thenReturn(pretreatment);
        lenient().when(pretreatment.putHeaders(anyString(), anyString())).thenReturn(pretreatment);
        lenient().when(pretreatment.putUserMetadata(anyString(), anyString())).thenReturn(pretreatment);
    }

    private ResourceMeta createResourceMeta() {
        return ResourceMeta.builder()
                .id(RESOURCE_ID)
                .uploaderId("user-001")
                .originalName(FILE_NAME)
                .hash("uuid-hash")
                .objectKey(OBJECT_KEY)
                .resourceType(0)
                .status(ResourceStatusConstants.UPLOADING)
                .build();
    }

    private UploadFileRequest createUploadRequest() {
        UploadFileRequest request = new UploadFileRequest();
        request.setUserId("user-001");
        request.setFileName(FILE_NAME);
        request.setMimeType(MIME_TYPE);
        return request;
    }

    @Nested
    @DisplayName("upload — 创建预签名上传 URL")
    class UploadTests {

        @Test
        @DisplayName("正常上传：COS 生成预签名 URL 成功，返回 FileUploadResponse")
        void uploadSuccess() {
            ResourceMeta meta = createResourceMeta();
            when(resourceMetaService.create(any())).thenReturn(meta);
            when(fileStorageService.generatePresignedUrl()).thenReturn(pretreatment);

            GeneratePresignedUrlResult urlResult = new GeneratePresignedUrlResult();
            urlResult.setUrl(COS_URL);
            when(pretreatment.generatePresignedUrl()).thenReturn(urlResult);

            FileUploadResponse response = fileService.upload(createUploadRequest());

            assertThat(response).isNotNull();
            assertThat(response.getResourceId()).isEqualTo(RESOURCE_ID);
            assertThat(response.getUploadUrl()).isEqualTo(COS_URL);
            assertThat(response.getObjectKey()).startsWith("image/").endsWith(".png");
            assertThat(response.getExpiration()).isPositive();
        }

        @Test
        @DisplayName("COS 异常：generatePresignedUrl 抛出异常，抛出 BusinessException(RESOURCE_CREATE_FAILED)")
        void uploadCosException() {
            ResourceMeta meta = createResourceMeta();
            when(resourceMetaService.create(any())).thenReturn(meta);
            when(fileStorageService.generatePresignedUrl()).thenReturn(pretreatment);

            RuntimeException cosException = new RuntimeException("COS SecretId/Key 配置错误");
            when(pretreatment.generatePresignedUrl()).thenThrow(cosException);

            assertThatThrownBy(() -> fileService.upload(createUploadRequest()))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("code", ResourceErrorConstants.RESOURCE_CREATE_FAILED)
                    .hasMessageContaining("文件存储服务暂不可用");

            verify(resourceMetaService).delete(RESOURCE_ID);
        }
    }

    @Nested
    @DisplayName("getDownloadUrl — 获取预签名下载 URL")
    class GetDownloadUrlTests {

        @Test
        @DisplayName("正常下载：COS 生成预签名下载 URL 成功，返回 URL 字符串")
        void getDownloadUrlSuccess() {
            ResourceMeta meta = createResourceMeta();
            when(resourceMetaService.get(RESOURCE_ID)).thenReturn(meta);
            when(fileStorageService.generatePresignedUrl()).thenReturn(pretreatment);

            GeneratePresignedUrlResult urlResult = new GeneratePresignedUrlResult();
            urlResult.setUrl(COS_URL);
            when(pretreatment.generatePresignedUrl()).thenReturn(urlResult);

            String url = fileService.getDownloadUrl(RESOURCE_ID);

            assertThat(url).isEqualTo(COS_URL);
        }

        @Test
        @DisplayName("COS 异常：generatePresignedUrl 抛出异常，抛出 BusinessException(RESOURCE_NOT_FOUND)")
        void getDownloadUrlCosException() {
            ResourceMeta meta = createResourceMeta();
            when(resourceMetaService.get(RESOURCE_ID)).thenReturn(meta);
            when(fileStorageService.generatePresignedUrl()).thenReturn(pretreatment);

            RuntimeException cosException = new RuntimeException("COS 服务连接超时");
            when(pretreatment.generatePresignedUrl()).thenThrow(cosException);

            assertThatThrownBy(() -> fileService.getDownloadUrl(RESOURCE_ID))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("code", ResourceErrorConstants.RESOURCE_NOT_FOUND)
                    .hasMessageContaining("文件服务暂不可用");
        }
    }
}
