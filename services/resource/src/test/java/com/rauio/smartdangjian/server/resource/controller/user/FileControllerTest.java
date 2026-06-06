package com.rauio.smartdangjian.server.resource.controller.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

import java.util.List;

import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.rauio.smartdangjian.security.CurrentUserProvider;
import com.rauio.smartdangjian.server.resource.pojo.entity.ResourceMeta;
import com.rauio.smartdangjian.server.resource.pojo.request.UploadFileRequest;
import com.rauio.smartdangjian.server.resource.pojo.response.FileInfoResponse;
import com.rauio.smartdangjian.server.resource.pojo.response.FileUploadResponse;
import com.rauio.smartdangjian.server.resource.service.FileService;

@ExtendWith(MockitoExtension.class)
class FileControllerTest {

    @Mock
    private FileService fileService;

    @Mock
    private HttpServletRequest httpServletRequest;

    @Mock
    private CurrentUserProvider currentUserProvider;

    @InjectMocks
    private FileController controller;

    @Test
    @DisplayName("upload 委托 service 上传文件")
    void upload() {
        UploadFileRequest request = new UploadFileRequest();
        request.setFileName("test.png");
        request.setMimeType("image/png");

        when(fileService.upload(any(UploadFileRequest.class)))
                .thenReturn(FileUploadResponse.builder()
                        .resourceId("1")
                        .uploadUrl("https://example.com/upload")
                        .build());
        when(currentUserProvider.getCurrentUserId()).thenReturn("1");

        var result = controller.upload(request);

        assertThat(result.getData().getResourceId()).isEqualTo("1");
        assertThat(request.getUserId()).isEqualTo("1");
    }

    @Test
    @DisplayName("confirmUpload 委托 service 确认上传")
    void confirmUpload() {
        when(fileService.confirmUpload(1L))
                .thenReturn(ResourceMeta.builder().id(1L).build());

        var result = controller.confirmUpload(1L);

        assertThat(result.getData().id()).isEqualTo("1");
    }

    @Test
    @DisplayName("getById 委托 service 获取文件信息")
    void getById() {
        when(fileService.getFileInfo(1L))
                .thenReturn(FileInfoResponse.builder().resourceId("1").build());

        var result = controller.getById(1L);

        assertThat(result.getData().getResourceId()).isEqualTo("1");
    }

    @Test
    @DisplayName("getByHash 委托 service 按哈希获取文件信息")
    void getByHash() {
        when(fileService.getFileInfoByHash("hash123"))
                .thenReturn(FileInfoResponse.builder()
                        .resourceId("1")
                        .hash("hash123")
                        .build());

        var result = controller.getByHash("hash123");

        assertThat(result.getData().getHash()).isEqualTo("hash123");
    }

    @Test
    @DisplayName("getDownloadUrl 委托 service 获取下载链接")
    void getDownloadUrl() {
        when(fileService.getDownloadUrl(1L)).thenReturn("https://example.com/download");

        var result = controller.getDownloadUrl(1L);

        assertThat(result.getData()).isEqualTo("https://example.com/download");
    }

    @Test
    @DisplayName("getBatchById 委托 service 批量获取下载链接")
    void getBatchById() {
        when(fileService.getBatchByIds(anyList())).thenReturn(List.of("url1", "url2"));

        var result = controller.getBatchById(List.of(1L, 2L));

        assertThat(result.getData()).hasSize(2);
    }

    @Test
    @DisplayName("getBatchByHash 委托 service 按哈希批量获取下载链接")
    void getBatchByHash() {
        when(fileService.getBatchByHashes(anyList())).thenReturn(List.of("url1"));

        var result = controller.getBatchByHash(List.of("hash1"));

        assertThat(result.getData()).hasSize(1);
    }

    @Test
    @DisplayName("delete 委托 service 删除文件")
    void delete() {
        doNothing().when(fileService).delete(1L);

        var result = controller.delete(1L);

        assertThat(result.getData()).isTrue();
    }

    @Test
    @DisplayName("uploadCallback 委托 service 处理上传回调")
    void uploadCallback() throws Exception {
        ServletInputStream servletInputStream = mock(ServletInputStream.class);
        when(httpServletRequest.getInputStream()).thenReturn(servletInputStream);
        doNothing().when(fileService).handleUploadCallback(any(), any());

        var result = controller.uploadCallback(1L, httpServletRequest);

        assertThat(result.getCode()).isEqualTo("200");
    }
}
