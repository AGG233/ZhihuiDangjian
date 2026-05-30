package com.rauio.smartdangjian.server.graph.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.stereotype.Service;

import com.rauio.smartdangjian.exception.BusinessException;
import com.rauio.smartdangjian.server.graph.constants.GraphErrorConstants;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PartyHistoryGraphService {

    private final Neo4jClient neo4jClient;

    private static final String REL_TYPE_PATTERN = "[A-Z_]+";
    private static final String LABEL_PATTERN = "[A-Z][A-Za-z]*";

    // ==================== 实体合并 ====================

    public void mergePerson(String graphId, String name, Map<String, Object> extraProps) {
        mergeEntity("Person", graphId, name, extraProps);
    }

    public void mergeEvent(String graphId, String name, Map<String, Object> extraProps) {
        mergeEntity("Event", graphId, name, extraProps);
    }

    public void mergeLocation(String graphId, String name, Map<String, Object> extraProps) {
        mergeEntity("Location", graphId, name, extraProps);
    }

    public void mergeTheory(String graphId, String name, Map<String, Object> extraProps) {
        mergeEntity("Theory", graphId, name, extraProps);
    }

    public void mergeDocument(String graphId, String title, Map<String, Object> extraProps) {
        Map<String, Object> props = new HashMap<>();
        props.put("graph_id", resolveGraphId(graphId));
        props.put("title", title);
        if (extraProps != null) {
            props.putAll(extraProps);
        }

        String cypher = "MERGE (n:Document {graph_id: $graphId}) SET n += $props";
        neo4jClient
                .query(cypher)
                .bind(props.get("graph_id"))
                .to("graphId")
                .bind(props)
                .to("props")
                .run();
    }

    // ==================== 关系添加 ====================

    public void addRelationship(
            String sourceGraphId, String targetGraphId, String relType, Map<String, Object> properties) {
        validateRelType(relType);

        Map<String, Object> props = properties != null ? new HashMap<>(properties) : new HashMap<>();

        String cypher = "MATCH (a) WHERE a.graph_id = $sourceId "
                + "MATCH (b) WHERE b.graph_id = $targetId "
                + "MERGE (a)-[r:" + relType + "]->(b) "
                + "SET r += $props";

        neo4jClient
                .query(cypher)
                .bind(sourceGraphId)
                .to("sourceId")
                .bind(targetGraphId)
                .to("targetId")
                .bind(props)
                .to("props")
                .run();
    }

    // ==================== 批量导入 ====================

    public void batchMergeEntities(String label, List<Map<String, Object>> entities) {
        validateLabel(label);

        // 确保每条记录有 graph_id
        for (Map<String, Object> entity : entities) {
            if (!entity.containsKey("graph_id") || entity.get("graph_id") == null) {
                entity.put("graph_id", UUID.randomUUID().toString());
            }
        }

        String cypher = "UNWIND $rows AS row MERGE (n:" + label + " {graph_id: row.graph_id}) SET n += row";
        neo4jClient.query(cypher).bind(entities).to("rows").run();
    }

    public void batchAddRelationships(List<Map<String, Object>> relationships) {
        // 按 relType 分组，每组用一次 UNWIND
        Map<String, List<Map<String, Object>>> grouped =
                relationships.stream().collect(Collectors.groupingBy(r -> (String) r.get("relType")));

        for (var entry : grouped.entrySet()) {
            String relType = entry.getKey();
            validateRelType(relType);

            List<Map<String, Object>> rows = entry.getValue();

            String cypher = "UNWIND $rows AS row "
                    + "MATCH (a) WHERE a.graph_id = row.sourceId "
                    + "MATCH (b) WHERE b.graph_id = row.targetId "
                    + "MERGE (a)-[r:" + relType + "]->(b) "
                    + "SET r += row.properties";

            neo4jClient.query(cypher).bind(rows).to("rows").run();
        }
    }

    // ==================== 删除 ====================

    public void deleteEntity(String graphId) {
        String cypher = "MATCH (n {graph_id: $graphId}) DETACH DELETE n";
        neo4jClient.query(cypher).bind(graphId).to("graphId").run();
    }

    // ==================== 内部方法 ====================

    private void mergeEntity(String label, String graphId, String name, Map<String, Object> extraProps) {
        Map<String, Object> props = new HashMap<>();
        props.put("graph_id", resolveGraphId(graphId));
        props.put("name", name);
        if (extraProps != null) {
            props.putAll(extraProps);
        }

        String cypher = "MERGE (n:" + label + " {graph_id: $graphId}) SET n += $props";
        neo4jClient
                .query(cypher)
                .bind(props.get("graph_id"))
                .to("graphId")
                .bind(props)
                .to("props")
                .run();
    }

    private String resolveGraphId(String graphId) {
        if (graphId != null && !graphId.isBlank()) {
            return graphId;
        }
        return UUID.randomUUID().toString();
    }

    private void validateRelType(String relType) {
        if (relType == null || !relType.matches(REL_TYPE_PATTERN)) {
            throw new BusinessException(GraphErrorConstants.GRAPH_INVALID_RELATIONSHIP, "无效的关系类型: " + relType);
        }
    }

    private void validateLabel(String label) {
        if (label == null || !label.matches(LABEL_PATTERN)) {
            throw new BusinessException(GraphErrorConstants.GRAPH_INVALID_RELATIONSHIP, "无效的节点标签: " + label);
        }
    }
}
