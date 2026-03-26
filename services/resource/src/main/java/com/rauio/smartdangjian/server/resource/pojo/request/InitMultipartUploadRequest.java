package com.rauio.smartdangjian.server.resource.pojo.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "初始化分片上传请求。用于秒传判定、断点续传恢复以及创建新的COS分片上传会话")
public record InitMultipartUploadRequest(
        @Schema(description = "文件内容哈希，通常由前端预先计算，用于秒传判定和资源去重", example = "8f14e45fceea167a5a36dedd4bea2543")
        @NotBlank(message = "fileHash不能为空")
        String fileHash,
        @Schema(description = "文件原始名称，包含文件名本体，通常不要求包含完整路径", example = "lesson.mp4")
        @NotBlank(message = "fileName不能为空")
        String fileName,
        @Schema(description = "文件后缀名，服务端会进一步标准化处理，可传mp4或.mp4", example = "mp4")
        @NotBlank(message = "suffix不能为空")
        String suffix,
        @Schema(description = "文件MIME类型，用于COS元数据和资源类型记录", example = "video/mp4")
        @NotBlank(message = "contentType不能为空")
        String contentType,
        @Schema(description = "文件总大小，单位字节", example = "104857600")
        @NotNull(message = "fileSize不能为空")
        @Min(value = 1, message = "fileSize必须大于0")
        Long fileSize,
        @Schema(description = "单个分片大小，单位字节，当前约束最小为1MB", example = "5242880")
        @NotNull(message = "partSize不能为空")
        @Min(value = 1048576, message = "partSize不能小于1MB")
        Long partSize
) {
}
