package com.rauio.smartdangjian.server.content.pojo.entity;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.rauio.smartdangjian.server.content.spec.BlockType;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;

@Data
@Builder
@ToString
@TableName("chapter_content_block")
@Schema(description = "章节内容块")
public class ChapterContentBlock {

    @TableId
    @Schema(description = "内容块ID")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    @Schema(description = "所属章节ID")
    private Long chapterId;

    @Schema(description = "内容块排序序号")
    private Integer orderIndex;

    @EnumValue
    @Schema(description = "内容块类型")
    private BlockType blockType;

    @Schema(description = "内容块的文本内容")
    private String textContent;

    @Schema(description = "内容块的资源ID")
    private Long resourceId;

    @Schema(description = "内容块的额外说明")
    private String caption;

    @Schema(description = "内容块的创建时间")
    private LocalDateTime createdAt;

    @Schema(description = "内容块的更新时间")
    private LocalDateTime updatedAt;
}
