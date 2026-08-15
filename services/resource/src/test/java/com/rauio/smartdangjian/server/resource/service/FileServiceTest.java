package com.rauio.smartdangjian.server.resource.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.InputStream;
import java.util.List;

import org.dromara.x.file.storage.core.FileInfo;
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
import com.rauio.smartdangjian.server.resource.pojo.response.FileInfoResponse;
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

    private static final Long RESOURCE_ID = 1L;
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
                .uploaderId(1L)
                .originalName(FILE_NAME)
                .hash("uuid-hash")
                .objectKey(OBJECT_KEY)
                .resourceType(0)
                .status(ResourceStatusConstants.UPLOADING)
                .build();
    }

    private ResourceMeta createPublicResourceMeta() {
        return ResourceMeta.builder()
                .id(RESOURCE_ID)
                .uploaderId(1L)
                .originalName(FILE_NAME)
                .hash("uuid-hash")
                .objectKey(OBJECT_KEY)
                .resourceType(0)
                .status(ResourceStatusConstants.PUBLIC)
                .build();
    }

    private UploadFileRequest createUploadRequest() {
        UploadFileRequest request = new UploadFileRequest();
        request.setUserId("1");
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
            assertThat(response.getResourceId()).isEqualTo("1");
            assertThat(response.getUploadUrl()).isEqualTo(COS_URL);
            assertThat(response.getObjectKey()).startsWith("image/").endsWith(".png");
            assertThat(response.getExpiration()).isPositive();
        }

        @Test
        @DisplayName("COS 异常：generatePresignedUrl 抛出异常，回退到本地中转 URL")
        void uploadCosException() {
            ResourceMeta meta = createResourceMeta();
            when(resourceMetaService.create(any())).thenReturn(meta);
            when(fileStorageService.generatePresignedUrl()).thenReturn(pretreatment);

            RuntimeException cosException = new RuntimeException("COS SecretId/Key 配置错误");
            when(pretreatment.generatePresignedUrl()).thenThrow(cosException);

            FileUploadResponse response = fileService.upload(createUploadRequest());

            assertThat(response).isNotNull();
            assertThat(response.getResourceId()).isEqualTo("1");
            assertThat(response.getUploadUrl()).startsWith("/api/resource/files/upload/callback/");
            assertThat(response.getUploadUrl()).endsWith("1");
            assertThat(response.getExpiration()).isEqualTo(-1L);

            verify(resourceMetaService, never()).delete(any());
        }

        @Test
        @DisplayName("上传时用户ID为空则使用当前登录用户ID")
        void uploadWithNullUserId() {
            UploadFileRequest request = new UploadFileRequest();
            request.setFileName(FILE_NAME);
            request.setMimeType(MIME_TYPE);
            // userId is null

            ResourceMeta meta = createResourceMeta();
            when(resourceMetaService.create(any())).thenReturn(meta);
            when(userService.getCurrentUserId()).thenReturn("2");
            when(fileStorageService.generatePresignedUrl()).thenReturn(pretreatment);

            GeneratePresignedUrlResult urlResult = new GeneratePresignedUrlResult();
            urlResult.setUrl(COS_URL);
            when(pretreatment.generatePresignedUrl()).thenReturn(urlResult);

            FileUploadResponse response = fileService.upload(request);

            assertThat(response).isNotNull();
            verify(userService).getCurrentUserId();
        }

        @Test
        @DisplayName("上传 video MIME 类型时存储到 video 目录")
        void uploadVideoMimeType() {
            UploadFileRequest request = new UploadFileRequest();
            request.setUserId("1");
            request.setFileName("test.mp4");
            request.setMimeType("video/mp4");

            ResourceMeta meta = ResourceMeta.builder()
                    .id(2L)
                    .uploaderId(1L)
                    .hash("video-hash")
                    .objectKey("video/")
                    .build();
            when(resourceMetaService.create(any())).thenReturn(meta);
            when(fileStorageService.generatePresignedUrl()).thenReturn(pretreatment);

            GeneratePresignedUrlResult urlResult = new GeneratePresignedUrlResult();
            urlResult.setUrl(COS_URL);
            when(pretreatment.generatePresignedUrl()).thenReturn(urlResult);

            FileUploadResponse response = fileService.upload(request);
            assertThat(response).isNotNull();
            assertThat(response.getObjectKey()).startsWith("video/");
        }
    }

    @Nested
    @DisplayName("getDownloadUrl — 获取预签名下载 URL")
    class GetDownloadUrlTests {

        @Test
        @DisplayName("正常下载：COS 生成预签名下载 URL 成功，返回 URL 字符串")
        void getDownloadUrlSuccess() {
            ResourceMeta meta = createPublicResourceMeta();
            when(resourceMetaService.get(RESOURCE_ID)).thenReturn(meta);
            when(fileStorageService.generatePresignedUrl()).thenReturn(pretreatment);

            GeneratePresignedUrlResult urlResult = new GeneratePresignedUrlResult();
            urlResult.setUrl(COS_URL);
            when(pretreatment.generatePresignedUrl()).thenReturn(urlResult);

            String url = fileService.getDownloadUrl(RESOURCE_ID);

            assertThat(url).isEqualTo(COS_URL);
        }

        @Test
        @DisplayName("资源未公开（UPLOADING/HIDDEN）时拒绝生成下载 URL")
        void getDownloadUrlRejectsNonPublic() {
            ResourceMeta meta = createResourceMeta();
            when(resourceMetaService.get(RESOURCE_ID)).thenReturn(meta);

            assertThatThrownBy(() -> fileService.getDownloadUrl(RESOURCE_ID))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("code", ResourceErrorConstants.RESOURCE_NOT_FOUND)
                    .hasMessageContaining("未公开");
            verify(fileStorageService, never()).generatePresignedUrl();
        }

        @Test
        @DisplayName("COS 异常：generatePresignedUrl 抛出异常，抛出 BusinessException(RESOURCE_NOT_FOUND)")
        void getDownloadUrlCosException() {
            ResourceMeta meta = createPublicResourceMeta();
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

    // ==================== confirmUpload ====================

    @Nested
    @DisplayName("confirmUpload — 确认文件上传")
    class ConfirmUploadTests {

        @Test
        @DisplayName("资源状态已经是 PUBLIC 时直接返回")
        void confirmUploadAlreadyPublic() {
            ResourceMeta meta = createPublicResourceMeta();
            when(resourceMetaService.get(RESOURCE_ID)).thenReturn(meta);

            ResourceMeta result = fileService.confirmUpload(RESOURCE_ID);

            assertThat(result.getStatus()).isEqualTo(ResourceStatusConstants.PUBLIC);
        }

        @Test
        @DisplayName("COS 文件存在时更新状态为 PUBLIC")
        void confirmUploadCosExists() {
            ResourceMeta meta = ResourceMeta.builder()
                    .id(RESOURCE_ID)
                    .uploaderId(1L)
                    .hash("uuid-hash")
                    .objectKey(OBJECT_KEY)
                    .status(ResourceStatusConstants.UPLOADING)
                    .build();
            ResourceMeta updated = ResourceMeta.builder()
                    .id(RESOURCE_ID)
                    .uploaderId(1L)
                    .hash("uuid-hash")
                    .objectKey(OBJECT_KEY)
                    .status(ResourceStatusConstants.PUBLIC)
                    .build();
            when(resourceMetaService.get(RESOURCE_ID)).thenReturn(meta, updated);
            when(fileStorageService.exists(any(FileInfo.class))).thenReturn(true);

            ResourceMeta result = fileService.confirmUpload(RESOURCE_ID);

            assertThat(result.getStatus()).isEqualTo(ResourceStatusConstants.PUBLIC);
            verify(resourceMetaService).updateStatus(RESOURCE_ID, ResourceStatusConstants.PUBLIC);
        }

        @Test
        @DisplayName("COS 文件不存在时抛出异常")
        void confirmUploadFileNotExists() {
            ResourceMeta meta = ResourceMeta.builder()
                    .id(RESOURCE_ID)
                    .uploaderId(1L)
                    .hash("uuid-hash")
                    .objectKey(OBJECT_KEY)
                    .status(ResourceStatusConstants.UPLOADING)
                    .build();
            when(resourceMetaService.get(RESOURCE_ID)).thenReturn(meta);
            when(fileStorageService.exists(any(FileInfo.class))).thenReturn(false);

            assertThatThrownBy(() -> fileService.confirmUpload(RESOURCE_ID))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("文件尚未上传到存储服务器");
        }
    }

    // ==================== handleUploadCallback ====================

    @Nested
    @DisplayName("handleUploadCallback — 处理上传回调")
    class HandleUploadCallbackTests {

        @Test
        @DisplayName("成功保存本地文件")
        void handleUploadCallbackSuccess() {
            ResourceMeta meta =
                    ResourceMeta.builder().id(RESOURCE_ID).objectKey(OBJECT_KEY).build();
            when(resourceMetaService.get(RESOURCE_ID)).thenReturn(meta);

            fileService.handleUploadCallback(RESOURCE_ID, mock(InputStream.class));

            verify(resourceMetaService).get(RESOURCE_ID);
        }
    }

    // ==================== getFileInfo / getFileInfoByHash ====================

    @Nested
    @DisplayName("getFileInfo — 获取文件信息")
    class GetFileInfoTests {

        @Test
        @DisplayName("根据ID获取文件信息")
        void getFileInfoById() {
            ResourceMeta meta = createPublicResourceMeta();
            when(resourceMetaService.get(RESOURCE_ID)).thenReturn(meta);
            when(fileStorageService.generatePresignedUrl()).thenReturn(pretreatment);

            GeneratePresignedUrlResult urlResult = new GeneratePresignedUrlResult();
            urlResult.setUrl(COS_URL);
            when(pretreatment.generatePresignedUrl()).thenReturn(urlResult);

            FileInfoResponse response = fileService.getFileInfo(RESOURCE_ID);

            assertThat(response).isNotNull();
            assertThat(response.getResourceId()).isEqualTo("1");
            assertThat(response.getOriginalName()).isEqualTo(FILE_NAME);
            assertThat(response.getDownloadUrl()).isEqualTo(COS_URL);
        }

        @Test
        @DisplayName("根据ID获取未公开资源信息被拒绝")
        void getFileInfoRejectsNonPublic() {
            ResourceMeta meta = createResourceMeta();
            when(resourceMetaService.get(RESOURCE_ID)).thenReturn(meta);

            assertThatThrownBy(() -> fileService.getFileInfo(RESOURCE_ID))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("code", ResourceErrorConstants.RESOURCE_NOT_FOUND)
                    .hasMessageContaining("未公开");
        }

        @Test
        @DisplayName("根据hash获取文件信息")
        void getFileInfoByHash() {
            ResourceMeta meta = createPublicResourceMeta();
            when(resourceMetaService.getByHash("uuid-hash")).thenReturn(meta);
            when(fileStorageService.generatePresignedUrl()).thenReturn(pretreatment);

            GeneratePresignedUrlResult urlResult = new GeneratePresignedUrlResult();
            urlResult.setUrl(COS_URL);
            when(pretreatment.generatePresignedUrl()).thenReturn(urlResult);

            FileInfoResponse response = fileService.getFileInfoByHash("uuid-hash");

            assertThat(response).isNotNull();
            assertThat(response.getHash()).isEqualTo("uuid-hash");
        }

        @Test
        @DisplayName("根据hash获取未公开资源信息被拒绝")
        void getFileInfoByHashRejectsNonPublic() {
            ResourceMeta meta = ResourceMeta.builder()
                    .id(RESOURCE_ID)
                    .hash("uuid-hash")
                    .objectKey(OBJECT_KEY)
                    .status(ResourceStatusConstants.HIDDEN)
                    .build();
            when(resourceMetaService.getByHash("uuid-hash")).thenReturn(meta);

            assertThatThrownBy(() -> fileService.getFileInfoByHash("uuid-hash"))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("code", ResourceErrorConstants.RESOURCE_NOT_FOUND)
                    .hasMessageContaining("未公开");
        }
    }

    // ==================== delete ====================

    @Nested
    @DisplayName("delete — 删除资源")
    class DeleteTests {

        @Test
        @DisplayName("正常删除成功")
        void deleteSuccess() {
            ResourceMeta meta = createResourceMeta();
            when(resourceMetaService.get(RESOURCE_ID)).thenReturn(meta);

            fileService.delete(RESOURCE_ID);

            verify(fileStorageService).delete(any(FileInfo.class));
            verify(resourceMetaService).delete(RESOURCE_ID);
        }

        @Test
        @DisplayName("COS 删除异常时仍然删除本地记录")
        void deleteCosException() {
            ResourceMeta meta = createResourceMeta();
            when(resourceMetaService.get(RESOURCE_ID)).thenReturn(meta);
            doThrow(new RuntimeException("COS error")).when(fileStorageService).delete(any(FileInfo.class));

            fileService.delete(RESOURCE_ID);

            verify(resourceMetaService).delete(RESOURCE_ID);
        }
    }

    // ==================== batch operations ====================

    @Nested
    @DisplayName("getBatch — 批量获取下载URL")
    class BatchTests {

        @Test
        @DisplayName("getBatchByIds 返回多个URL")
        void getBatchByIds() {
            ResourceMeta meta1 = ResourceMeta.builder()
                    .id(1L)
                    .objectKey("image/a.png")
                    .status(ResourceStatusConstants.PUBLIC)
                    .build();
            ResourceMeta meta2 = ResourceMeta.builder()
                    .id(2L)
                    .objectKey("image/b.png")
                    .status(ResourceStatusConstants.PUBLIC)
                    .build();
            when(resourceMetaService.get(1L)).thenReturn(meta1);
            when(resourceMetaService.get(2L)).thenReturn(meta2);
            when(fileStorageService.generatePresignedUrl()).thenReturn(pretreatment);

            GeneratePresignedUrlResult urlResult = new GeneratePresignedUrlResult();
            urlResult.setUrl(COS_URL);
            when(pretreatment.generatePresignedUrl()).thenReturn(urlResult);

            List<String> urls = fileService.getBatchByIds(List.of(1L, 2L));

            assertThat(urls).hasSize(2);
            assertThat(urls.get(0)).isEqualTo(COS_URL);
        }

        @Test
        @DisplayName("getBatchByHashes 返回多个URL")
        void getBatchByHashes() {
            ResourceMeta meta = ResourceMeta.builder()
                    .id(1L)
                    .hash("hash1")
                    .objectKey("image/a.png")
                    .status(ResourceStatusConstants.PUBLIC)
                    .build();
            when(resourceMetaService.getByHash("hash1")).thenReturn(meta);
            // Use lazy answer for repeated calls
            when(fileStorageService.generatePresignedUrl()).thenReturn(pretreatment);

            GeneratePresignedUrlResult urlResult = new GeneratePresignedUrlResult();
            urlResult.setUrl(COS_URL);
            when(pretreatment.generatePresignedUrl()).thenReturn(urlResult);

            List<String> urls = fileService.getBatchByHashes(List.of("hash1"));

            assertThat(urls).hasSize(1);
        }
    }

    // ==================== getByHash ====================

    @Nested
    @DisplayName("getByHash — 根据hash获取下载URL")
    class GetByHashTests {

        @Test
        @DisplayName("根据hash获取下载URL")
        void getByHash() {
            ResourceMeta meta = ResourceMeta.builder()
                    .id(1L)
                    .hash("hash1")
                    .objectKey("image/a.png")
                    .status(ResourceStatusConstants.PUBLIC)
                    .build();
            when(resourceMetaService.getByHash("hash1")).thenReturn(meta);
            when(fileStorageService.generatePresignedUrl()).thenReturn(pretreatment);

            GeneratePresignedUrlResult urlResult = new GeneratePresignedUrlResult();
            urlResult.setUrl(COS_URL);
            when(pretreatment.generatePresignedUrl()).thenReturn(urlResult);

            String url = fileService.getByHash("hash1");

            assertThat(url).isEqualTo(COS_URL);
        }

        @Test
        @DisplayName("未公开资源按hash获取下载URL被拒绝")
        void getByHashRejectsNonPublic() {
            ResourceMeta meta = ResourceMeta.builder()
                    .id(1L)
                    .hash("hash1")
                    .objectKey("image/a.png")
                    .build();
            when(resourceMetaService.getByHash("hash1")).thenReturn(meta);

            assertThatThrownBy(() -> fileService.getByHash("hash1"))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("code", ResourceErrorConstants.RESOURCE_NOT_FOUND)
                    .hasMessageContaining("未公开");
        }
    }
}
