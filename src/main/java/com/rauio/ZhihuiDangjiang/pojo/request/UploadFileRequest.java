package com.rauio.ZhihuiDangjiang.pojo.request;

import lombok.Builder;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
@Builder
public class UploadFileRequest {
    private MultipartFile file;
}
