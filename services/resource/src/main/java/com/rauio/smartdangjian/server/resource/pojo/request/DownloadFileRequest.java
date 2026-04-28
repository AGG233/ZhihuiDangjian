package com.rauio.smartdangjian.server.resource.pojo.request;


import jakarta.activation.MimeType;
import lombok.Data;

@Data
public class DownloadFileRequest {
    private String fileName;
    private MimeType mimeType;
    private String hash;
}
