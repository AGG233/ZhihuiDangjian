package com.rauio.smartdangjian.server.ai.tool;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import com.rauio.smartdangjian.server.ai.util.ToolContextUtil;
import com.rauio.smartdangjian.server.graph.constants.GraphConstants;
import com.rauio.smartdangjian.server.graph.pojo.response.GraphEdgeResponse;
import com.rauio.smartdangjian.server.graph.pojo.response.GraphNodeResponse;
import com.rauio.smartdangjian.server.graph.pojo.response.KnowledgeGraphResponse;
import com.rauio.smartdangjian.server.graph.service.KnowledgeGraphService;
import com.rauio.smartdangjian.server.user.service.UserService;

import lombok.RequiredArgsConstructor;

/**
 * 知识图谱评价工具：为 Agent 提供当前用户的学习知识图谱结构摘要（节点、关系、覆盖范围），
 * 供大语言模型对用户的知识图谱进行评价分析（对应模块文档 3.4）。
 */
@Component
@RequiredArgsConstructor
public class GraphEvaluationTool {

    private final KnowledgeGraphService knowledgeGraphService;
    private final UserService userService;

    /**
     * 获取当前用户的学习知识图谱结构摘要。
     *
     * @param toolContext 工具上下文（含当前用户 ID）
     * @return 图谱摘要：节点/边数量、课程与章节覆盖、已学章节数、名称列表与关系类型分布
     */
    @Tool(name = "getUserKnowledgeGraph", description = "获取当前用户的学习知识图谱结构（节点、关系与覆盖范围摘要），用于对用户知识图谱进行评价分析")
    public Map<String, Object> getUserKnowledgeGraph(ToolContext toolContext) {
        String userId = ToolContextUtil.getUserId(toolContext, userService);
        KnowledgeGraphResponse graph = knowledgeGraphService.getUserGraph(userId);

        List<GraphNodeResponse> nodes = graph.getNodes();
        List<GraphEdgeResponse> edges = graph.getEdges();

        long courseCount = nodes.stream()
                .filter(n -> GraphConstants.LABEL_COURSE.equals(n.getLabel()))
                .count();
        long chapterCount = nodes.stream()
                .filter(n -> GraphConstants.LABEL_CHAPTER.equals(n.getLabel()))
                .count();
        long learnedChapterCount = edges.stream()
                .filter(e -> GraphConstants.EDGE_LEARNED_CHAPTER.equals(e.getType()))
                .count();

        List<String> courseNames = nodes.stream()
                .filter(n -> GraphConstants.LABEL_COURSE.equals(n.getLabel()))
                .map(GraphNodeResponse::getName)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        List<String> chapterNames = nodes.stream()
                .filter(n -> GraphConstants.LABEL_CHAPTER.equals(n.getLabel()))
                .map(GraphNodeResponse::getName)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        Map<String, Long> edgeTypeCounts =
                edges.stream().collect(Collectors.groupingBy(GraphEdgeResponse::getType, Collectors.counting()));

        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("userId", userId);
        summary.put("nodeCount", nodes.size());
        summary.put("edgeCount", edges.size());
        summary.put("courseCount", courseCount);
        summary.put("chapterCount", chapterCount);
        summary.put("learnedChapterCount", learnedChapterCount);
        summary.put("courseNames", courseNames);
        summary.put("chapterNames", chapterNames);
        summary.put("edgeTypeCounts", edgeTypeCounts);
        return summary;
    }
}
