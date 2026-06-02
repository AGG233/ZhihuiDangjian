package com.rauio.smartdangjian.server.resource.pojo.response;

import com.rauio.smartdangjian.server.resource.pojo.entity.ResourceMeta;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "资源元数据响应")
public record ResourceMetaResponse(
        @Schema(description = "资源ID", example = "1919810") String id,
        @Schema(description = "上传人ID", example = "114514") String uploaderId,
        @Schema(description = "文件原始名称", example = "党课封面.png") String originalName,
        @Schema(description = "文件内容哈希", example = "8f14e45fceea167a5a36dedd4bea2543") String hash,
        @Schema(description = "对象存储键", example = "image/uuid.png") String objectKey,
        @Schema(description = "资源类型：0表示图片，1表示视频", example = "0") Integer resourceType,
        @Schema(description = "资源状态：0表示上传中，1表示公开可用，2表示隐藏", example = "1") Integer status) {

    public static ResourceMetaResponse from(ResourceMeta meta) {
        if (meta == null) {
            return null;
        }
        return new ResourceMetaResponse(
                meta.getId() == null ? null : String.valueOf(meta.getId()),
                meta.getUploaderId() == null ? null : String.valueOf(meta.getUploaderId()),
                meta.getOriginalName(),
                meta.getHash(),
                meta.getObjectKey(),
                meta.getResourceType(),
                meta.getStatus());
    }
}
