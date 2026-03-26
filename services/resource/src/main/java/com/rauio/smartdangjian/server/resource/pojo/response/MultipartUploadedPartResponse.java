package com.rauio.smartdangjian.server.resource.pojo.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "已上传分片信息，描述某个partNumber已经完成上传并记录了对应的ETag")
public record MultipartUploadedPartResponse(
        @Schema(description = "分片序号，从1开始", example = "1")
        int partNumber,
        @Schema(description = "分片ETag，由COS返回，最终完成分片合并时必须携带", example = "\"etag-value\"")
        String etag
) {
}
