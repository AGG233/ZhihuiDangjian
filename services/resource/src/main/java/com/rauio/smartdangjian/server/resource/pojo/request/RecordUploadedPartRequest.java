package com.rauio.smartdangjian.server.resource.pojo.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "记录已上传分片请求。当前端成功将某个分片PUT到COS后，应立即回传对应的partNumber和ETag")
public record RecordUploadedPartRequest(
        @Schema(description = "上传会话ID，由初始化接口返回", example = "upload-id")
        @NotBlank(message = "uploadId不能为空")
        String uploadId,
        @Schema(description = "分片序号，从1开始，必须与COS上传时的partNumber一致", example = "1")
        @Min(value = 1, message = "partNumber必须大于0")
        int partNumber,
        @Schema(description = "分片ETag，从COS成功响应头中获取，后续合并分片时依赖该值", example = "\"etag-value\"")
        @NotBlank(message = "etag不能为空")
        String etag
) {
}
