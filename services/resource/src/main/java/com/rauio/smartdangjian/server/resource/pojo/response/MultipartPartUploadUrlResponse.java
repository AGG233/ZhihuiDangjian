package com.rauio.smartdangjian.server.resource.pojo.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "分片上传地址响应。前端应对uploadUrl直接发起HTTP PUT请求上传对应分片二进制内容")
public record MultipartPartUploadUrlResponse(
        @Schema(description = "上传会话ID，用于标识该地址属于哪一次分片上传会话", example = "upload-id")
        String uploadId,
        @Schema(description = "分片序号，对应本次预签名地址可上传的分片编号", example = "1")
        int partNumber,
        @Schema(description = "COS预签名上传地址，通常带有时效性和签名参数")
        String uploadUrl
) {
}
