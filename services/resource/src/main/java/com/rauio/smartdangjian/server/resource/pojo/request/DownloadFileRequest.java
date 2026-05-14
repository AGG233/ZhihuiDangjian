package com.rauio.smartdangjian.server.resource.pojo.request;

import lombok.Data;

@Data
@Schema(description = "文件下载请求")
public class DownloadFileRequest {
    @Schema(description = "文件名")
    private String fileName;
    private String mimeType;
    private String hash;
}
