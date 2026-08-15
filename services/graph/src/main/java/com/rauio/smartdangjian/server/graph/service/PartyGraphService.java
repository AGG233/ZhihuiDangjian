package com.rauio.smartdangjian.server.graph.service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.neo4j.driver.Value;
import org.neo4j.driver.types.Node;
import org.neo4j.driver.types.Relationship;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.stereotype.Service;

import com.rauio.smartdangjian.exception.BusinessException;
import com.rauio.smartdangjian.server.graph.constants.GraphErrorConstants;
import com.rauio.smartdangjian.server.graph.pojo.response.GraphEdgeResponse;
import com.rauio.smartdangjian.server.graph.pojo.response.GraphNodeResponse;
import com.rauio.smartdangjian.server.graph.pojo.response.KnowledgeGraphResponse;

import lombok.RequiredArgsConstructor;

/**
 * 党史实体知识图谱服务（5 类实体节点 + 4 类关系）。
 *
 * <p>节点：Person、Event、Theory、Document、Location；关系：PARTICIPATED_IN、
 * INITIATED、EVOLVED_FROM、RELATED_TO。全部写入走 MERGE 保证幂等。
 */
@Service
@RequiredArgsConstructor
public class PartyGraphService {

    /**
     * 党史实体类型 → 除 id 外的属性字段。label 与字段名均为白名单常量，
     * 不来自用户输入，用于安全生成不同实体的 Cypher（按 type 分 Cypher）。
     */
    private static final Map<String, List<String>> PARTY_ENTITY_FIELDS = Map.of(
            "Person", List.of("name", "description", "birthYear", "deathYear"),
            "Event", List.of("name", "description", "date", "location"),
            "Theory", List.of("name", "description", "era"),
            "Document", List.of("name", "description", "publisher", "date"),
            "Location", List.of("name", "description", "type"));

    private final Neo4jClient neo4jClient;

    /**
     * MERGE 幂等写入党史实体节点。
     *
     * @param type 实体类型（Person/Event/Theory/Document/Location）
     * @param props 实体属性，必须含 id；其余字段对应 {@link #PARTY_ENTITY_FIELDS}
     */
    public void upsertPartyEntity(String type, Map<String, Object> props) {
        List<String> fields = PARTY_ENTITY_FIELDS.get(type);
        if (fields == null) {
            throw new BusinessException(GraphErrorConstants.PARTY_ENTITY_TYPE_INVALID, "不支持的党史实体类型: " + type);
        }
        Object id = props.get("id");
        if (id == null) {
            throw new BusinessException(GraphErrorConstants.PARTY_ENTITY_ID_REQUIRED, "党史实体 ID 不能为空");
        }

        String cypher = upsertCypher(type, fields);
        Neo4jClient.RunnableSpec spec = neo4jClient.query(cypher).bind(id).to("id");
        for (String field : fields) {
            spec = spec.bind(props.get(field)).to(field);
        }
        spec.run();
    }

    /**
     * 按类型查询党史实体及其关联子图（节点 + 边）。
     *
     * @param type 实体类型
     * @return 图谱视图对象
     */
    public KnowledgeGraphResponse queryByType(String type) {
        String label = requirePartyLabel(type);
        String cypher = "MATCH (n:" + label + ")\n" + "OPTIONAL MATCH (n)-[r]-(m)\n" + "RETURN n, r, m";

        List<Map<String, Object>> rows =
                (List<Map<String, Object>>) neo4jClient.query(cypher).fetch().all();

        return buildGraph(rows);
    }

    /**
     * 查询实体的 1 跳邻居，按关系类型过滤。
     *
     * @param entityType 实体类型
     * @param entityId 实体 ID
     * @param relationTypes 关系类型列表，为空则无结果
     * @return 图谱视图对象
     */
    public KnowledgeGraphResponse queryRelated(String entityType, String entityId, List<String> relationTypes) {
        String label = requirePartyLabel(entityType);
        String cypher = "MATCH (n:" + label + " {id:$entityId})-[r]-(m)\n"
                + "WHERE type(r) IN $relationTypes\n"
                + "RETURN n, r, m";

        List<Map<String, Object>> rows = (List<Map<String, Object>>) neo4jClient
                .query(cypher)
                .bind(entityId)
                .to("entityId")
                .bind(relationTypes)
                .to("relationTypes")
                .fetch()
                .all();

        return buildGraph(rows);
    }

    /**
     * 校验实体类型并返回 Neo4j label（白名单校验后安全用于 Cypher 拼接）。
     *
     * @param type 实体类型
     * @return 校验后的 label
     */
    private String requirePartyLabel(String type) {
        if (!PARTY_ENTITY_FIELDS.containsKey(type)) {
            throw new BusinessException(GraphErrorConstants.PARTY_ENTITY_TYPE_INVALID, "不支持的党史实体类型: " + type);
        }
        return type;
    }

