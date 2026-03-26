package com.rauio.smartdangjian.server.resource.pojo.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "轮播图创建请求。调用方应在resourceId和hash中至少提供一个，系统会据此定位已存在资源并加入轮播列表")
public record BannerCreateRequest(
        @Schema(description = "资源ID。优先用于直接引用系统内已存在资源", example = "1919810")
        String resourceId,
        @Schema(description = "资源哈希。当未知资源ID时，可通过哈希定位已入库资源", example = "8f14e45fceea167a5a36dedd4bea2543")
        String hash
) {
}
