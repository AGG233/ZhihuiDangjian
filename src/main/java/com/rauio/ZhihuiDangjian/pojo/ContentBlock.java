package com.rauio.ZhihuiDangjian.pojo;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.rauio.ZhihuiDangjian.utils.Spec.BlockType;
import com.rauio.ZhihuiDangjian.utils.Spec.ParentType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;

import java.time.LocalDateTime;

@Data
@Builder
@ToString
@TableName("content_block")
@Schema(description = "内容块")
public class ContentBlock {

    @Schema(description = "内容块ID")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long      id;

    @Schema(description = "父级内容块ID")
    private Long      parentId;
    @EnumValue
    @Schema(description = "父级内容块类型")
    private ParentType parentType;
    @EnumValue
    @Schema(description = "内容块类型")
    private BlockType   blockType;
    @Schema(description = "内容块的文本内容")
    private String      textContent;

    @Schema(description = "内容块的资源ID")
    private String      resourceId;

    @Schema(description = "内容块的额外说明")
    private String      caption;

    @Schema(description = "内容块的创建时间")
    private LocalDateTime        createdAt;

    @Schema(description = "内容块的更新时间")
    private LocalDateTime        updatedAt;
}
