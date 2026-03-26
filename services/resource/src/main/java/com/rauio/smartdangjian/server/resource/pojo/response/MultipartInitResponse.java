package com.rauio.smartdangjian.server.resource.pojo.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "初始化分片上传响应。用于告诉前端当前文件是否可秒传、是否已有可恢复会话，以及后续上传所需的基础参数")
public record MultipartInitResponse(
        @Schema(description = "是否命中秒传。为true时表示资源已存在，前端无需继续上传文件内容", example = "false")
        boolean instantUpload,
        @Schema(description = "上传会话ID。instantUpload=true时通常为空，否则用于后续所有分片操作", example = "upload-id")
        String uploadId,
        @Schema(description = "对象存储键，即本次上传目标文件在COS中的存储路径", example = "resource/abc.mp4")
        String objectKey,
        @Schema(description = "服务端确认的分片大小，前端应严格按该大小切片", example = "5242880")
        Long partSize,
        @Schema(description = "已上传分片列表。用于断点续传时跳过已成功上传的分片")
        List<MultipartUploadedPartResponse> uploadedParts,
        @Schema(description = "已存在资源ID。仅在命中秒传时返回，表示系统内已存在对应资源", example = "1919810")
        String resourceId
) {
}
