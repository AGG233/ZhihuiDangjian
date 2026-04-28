package com.rauio.smartdangjian.server.resource.pojo.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "文件信息响应，包含资源元数据和预签名下载链接")
public class FileInfoResponse {

    @Schema(description = "资源ID", example = "1919810")
    private String resourceId;

    @Schema(description = "文件原始名称", example = "党课封面.png")
    private String originalName;

    @Schema(description = "文件内容哈希", example = "8f14e45fceea167a5a36dedd4bea2543")
    private String hash;

    @Schema(description = "对象存储键", example = "image/uuid.png")
    private String objectKey;

    @Schema(description = "资源类型：0表示图片，1表示视频", example = "0")
    private Integer resourceType;

    @Schema(description = "资源状态：0表示上传中，1表示公开可用，2表示隐藏", example = "1")
    private Integer status;

    @Schema(description = "预签名下载链接，具有时效性", example = "https://bucket.cos.ap-guangzhou.myqcloud.com/image/uuid.png?sign=...")
    private String downloadUrl;

    @Schema(description = "文件大小（字节），COS端获取，可能为null", example = "1048576")
    private Long size;
}
