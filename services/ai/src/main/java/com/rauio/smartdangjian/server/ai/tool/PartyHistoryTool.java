package com.rauio.smartdangjian.server.ai.tool;

import java.util.ArrayList;
import java.util.List;

import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rauio.smartdangjian.server.graph.pojo.response.KnowledgeGraphResponse;
import com.rauio.smartdangjian.server.graph.service.PartyHistoryQueryService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class PartyHistoryTool {

    private final PartyHistoryQueryService queryService;
    private final ObjectMapper objectMapper;

    @Tool(description = "搜索党史知识图谱：根据关键词查找人物、事件、地点、理论、文献。返回匹配的实体列表。可用于回答党史人物事迹、重大事件、理论渊源等问题。")
    public String searchPartyHistory(
            @ToolParam(description = "搜索关键词，如人物名、事件名、理论名等") String keyword,
            @ToolParam(description = "实体类型过滤，可选值: Person, Event, Location, Theory, Document。为空则搜索所有类型")
                    List<String> entityTypes,
            @ToolParam(description = "返回数量限制，默认10") Integer limit,
            ToolContext toolContext) {
        int size = limit != null && limit > 0 ? limit : 10;
        List<String> types = entityTypes != null ? entityTypes : new ArrayList<>();
        var result = queryService.searchEntities(keyword, types, 1, size);
        return toJsonString(result);
    }

    @Tool(description = "查询党史人物详情和关联信息：获取人物的基本信息、参与的事件、提出的理论等。可用于深入了解某位党史人物的生平与贡献。")
    public String getPersonDetail(@ToolParam(description = "人物姓名，如'邓小平'") String name, ToolContext toolContext) {
        var searchResult = queryService.searchEntities(name, List.of("Person"), 1, 1);
        if (searchResult.getNodes().isEmpty()) {
            return "{\"message\": \"未找到人物: " + name + "\"}";
        }
        String graphId = extractGraphId(searchResult);
        if (graphId == null) {
            return toJsonString(searchResult);
        }
        var detail = queryService.getEntityDetail(graphId);
        log.debug("查询人物详情 graphId={} nodes={}", graphId, detail.getNodes().size());
        return toJsonString(detail);
    }

    @Tool(description = "追溯理论发展脉络：从某一理论出发，查看其来源和后续发展，展现理论的继承与创新关系。可用于回答'某某理论是如何发展的'等问题。")
    public String traceTheoryEvolution(
            @ToolParam(description = "理论名称，如'邓小平理论'") String theoryName, ToolContext toolContext) {
        var searchResult = queryService.searchEntities(theoryName, List.of("Theory"), 1, 1);
        if (searchResult.getNodes().isEmpty()) {
            return "{\"message\": \"未找到理论: " + theoryName + "\"}";
        }
        String graphId = extractGraphId(searchResult);
        if (graphId == null) {
            return toJsonString(searchResult);
        }
        var evolution = queryService.getTheoryEvolution(graphId);
        log.debug("追溯理论演化 graphId={} nodes={}", graphId, evolution.getNodes().size());
        return toJsonString(evolution);
    }

    @Tool(description = "查询历史事件的因果链和时间线：展示事件的来龙去脉。可用于回答'某某事件的前因后果'等问题。")
    public String getEventTimeline(
            @ToolParam(description = "事件名称，如'十一届三中全会'") String eventName, ToolContext toolContext) {
        var searchResult = queryService.searchEntities(eventName, List.of("Event"), 1, 1);
        if (searchResult.getNodes().isEmpty()) {
            return "{\"message\": \"未找到事件: " + eventName + "\"}";
        }
        String graphId = extractGraphId(searchResult);
        if (graphId == null) {
            return toJsonString(searchResult);
        }
        var timeline = queryService.getEventTimeline(graphId, 3);
        log.debug("查询事件时间线 graphId={} nodes={}", graphId, timeline.getNodes().size());
        return toJsonString(timeline);
    }

    @Tool(description = "查找两个党史实体之间的关联路径：发现人物与事件、理论与事件等之间的最短联系链。可用于回答'A和B有什么关系'等问题。")
    public String findConnection(
            @ToolParam(description = "起始实体名称，如'陈云'") String sourceName,
            @ToolParam(description = "目标实体名称，如'改革开放'") String targetName,
            ToolContext toolContext) {
        String sourceId = resolveGraphId(sourceName);
        String targetId = resolveGraphId(targetName);
        if (sourceId == null || targetId == null) {
            return "{\"message\": \"未找到实体: " + (sourceId == null ? sourceName : "")
                    + (targetId == null ? " " + targetName : "") + "\"}";
        }
        var connection = queryService.findConnection(sourceId, targetId, 4);
        log.debug(
                "查找关联路径 {}->{} nodes={}",
                sourceId,
                targetId,
                connection.getNodes().size());
        return toJsonString(connection);
    }

    @Tool(description = "推断人物的党史影响力网络：从人物出发，沿关系展开多层级子图，展示人物的影响范围。可用于回答'某某人物产生了哪些影响'等问题。")
    public String inferPersonInfluence(
            @ToolParam(description = "人物姓名，如'毛泽东'") String personName, ToolContext toolContext) {
        var searchResult = queryService.searchEntities(personName, List.of("Person"), 1, 1);
        if (searchResult.getNodes().isEmpty()) {
            return "{\"message\": \"未找到人物: " + personName + "\"}";
        }
        String graphId = extractGraphId(searchResult);
        if (graphId == null) {
            return toJsonString(searchResult);
        }
        var influence = queryService.inferPersonInfluence(graphId, 4);
        log.debug("推断人物影响力 graphId={} nodes={}", graphId, influence.getNodes().size());
        return toJsonString(influence);
    }

    private String resolveGraphId(String name) {
        var result = queryService.searchEntities(name, List.of(), 1, 1);
        return extractGraphId(result);
    }

    private String extractGraphId(KnowledgeGraphResponse response) {
        if (response.getNodes() == null || response.getNodes().isEmpty()) {
            return null;
        }
        String id = response.getNodes().getFirst().getId();
        if (id != null && id.contains(":")) {
            return id.substring(id.indexOf(":") + 1);
        }
        return id;
    }

    private String toJsonString(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            log.error("序列化 KnowledgeGraphResponse 失败", e);
            return "{}";
        }
    }
}
