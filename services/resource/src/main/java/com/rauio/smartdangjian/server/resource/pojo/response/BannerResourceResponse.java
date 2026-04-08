package com.rauio.smartdangjian.server.resource.pojo.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "用户侧轮播图响应，包含轮播顺序、资源标识和可直接访问的预签名链接")
public record BannerResourceResponse(
        @Schema(description = "轮播图顺序，从0开始", example = "0")
        int order,
        @Schema(description = "资源ID", example = "1919810")
        String resourceId,
        @Schema(description = "文件原始名称", example = "banner-1.png")
        String originalName,
        @Schema(description = "资源哈希", example = "8f14e45fceea167a5a36dedd4bea2543")
        String hash,
        @Schema(description = "对象存储键", example = "resource/8f14e45fceea167a5a36dedd4bea2543.png")
        String objectKey,
        @Schema(description = "资源类型", example = "image/png")
        String resourceType,
        @Schema(description = "资源状态：0表示禁用，1表示可用，2表示已删除", example = "1")
        Integer status,
        @Schema(description = "可直接访问的预签名下载链接，当前默认有效期为10分钟")
        String downloadUrl
) {
}
