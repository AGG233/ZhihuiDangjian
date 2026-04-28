package com.rauio.smartdangjian.server.resource.pojo.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "文件上传响应，包含预签名上传URL和资源标识")
public class FileUploadResponse {

    @Schema(description = "资源ID，后续确认上传和获取下载链接时使用", example = "1919810")
    private String resourceId;

    @Schema(description = "预签名PUT上传地址，前端应直接对该地址发起HTTP PUT请求上传文件体", example = "https://bucket.cos.ap-guangzhou.myqcloud.com/image/uuid.png?sign=...")
    private String uploadUrl;

    @Schema(description = "对象存储键，即文件在COS中的完整存储路径", example = "image/uuid.png")
    private String objectKey;

    @Schema(description = "预签名URL过期时间戳（毫秒）", example = "1714291200000")
    private Long expiration;
}
