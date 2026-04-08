package com.rauio.smartdangjian.server.graph.pojo.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@Schema(description = "知识图谱视图对象")
public class KnowledgeGraphVO {
    @Schema(description = "节点列表")
    private List<GraphNodeVO> nodes;
    @Schema(description = "边列表")
    private List<GraphEdgeVO> edges;
}
