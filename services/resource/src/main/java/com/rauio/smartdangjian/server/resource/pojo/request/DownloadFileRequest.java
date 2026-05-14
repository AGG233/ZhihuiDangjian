package com.rauio.smartdangjian.server.resource.pojo.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.activation.MimeType;
import lombok.Data;

@Data
@Schema(description = "文件下载请求")
public class DownloadFileRequest {
    @Schema(description = "文件名")
    private String fileName;
    @Schema(description = "MIME类型")
    private MimeType mimeType;
    @Schema(description = "文件哈希")
    private String hash;
}