    /**
     * 生成指定实体的 MERGE + SET Cypher（幂等：重复调用不产生重复节点）。
     *
     * @param type 实体类型
     * @param fields 属性字段白名单
     * @return Cypher 语句
     */
    private String upsertCypher(String type, List<String> fields) {
        StringBuilder cypher = new StringBuilder("MERGE (n:").append(type).append(" {id:$id}) SET ");
        for (int i = 0; i < fields.size(); i++) {
            if (i > 0) {
                cypher.append(", ");
            }
            String field = fields.get(i);
            cypher.append("n.").append(field).append(" = $").append(field);
        }
        return cypher.toString();
    }

    /**
     * 将 Neo4j 查询结果转换为前端图谱结构。
     *
     * @param rows 查询结果行
     * @return 图谱视图对象
     */
    private KnowledgeGraphResponse buildGraph(List<Map<String, Object>> rows) {
        Map<String, GraphNodeResponse> nodeMap = new LinkedHashMap<>();
        Set<String> edgeKeys = new LinkedHashSet<>();
        List<GraphEdgeResponse> edges = new ArrayList<>();

        for (Map<String, Object> row : rows) {
            Node sourceNode = asNode(row.get("n"));
            Node targetNode = asNode(row.get("m"));
            Relationship relationship = asRelationship(row.get("r"));

            String sourceKey = addNode(nodeMap, sourceNode);
            String targetKey = addNode(nodeMap, targetNode);

            if (relationship != null && sourceKey != null && targetKey != null) {
                addEdge(edgeKeys, edges, sourceKey, targetKey, relationship.type());
            }
        }

        return KnowledgeGraphResponse.builder()
                .nodes(new ArrayList<>(nodeMap.values()))
                .edges(edges)
                .build();
    }

    /**
     * 将对象安全转换为 Neo4j 节点。
     *
     * @param value 原始对象
     * @return 节点对象
     */
    private Node asNode(Object value) {
        if (value instanceof Node node) {
            return node;
        }
        return null;
    }

    /**
     * 将对象安全转换为 Neo4j 关系。
     *
     * @param value 原始对象
     * @return 关系对象
     */
    private Relationship asRelationship(Object value) {
        if (value instanceof Relationship relationship) {
            return relationship;
        }
        return null;
    }

    /**
     * 向图谱节点映射中注册节点。
     *
     * @param nodeMap 节点映射
     * @param node Neo4j 节点
     * @return 节点唯一键
     */
    private String addNode(Map<String, GraphNodeResponse> nodeMap, Node node) {
        if (node == null) {
            return null;
        }
        String label =
                node.labels().iterator().hasNext() ? node.labels().iterator().next() : "Node";
        String id = readId(node);
        String key = label + ":" + id;
        if (!nodeMap.containsKey(key)) {
            nodeMap.put(
                    key,
                    GraphNodeResponse.builder()
                            .id(key)
                            .label(label)
                            .name(readName(node, id))
                            .build());
        }
        return key;
    }

    /**
     * 读取节点业务 ID。
     *
     * @param node Neo4j 节点
     * @return 节点 ID
     */
    private String readId(Node node) {
        if (node.containsKey("id") && !node.get("id").isNull()) {
            Value value = node.get("id");
            if ("INTEGER".equals(value.type().name())) {
                return String.valueOf(value.asLong());
            }
            return value.asString();
        }
        return String.valueOf(node.id());
    }

    /**
     * 读取节点展示名称。
     *
     * @param node Neo4j 节点
     * @param fallback 回退名称
     * @return 节点名称
     */
    private String readName(Node node, String fallback) {
        if (node.containsKey("name") && !node.get("name").isNull()) {
            return node.get("name").asString();
        }
        if (node.containsKey("title") && !node.get("title").isNull()) {
            return node.get("title").asString();
        }
        return fallback;
    }

    /**
     * 去重并追加图谱边。
     *
     * @param edgeKeys 边去重键集合
     * @param edges 边列表
     * @param source 源节点键
     * @param target 目标节点键
     * @param type 边类型
     */
    private void addEdge(
            Set<String> edgeKeys, List<GraphEdgeResponse> edges, String source, String target, String type) {
        String key = source + "|" + type + "|" + target;
        if (edgeKeys.add(key)) {
            edges.add(GraphEdgeResponse.builder()
                    .source(source)
                    .target(target)
                    .type(type)
                    .build());
        }
    }
}
