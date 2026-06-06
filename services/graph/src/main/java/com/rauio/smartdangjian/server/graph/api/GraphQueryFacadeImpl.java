package com.rauio.smartdangjian.server.graph.api;

import java.util.List;

import org.springframework.stereotype.Component;

import com.rauio.smartdangjian.server.graph.pojo.response.KnowledgeGraphResponse;
import com.rauio.smartdangjian.server.graph.service.PartyHistoryQueryService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class GraphQueryFacadeImpl implements GraphQueryFacade {

    private final PartyHistoryQueryService partyHistoryQueryService;

    @Override
    public KnowledgeGraphResponse searchEntities(String keyword, List<String> entityTypes, int pageNum, int pageSize) {
        return partyHistoryQueryService.searchEntities(keyword, entityTypes, pageNum, pageSize);
    }

    @Override
    public KnowledgeGraphResponse getEntityDetail(String graphId) {
        return partyHistoryQueryService.getEntityDetail(graphId);
    }

    @Override
    public KnowledgeGraphResponse getTheoryEvolution(String graphId) {
        return partyHistoryQueryService.getTheoryEvolution(graphId);
    }

    @Override
    public KnowledgeGraphResponse getEventTimeline(String graphId, int depth) {
        return partyHistoryQueryService.getEventTimeline(graphId, depth);
    }

    @Override
    public KnowledgeGraphResponse findConnection(String sourceId, String targetId, int maxDepth) {
        return partyHistoryQueryService.findConnection(sourceId, targetId, maxDepth);
    }

    @Override
    public KnowledgeGraphResponse inferPersonInfluence(String graphId, int depth) {
        return partyHistoryQueryService.inferPersonInfluence(graphId, depth);
    }
}
