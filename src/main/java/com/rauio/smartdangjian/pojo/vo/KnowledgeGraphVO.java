package com.rauio.smartdangjian.pojo.vo;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class KnowledgeGraphVO {
    private List<GraphNodeVO> nodes;
    private List<GraphEdgeVO> edges;
}
