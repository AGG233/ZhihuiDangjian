package com.rauio.smartdangjian.server.graph.pojo.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "知识图谱节点视图对象")
public class GraphNodeVO {
    @Schema(description = "节点ID")
    private String id;
    @Schema(description = "节点标签")
    private String label;
    @Schema(description = "节点名称")
    private String name;
}
