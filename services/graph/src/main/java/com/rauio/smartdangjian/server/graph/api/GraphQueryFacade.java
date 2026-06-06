package com.rauio.smartdangjian.server.graph.api;

import java.util.List;

import com.rauio.smartdangjian.server.graph.pojo.response.KnowledgeGraphResponse;

public interface GraphQueryFacade {

    KnowledgeGraphResponse searchEntities(String keyword, List<String> entityTypes, int pageNum, int pageSize);

    KnowledgeGraphResponse getEntityDetail(String graphId);

    KnowledgeGraphResponse getTheoryEvolution(String graphId);

    KnowledgeGraphResponse getEventTimeline(String graphId, int depth);

    KnowledgeGraphResponse findConnection(String sourceId, String targetId, int maxDepth);

    KnowledgeGraphResponse inferPersonInfluence(String graphId, int depth);
}
