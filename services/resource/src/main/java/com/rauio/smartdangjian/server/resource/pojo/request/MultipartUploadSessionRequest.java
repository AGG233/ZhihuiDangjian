package com.rauio.smartdangjian.server.resource.pojo.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "分片上传会话请求。用于查询进度、完成上传或终止上传等依赖uploadId的操作")
public record MultipartUploadSessionRequest(
        @Schema(description = "上传会话ID，由初始化分片上传接口返回，代表一次具体的COS分片上传会话", example = "upload-id")
        @NotBlank(message = "uploadId不能为空")
        String uploadId
) {
}
