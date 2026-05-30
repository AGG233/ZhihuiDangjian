package com.rauio.smartdangjian.server.graph.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class PartyHistoryQueryService {

    private final Neo4jClient neo4jClient;

    private static final Set<String> VALID_ENTITY_TYPES = Set.of("Person", "Event", "Location", "Theory", "Document");
    private static final Set<String> INFLUENCE_REL_TYPES = Set.of(
            "INITIATED", "PARTICIPATED_IN", "PROMOTED", "ELABORATED", "INHERITED_DEVELOPED", "AUTHORED", "RELATED_TO");

    // ==================== 公共查询 API ====================

    public KnowledgeGraphResponse searchEntities(String keyword, List<String> entityTypes, int page, int size) {
        List<String> types = resolveEntityTypes(entityTypes);
        int skip = (Math.max(page, 1) - 1) * Math.min(Math.max(size, 1), 100);
        int limit = Math.min(Math.max(size, 1), 100);

        String cypher = "CALL db.index.fulltext.queryNodes('entity_search', $keyword) YIELD node, score "
                + "WHERE size($types) = 0 OR any(label IN labels(node) WHERE label IN $types) "
                + "RETURN node "
                + "ORDER BY score DESC "
                + "SKIP $skip LIMIT $limit";

        var rows = (Collection<Map<String, Object>>) neo4jClient
                .query(cypher)
                .bind(keyword)
                .to("keyword")
                .bind(types)
                .to("types")
                .bind(skip)
                .to("skip")
                .bind(limit)
                .to("limit")
                .fetch()
                .all();

        return buildFromNodeCollection(rows, "node");
    }

    public KnowledgeGraphResponse getEntityDetail(String graphId) {
        String cypher = "MATCH (n) WHERE n.graph_id = $graphId " + "OPTIONAL MATCH (n)-[r]-(m) " + "RETURN n, r, m";

        var rows = (Collection<Map<String, Object>>)
                neo4jClient.query(cypher).bind(graphId).to("graphId").fetch().all();

        return buildFromNodeRelCollection(rows, "n", "r", "m");
    }

    public KnowledgeGraphResponse getPersonEvents(String personGraphId) {
        String cypher = "MATCH (n:Person) WHERE n.graph_id = $graphId "
                + "MATCH (n)-[r:INITIATED|PARTICIPATED_IN|PROMOTED]-(m:Event) "
                + "RETURN n, r, m";

        var rows = (Collection<Map<String, Object>>) neo4jClient
                .query(cypher)
                .bind(personGraphId)
                .to("graphId")
                .fetch()
                .all();

        return buildFromNodeRelCollection(rows, "n", "r", "m");
    }

    public KnowledgeGraphResponse getEventTimeline(String eventGraphId, int depth) {
        int d = Math.clamp(depth, 1, 4);

        String cypher = "MATCH (e:Event) WHERE e.graph_id = $graphId "
                + "MATCH p = (e)-[*1.." + d + "]-(related:Event) "
                + "WHERE all(rel IN relationships(p) WHERE type(rel) IN ['CAUSED', 'PRECEDED', 'RELATED_TO', 'LED_TO']) "
                + "RETURN nodes(p) AS nodes, relationships(p) AS relationships";

        var rows = (Collection<Map<String, Object>>) neo4jClient
                .query(cypher)
                .bind(eventGraphId)
                .to("graphId")
                .fetch()
                .all();

        return buildFromPathRows(rows);
    }

    public KnowledgeGraphResponse getTheoryEvolution(String theoryGraphId) {
        String cypher = "MATCH (t:Theory) WHERE t.graph_id = $graphId "
                + "OPTIONAL MATCH (t)-[r:ELABORATED|INHERITED_DEVELOPED|RELATED_TO]-(related:Theory) "
                + "RETURN t, r, related";

        var rows = (Collection<Map<String, Object>>) neo4jClient
                .query(cypher)
                .bind(theoryGraphId)
                .to("graphId")
                .fetch()
                .all();

        return buildFromNodeRelCollection(rows, "t", "r", "related");
    }

    // ==================== Phase 3: 推理查询 ====================

    public KnowledgeGraphResponse findConnection(String sourceGraphId, String targetGraphId, int maxDepth) {
        if (maxDepth < 2 || maxDepth > 6) {
            maxDepth = 4;
        }

        String cypher = "MATCH (a), (b) WHERE a.graph_id = $sourceId AND b.graph_id = $targetId "
                + "MATCH p = shortestPath((a)-[*.." + maxDepth + "]-(b)) "
                + "RETURN nodes(p) AS nodes, relationships(p) AS relationships LIMIT 1";

        var rows = (Collection<Map<String, Object>>) neo4jClient
                .query(cypher)
                .bind(sourceGraphId)
                .to("sourceId")
                .bind(targetGraphId)
                .to("targetId")
                .fetch()
                .all();

        KnowledgeGraphResponse result = buildFromPathRows(rows);
        if (result.getNodes().isEmpty()) {
            throw new BusinessException(
                    GraphErrorConstants.GRAPH_ENTITY_NOT_FOUND,
                    "未找到从 " + sourceGraphId + " 到 " + targetGraphId + " 的路径");
        }
        return result;
    }

    public KnowledgeGraphResponse inferPersonInfluence(String personGraphId, int maxDepth) {
        if (maxDepth < 2 || maxDepth > 6) {
            maxDepth = 4;
        }

        String relTypes = String.join("|", INFLUENCE_REL_TYPES);
        String cypher = "MATCH (p:Person) WHERE p.graph_id = $personId "
                + "MATCH p = (p)-[:" + relTypes + "*1.." + maxDepth + "]-(related) "
                + "RETURN nodes(p) AS nodes, relationships(p) AS relationships";

        var rows = (Collection<Map<String, Object>>) neo4jClient
                .query(cypher)
                .bind(personGraphId)
                .to("personId")
                .fetch()
                .all();

        return buildFromPathRows(rows);
    }

    // ==================== 图谱构建辅助方法 ====================

    private KnowledgeGraphResponse buildFromNodeCollection(Collection<Map<String, Object>> rows, String nodeKey) {
        Map<String, GraphNodeResponse> nodeMap = new LinkedHashMap<>();
        for (Map<String, Object> row : rows) {
            Node node = asNode(row.get(nodeKey));
            if (node != null) {
                addPartyNode(nodeMap, node);
            }
        }
        return KnowledgeGraphResponse.builder()
                .nodes(new ArrayList<>(nodeMap.values()))
                .edges(Collections.emptyList())
                .build();
    }

    private KnowledgeGraphResponse buildFromNodeRelCollection(
            Collection<Map<String, Object>> rows, String fromKey, String relKey, String toKey) {
        Map<String, GraphNodeResponse> nodeMap = new LinkedHashMap<>();
        Set<String> edgeKeys = new LinkedHashSet<>();
        List<GraphEdgeResponse> edges = new ArrayList<>();

        for (Map<String, Object> row : rows) {
            Node fromNode = asNode(row.get(fromKey));
            Relationship rel = asRelationship(row.get(relKey));
            Node toNode = asNode(row.get(toKey));

            String fromKey2 = addPartyNode(nodeMap, fromNode);
            String toKey2 = addPartyNode(nodeMap, toNode);

            if (rel != null && fromKey2 != null && toKey2 != null) {
                addEdge(edgeKeys, edges, fromKey2, toKey2, rel.type());
            }
        }
        return KnowledgeGraphResponse.builder()
                .nodes(new ArrayList<>(nodeMap.values()))
                .edges(edges)
                .build();
    }

    private KnowledgeGraphResponse buildFromPathRows(Collection<Map<String, Object>> rows) {
        Map<String, GraphNodeResponse> nodeMap = new LinkedHashMap<>();
        Map<String, String> elementIdToKey = new LinkedHashMap<>();
        Set<String> edgeKeys = new LinkedHashSet<>();
        List<GraphEdgeResponse> edges = new ArrayList<>();

        for (Map<String, Object> row : rows) {
            List<Node> nodes = asNodeList(row.get("nodes"));
            List<Relationship> rels = asRelationshipList(row.get("relationships"));

            for (Node node : nodes) {
                String key = addPartyNode(nodeMap, node);
                elementIdToKey.put(node.elementId(), key);
            }
            for (Relationship rel : rels) {
                String sourceKey = elementIdToKey.get(rel.startNodeElementId());
                String targetKey = elementIdToKey.get(rel.endNodeElementId());
                if (sourceKey != null && targetKey != null) {
                    addEdge(edgeKeys, edges, sourceKey, targetKey, rel.type());
                }
            }
        }
        return KnowledgeGraphResponse.builder()
                .nodes(new ArrayList<>(nodeMap.values()))
                .edges(edges)
                .build();
    }

    private String addPartyNode(Map<String, GraphNodeResponse> nodeMap, Node node) {
        if (node == null) return null;
        String label =
                node.labels().iterator().hasNext() ? node.labels().iterator().next() : "Entity";
        String graphId = readGraphId(node);
        String key = label + ":" + graphId;
        if (!nodeMap.containsKey(key)) {
            nodeMap.put(
                    key,
                    GraphNodeResponse.builder()
                            .id(key)
                            .label(label)
                            .name(readName(node, graphId))
                            .build());
        }
        return key;
    }

    private String readGraphId(Node node) {
        if (node.containsKey("graph_id") && !node.get("graph_id").isNull()) {
            return node.get("graph_id").asString();
        }
        return String.valueOf(node.id());
    }

    private String readName(Node node, String fallback) {
        if (node.containsKey("name") && !node.get("name").isNull()) {
            return node.get("name").asString();
        }
        if (node.containsKey("title") && !node.get("title").isNull()) {
            return node.get("title").asString();
        }
        return fallback;
    }

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

    @SuppressWarnings("unchecked")
    private List<Node> asNodeList(Object value) {
        if (value == null) return Collections.emptyList();
        if (value instanceof List<?> list) {
            return list.stream()
                    .filter(Node.class::isInstance)
                    .map(Node.class::cast)
                    .toList();
        }
        return Collections.emptyList();
    }

    @SuppressWarnings("unchecked")
    private List<Relationship> asRelationshipList(Object value) {
        if (value == null) return Collections.emptyList();
        if (value instanceof List<?> list) {
            return list.stream()
                    .filter(Relationship.class::isInstance)
                    .map(Relationship.class::cast)
                    .toList();
        }
        return Collections.emptyList();
    }

    private Node asNode(Object value) {
        if (value instanceof Node node) return node;
        return null;
    }

    private Relationship asRelationship(Object value) {
        if (value instanceof Relationship rel) return rel;
        return null;
    }

    private List<String> resolveEntityTypes(List<String> types) {
        if (types == null || types.isEmpty()) {
            return Collections.emptyList();
        }
        return types.stream().filter(VALID_ENTITY_TYPES::contains).distinct().toList();
    }
}
