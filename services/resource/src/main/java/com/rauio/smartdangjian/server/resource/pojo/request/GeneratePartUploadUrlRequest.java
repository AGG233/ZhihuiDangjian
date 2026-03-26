package com.rauio.smartdangjian.server.resource.pojo.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "生成分片上传地址请求。前端在拿到uploadId后，需按分片序号逐个请求对应的预签名PUT地址")
public record GeneratePartUploadUrlRequest(
        @Schema(description = "上传会话ID，由初始化分片上传接口返回", example = "upload-id")
        @NotBlank(message = "uploadId不能为空")
        String uploadId,
        @Schema(description = "分片序号，从1开始递增，必须与前端实际上传的分片序号一致", example = "1")
        @Min(value = 1, message = "partNumber必须大于0")
        int partNumber
) {
}
