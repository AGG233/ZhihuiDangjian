package com.rauio.smartdangjian.server.resource.controller.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.rauio.smartdangjian.server.resource.pojo.entity.ResourceMeta;
import com.rauio.smartdangjian.server.resource.pojo.request.UploadFileRequest;
import com.rauio.smartdangjian.server.resource.pojo.response.FileInfoResponse;
import com.rauio.smartdangjian.server.resource.pojo.response.FileUploadResponse;
import com.rauio.smartdangjian.server.resource.service.FileService;

@ExtendWith(MockitoExtension.class)
class FileControllerTest {

    @Mock
    private FileService fileService;

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
                        .resourceId("r-1")
                        .uploadUrl("https://example.com/upload")
                        .build());

        var result = controller.upload(request);

        assertThat(result.getData().getResourceId()).isEqualTo("r-1");
    }

    @Test
    @DisplayName("confirmUpload 委托 service 确认上传")
    void confirmUpload() {
        when(fileService.confirmUpload("r-1"))
                .thenReturn(ResourceMeta.builder().id("r-1").build());

        var result = controller.confirmUpload("r-1");

        assertThat(result.getData().getId()).isEqualTo("r-1");
    }

    @Test
    @DisplayName("getById 委托 service 获取文件信息")
    void getById() {
        when(fileService.getFileInfo("r-1"))
                .thenReturn(FileInfoResponse.builder().resourceId("r-1").build());

        var result = controller.getById("r-1");

        assertThat(result.getData().getResourceId()).isEqualTo("r-1");
    }

    @Test
    @DisplayName("getByHash 委托 service 按哈希获取文件信息")
    void getByHash() {
        when(fileService.getFileInfoByHash("hash123"))
                .thenReturn(FileInfoResponse.builder()
                        .resourceId("r-1")
                        .hash("hash123")
                        .build());

        var result = controller.getByHash("hash123");

        assertThat(result.getData().getHash()).isEqualTo("hash123");
    }

    @Test
    @DisplayName("getDownloadUrl 委托 service 获取下载链接")
    void getDownloadUrl() {
        when(fileService.getDownloadUrl("r-1")).thenReturn("https://example.com/download");

        var result = controller.getDownloadUrl("r-1");

        assertThat(result.getData()).isEqualTo("https://example.com/download");
    }

    @Test
    @DisplayName("getBatchById 委托 service 批量获取下载链接")
    void getBatchById() {
        when(fileService.getBatchByIds(anyList())).thenReturn(List.of("url1", "url2"));

        var result = controller.getBatchById(List.of("r-1", "r-2"));

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
        doNothing().when(fileService).delete("r-1");

        var result = controller.delete("r-1");

        assertThat(result.getData()).isTrue();
    }
}
