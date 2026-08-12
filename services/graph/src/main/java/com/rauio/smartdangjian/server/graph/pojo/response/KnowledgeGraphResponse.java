package com.rauio.smartdangjian.server.graph.pojo.response;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "知识图谱视图对象")
public class KnowledgeGraphResponse {
    @Schema(description = "节点列表")
    private List<GraphNodeResponse> nodes;

    @Schema(description = "边列表")
    private List<GraphEdgeResponse> edges;
}
