package com.rauio.smartdangjian.server.resource.pojo.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.util.MimeType;

@Data
public class UploadFileRequest {

    @NotBlank(message = "文件名不能为空")
    private String  fileName;

    @NotNull(message = "文件类型不能为空")
    private MimeType mimeType;
}
