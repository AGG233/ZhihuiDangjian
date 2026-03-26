package com.rauio.smartdangjian.server.resource.pojo.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "分片上传进度响应。前端可用该响应恢复当前uploadId的状态并继续断点续传")
public record MultipartProgressResponse(
        @Schema(description = "上传会话ID，对应一条具体的分片上传任务", example = "upload-id")
        String uploadId,
        @Schema(description = "对象存储键，即该文件在COS中的目标路径", example = "resource/abc.mp4")
        String objectKey,
        @Schema(description = "上传状态，典型值包括UPLOADING、COMPLETED、ABORTED", example = "UPLOADING")
        String status,
        @Schema(description = "已上传分片列表，前端可据此计算上传进度并跳过已成功分片")
        List<MultipartUploadedPartResponse> uploadedParts
) {
}
