package com.rauio.smartdangjian.server.content.pojo.vo;

import com.rauio.smartdangjian.server.content.spec.BlockType;
import com.rauio.smartdangjian.server.content.spec.ParentType;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "内容块视图对象")
public class ContentBlockVO {

    @Schema(description = "父级内容块ID")
    private String parentId;

    @Schema(description = "父级内容块类型")
    private ParentType parentType;

    @Schema(description = "内容块类型")
    private BlockType blockType;

    @Schema(description = "内容块的文本内容")
    private String textContent;

    @Schema(description = "内容块的资源ID")
    private String resourceId;

    @Schema(description = "内容块的额外说明")
    private String caption;
}
