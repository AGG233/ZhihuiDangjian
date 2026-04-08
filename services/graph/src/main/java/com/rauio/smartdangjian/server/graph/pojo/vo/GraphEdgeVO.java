package com.rauio.smartdangjian.server.graph.pojo.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "知识图谱边视图对象")
public class GraphEdgeVO {
    @Schema(description = "起始节点ID")
    private String source;
    @Schema(description = "目标节点ID")
    private String target;
    @Schema(description = "关系类型")
    private String type;
}
