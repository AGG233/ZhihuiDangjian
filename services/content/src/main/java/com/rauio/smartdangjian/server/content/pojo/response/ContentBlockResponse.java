package com.rauio.smartdangjian.server.content.pojo.response;

import com.rauio.smartdangjian.server.content.spec.BlockType;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "内容块视图对象")
public class ContentBlockResponse {

    @Schema(description = "父级内容块ID")
    private Long parentId;

    @Schema(description = "内容块类型")
    private BlockType blockType;

    @Schema(description = "内容块的文本内容")
    private String textContent;

    @Schema(description = "内容块的资源ID")
    private Long resourceId;

    @Schema(description = "内容块的额外说明")
    private String caption;
}
