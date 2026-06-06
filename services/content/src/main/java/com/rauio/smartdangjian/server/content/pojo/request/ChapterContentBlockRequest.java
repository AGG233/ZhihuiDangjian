package com.rauio.smartdangjian.server.content.pojo.request;

import jakarta.validation.constraints.NotNull;

import com.rauio.smartdangjian.server.content.pojo.entity.ChapterContentBlock;
import com.rauio.smartdangjian.server.content.spec.BlockType;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "章节内容块请求")
public record ChapterContentBlockRequest(
        @Schema(description = "内容块ID，创建时留空") Long id,
        @Schema(description = "内容块排序序号") Integer orderIndex,
        @NotNull @Schema(description = "内容块类型") BlockType blockType,
        @Schema(description = "内容块的文本内容") String textContent,
        @Schema(description = "内容块的资源ID") Long resourceId,
        @Schema(description = "内容块的额外说明") String caption) {

    public ChapterContentBlock toEntity(Long chapterId) {
        return ChapterContentBlock.builder()
                .id(id)
                .chapterId(chapterId)
                .orderIndex(orderIndex)
                .blockType(blockType)
                .textContent(textContent)
                .resourceId(resourceId)
                .caption(caption)
                .build();
    }
}
