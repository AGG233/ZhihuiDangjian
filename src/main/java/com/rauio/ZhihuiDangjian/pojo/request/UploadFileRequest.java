package com.rauio.ZhihuiDangjian.pojo.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
@Builder
@Schema(description = "上传文件请求")
public class UploadFileRequest {

    @Schema(description = "上传的文件")
    private MultipartFile file;
}
