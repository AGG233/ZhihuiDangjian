package com.rauio.smartdangjian.server.graph.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.neo4j.driver.Value;
import org.neo4j.driver.types.Node;
import org.neo4j.driver.types.Relationship;
import org.neo4j.driver.types.Type;
import org.springframework.data.neo4j.core.Neo4jClient;

import com.rauio.smartdangjian.server.graph.pojo.response.GraphNodeResponse;
import com.rauio.smartdangjian.server.graph.pojo.response.KnowledgeGraphResponse;
import com.rauio.smartdangjian.server.graph.pojo.response.PartySeedImportResponse;

/**
 * 党史图谱跨层流程测试：真实 {@link PartySeedDataImporter} + {@link PartyGraphService} 串联，
 * 仅 mock {@link Neo4jClient}（内存桩记录写入的节点/关系，并按查询 Cypher 回放结果）。
 *
 * <p>验证「种子导入 → queryByType → queryRelated」全链路：导入 48 条种子后按类型查询返回
 * 对应 label 节点及其关联子图，关联探索返回 1 跳邻居；空库/未导入时查询返回空 nodes/edges
 * 不报错。写入与查询的 Cypher、绑定参数均被记录并断言。
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PartyGraphFlowTest {

    @Mock
    private Neo4jClient neo4jClient;

    private PartyGraphService partyGraphService;
    private PartySeedDataImporter partySeedDataImporter;
    private Neo4jStub neo4jStub;

    @BeforeEach
    void setUp() {
        reset(neo4jClient);
        neo4jStub = new Neo4jStub();
        partyGraphService = new PartyGraphService(neo4jClient);
        partySeedDataImporter = new PartySeedDataImporter(partyGraphService, neo4jClient);
    }

    // ==================== Neo4jClient 内存桩 ====================

    /** 一次写入记录：Cypher 与绑定参数快照 */
    private record Write(String cypher, Map<String, Object> params) {}

    /**
     * Neo4jClient 内存桩：整条 query→bind→to→run/fetch→all 链按写入内容回放。
     *
     * <p>写入（run）把节点/关系登记到内存注册表；查询（fetch().all()）按 Cypher 中的
     * label 与绑定参数从注册表构造结果行，实现「导入后再查询可见」的语义。
     */
    private final class Neo4jStub {

        private static final Pattern QUERY_LABEL = Pattern.compile("MATCH \\(n:(\\w+)");

        final Neo4jClient.UnboundRunnableSpec spec = mock(Neo4jClient.UnboundRunnableSpec.class);
        final Neo4jClient.OngoingBindSpec bindSpec = mock(Neo4jClient.OngoingBindSpec.class);
        final Neo4jClient.RecordFetchSpec<Map<String, Object>> fetchSpec = mock(Neo4jClient.RecordFetchSpec.class);

        /** 已执行的写入记录（Cypher + 绑定参数），供断言使用 */
        final List<Write> writes = new ArrayList<>();

        /** 当前查询的绑定参数（bind → to 累积） */
        private final Map<String, Object> params = new LinkedHashMap<>();

        private final Deque<Object> pendingValues = new ArrayDeque<>();
        private String currentCypher;

        /** 节点注册表：label → (id → name) */
        private final Map<String, Map<String, String>> nodes = new LinkedHashMap<>();
        /** 关系注册表：节点 key → [(目标 key, 关系类型)]，双向登记 */
        private final Map<String, List<Object[]>> relations = new LinkedHashMap<>();

        private Neo4jStub() {
            lenient().when(neo4jClient.query(anyString())).thenAnswer(inv -> {
                currentCypher = inv.getArgument(0);
                params.clear();
                pendingValues.clear();
                return spec;
            });
            lenient().when(spec.bind(any())).thenAnswer(inv -> {
                pendingValues.addLast(inv.getArgument(0));
                return bindSpec;
            });
            lenient().when(bindSpec.to(anyString())).thenAnswer(inv -> {
                params.put(inv.getArgument(0), pendingValues.removeFirst());
                return spec;
            });
            lenient()
                    .doAnswer(inv -> {
                        writes.add(new Write(currentCypher, new LinkedHashMap<>(params)));
                        registerWrite(currentCypher, params);
                        return null;
                    })
                    .when(spec)
                    .run();
            lenient().when(spec.fetch()).thenReturn(fetchSpec);
            lenient().when(fetchSpec.all()).thenAnswer(inv -> replayRows(currentCypher, params));
        }

        /** 将一次写入登记到内存注册表（按 Cypher 形态区分节点/关系写入） */
        private void registerWrite(String cypher, Map<String, Object> params) {
            if (cypher.contains("MERGE (n:Person")) {
                registerNode("Person", params.get("id"), params.get("name"));
            } else if (cypher.contains("MERGE (n:Event")) {
                registerNode("Event", params.get("id"), params.get("name"));
            } else if (cypher.contains("MERGE (n:Theory")) {
                registerNode("Theory", params.get("id"), params.get("name"));
            } else if (cypher.contains("MERGE (n:Document")) {
                registerNode("Document", params.get("id"), params.get("name"));
            } else if (cypher.contains("PARTICIPATED_IN")) {
                registerRelation(
                        "Person:" + params.get("personId"), "Event:" + params.get("eventId"), "PARTICIPATED_IN");
            } else if (cypher.contains("RELATED_TO")) {
                registerNode("Document", params.get("documentId"), params.get("documentName"));
                registerNode("Theory", params.get("theoryId"), null);
                registerRelation(
                        "Document:" + params.get("documentId"), "Theory:" + params.get("theoryId"), "RELATED_TO");
            }
        }

        private void registerNode(String label, Object id, Object name) {
            if (id == null) {
                return;
            }
            String key = String.valueOf(id);
            nodes.computeIfAbsent(label, k -> new LinkedHashMap<>())
                    .putIfAbsent(key, name == null ? key : String.valueOf(name));
        }

        private void registerRelation(String sourceKey, String targetKey, String type) {
            relations.computeIfAbsent(sourceKey, k -> new ArrayList<>()).add(new Object[] {targetKey, type});
            relations.computeIfAbsent(targetKey, k -> new ArrayList<>()).add(new Object[] {sourceKey, type});
        }

        /** 按查询 Cypher 与绑定参数回放结果行 */
        @SuppressWarnings("unchecked")
        private List<Map<String, Object>> replayRows(String cypher, Map<String, Object> params) {
            Matcher matcher = QUERY_LABEL.matcher(cypher);
            if (!matcher.find()) {
                return List.of();
            }
            String label = matcher.group(1);
            if (cypher.contains("$entityId")) {
                List<String> relationTypes = (List<String>) params.get("relationTypes");
                return relatedRows(label, String.valueOf(params.get("entityId")), relationTypes);
            }
            return byTypeRows(label);
        }

        /** queryByType：返回该 label 下全部节点及其关联子图 */
        private List<Map<String, Object>> byTypeRows(String label) {
            List<Map<String, Object>> rows = new ArrayList<>();
            Map<String, String> labelNodes = nodes.getOrDefault(label, Collections.emptyMap());
            for (Map.Entry<String, String> entry : labelNodes.entrySet()) {
                String sourceKey = label + ":" + entry.getKey();
                List<Object[]> rels = relations.getOrDefault(sourceKey, Collections.emptyList());
                if (rels.isEmpty()) {
                    rows.add(buildRow(sourceKey, null, null));
                } else {
                    for (Object[] rel : rels) {
                        rows.add(buildRow(sourceKey, (String) rel[0], (String) rel[1]));
                    }
                }
            }
            return rows;
        }

        /** queryRelated：返回目标实体的 1 跳邻居（按关系类型过滤） */
        private List<Map<String, Object>> relatedRows(String label, String entityId, List<String> relationTypes) {
            List<Map<String, Object>> rows = new ArrayList<>();
            String sourceKey = label + ":" + entityId;
            List<Object[]> rels = relations.getOrDefault(sourceKey, Collections.emptyList());
            for (Object[] rel : rels) {
                if (relationTypes.contains((String) rel[1])) {
                    rows.add(buildRow(sourceKey, (String) rel[0], (String) rel[1]));
                }
            }
            return rows;
        }

        private Map<String, Object> buildRow(String sourceKey, String targetKey, String relType) {
            Map<String, Object> row = new HashMap<>();
            row.put("n", nodeMock(sourceKey));
            row.put("r", relType == null ? null : mockRelationship(relType));
            row.put("m", targetKey == null ? null : nodeMock(targetKey));
            return row;
        }

        private Node nodeMock(String key) {
            int idx = key.indexOf(':');
            String label = key.substring(0, idx);
            String id = key.substring(idx + 1);
            String name = nodes.getOrDefault(label, Collections.emptyMap()).get(id);
            return mockPartyNode(label, id, name == null ? id : name);
        }
    }

    // ==================== Helper: Neo4j Value / Node / Relationship mocks ====================

    private Value mockStringValue(String value) {
        Value val = mock(Value.class);
        Type type = mock(Type.class);
        when(val.isNull()).thenReturn(false);
        when(type.name()).thenReturn("STRING");
        when(val.type()).thenReturn(type);
        when(val.asString()).thenReturn(value);
        return val;
    }

    private Node mockPartyNode(String label, String id, String name) {
        Value idVal = mockStringValue(id);
        Value nameVal = mockStringValue(name);
        Node node = mock(Node.class);
        when(node.labels()).thenReturn(List.of(label));
        when(node.containsKey("id")).thenReturn(true);
        when(node.get("id")).thenReturn(idVal);
        when(node.containsKey("name")).thenReturn(true);
        when(node.get("name")).thenReturn(nameVal);
        when(node.containsKey("title")).thenReturn(false);
        return node;
    }

    private Relationship mockRelationship(String type) {
        Relationship rel = mock(Relationship.class);
        when(rel.type()).thenReturn(type);
        return rel;
    }

    // ==================== FlowTests: 种子导入 → 查询全链路 ====================

    @Nested
    @DisplayName("FlowTests — 种子导入 → 查询全链路")
    class FlowTests {

        @Test
        @DisplayName("导入 48 条种子后 queryByType(Person) 返回 Person 节点及其关联事件")
        void importThenQueryByTypeReturnsPersonSubgraph() {
            PartySeedImportResponse importResult = partySeedDataImporter.importAll();

            assertThat(importResult.getPersonCount()).isEqualTo(10);
            assertThat(importResult.getEventCount()).isEqualTo(8);
            assertThat(importResult.getTheoryCount()).isEqualTo(6);
            assertThat(importResult.getPersonEventCount()).isEqualTo(16);
            assertThat(importResult.getDocumentTheoryCount()).isEqualTo(8);
            assertThat(importResult.getTotal()).isEqualTo(48);

            KnowledgeGraphResponse graph = partyGraphService.queryByType("Person");

            assertThat(graph).isNotNull();
            // 10 个 Person + 7 个被 Person 参与的事件（long_march 无 Person 参与）
            assertThat(graph.getNodes()).hasSize(17);
            assertThat(graph.getNodes())
                    .filteredOn(n -> "Person".equals(n.getLabel()))
                    .hasSize(10);
            assertThat(graph.getNodes())
                    .filteredOn(n -> "Event".equals(n.getLabel()))
                    .hasSize(7);
            assertThat(graph.getNodes())
                    .extracting(GraphNodeResponse::getId)
                    .contains("Person:mao_zedong", "Person:zhou_enlai", "Event:first_congress", "Event:zunyi_meeting");
            assertThat(graph.getEdges()).hasSize(16);
            assertThat(graph.getEdges()).allSatisfy(e -> assertThat(e.getType()).isEqualTo("PARTICIPATED_IN"));
        }

        @Test
        @DisplayName("导入后 queryRelated 返回目标实体的 1 跳关联")
        void importThenQueryRelatedReturnsNeighbors() {
            partySeedDataImporter.importAll();

            KnowledgeGraphResponse graph =
                    partyGraphService.queryRelated("Person", "mao_zedong", List.of("PARTICIPATED_IN", "INITIATED"));

            assertThat(graph).isNotNull();
            assertThat(graph.getNodes()).hasSize(5);
            assertThat(graph.getNodes())
                    .extracting(GraphNodeResponse::getId)
                    .containsExactlyInAnyOrder(
                            "Person:mao_zedong",
                            "Event:first_congress",
                            "Event:autumn_harvest_uprising",
                            "Event:zunyi_meeting",
                            "Event:founding_of_prc");
            assertThat(graph.getEdges()).hasSize(4);
            assertThat(graph.getEdges()).allSatisfy(e -> assertThat(e.getType()).isEqualTo("PARTICIPATED_IN"));
        }

        @Test
        @DisplayName("导入后 queryRelated 按关系类型过滤返回空结果")
        void queryRelatedFiltersByRelationType() {
            partySeedDataImporter.importAll();

            // 种子数据无 INITIATED 关系 → 空结果不报错
            KnowledgeGraphResponse graph = partyGraphService.queryRelated("Person", "mao_zedong", List.of("INITIATED"));

            assertThat(graph).isNotNull();
            assertThat(graph.getNodes()).isEmpty();
            assertThat(graph.getEdges()).isEmpty();
        }

        @Test
        @DisplayName("导入记录节点/关系 Cypher 与绑定参数")
        void importRecordsCypherAndBoundParams() {
            partySeedDataImporter.importAll();

            // 48 条写入：10 Person + 8 Event + 6 Theory + 16 person_event + 8 document_theory
            assertThat(neo4jStub.writes).hasSize(48);

            // Person 节点 MERGE Cypher 含白名单字段，绑定值完整
            assertThat(neo4jStub.writes).anySatisfy(write -> {
                assertThat(write.cypher()).contains("MERGE (n:Person {id:$id})").contains("n.birthYear = $birthYear");
                assertThat(write.params())
                        .containsEntry("id", "mao_zedong")
                        .containsEntry("name", "毛泽东")
                        .containsEntry("birthYear", 1893)
                        .containsEntry("deathYear", 1976);
            });

            // Person-PARTICIPATED_IN->Event 关系 MERGE 与绑定值
            assertThat(neo4jStub.writes).anySatisfy(write -> {
                assertThat(write.cypher()).contains("MERGE (p)-[:PARTICIPATED_IN]->(e)");
                assertThat(write.params())
                        .containsEntry("personId", "mao_zedong")
                        .containsEntry("eventId", "first_congress");
            });

            // Document-RELATED_TO->Theory 关系 MERGE 顺带补齐 Document 节点属性
            assertThat(neo4jStub.writes).anySatisfy(write -> {
                assertThat(write.cypher())
                        .contains("MERGE (d)-[:RELATED_TO]->(t)")
                        .contains("d.name = $documentName");
                assertThat(write.params())
                        .containsEntry("documentId", "communist_manifesto")
                        .containsEntry("documentName", "共产党宣言")
                        .containsEntry("theoryId", "marxism_leninism");
            });
        }

        @Test
        @DisplayName("导入后查询 Cypher 使用对应 label 与参数绑定")
        void queryCyphersUseLabelAndBindings() {
            partySeedDataImporter.importAll();

            partyGraphService.queryByType("Person");
            partyGraphService.queryRelated("Person", "mao_zedong", List.of("PARTICIPATED_IN"));

            ArgumentCaptor<String> cypherCaptor = ArgumentCaptor.forClass(String.class);
            verify(neo4jClient, times(50)).query(cypherCaptor.capture());
            List<String> cyphers = cypherCaptor.getAllValues();
            // 前 48 条为导入写入，后 2 条为本测试触发的查询
            List<String> queryCyphers = cyphers.subList(48, cyphers.size());

            assertThat(queryCyphers.get(0))
                    .contains("MATCH (n:Person)")
                    .contains("OPTIONAL MATCH (n)-[r]-(m)")
                    .contains("RETURN n, r, m");
            assertThat(queryCyphers.get(1))
                    .contains("MATCH (n:Person {id:$entityId})-[r]-(m)")
                    .contains("WHERE r.type() IN $relationTypes")
                    .contains("RETURN n, r, m");
        }
    }

    // ==================== EmptyDatabaseTests: 空库查询 ====================

    @Nested
    @DisplayName("EmptyDatabaseTests — 空库查询")
    class EmptyDatabaseTests {

        @Test
        @DisplayName("未导入种子时 queryByType 返回空 nodes/edges 不报错")
        void queryByTypeOnEmptyDatabaseReturnsEmptyGraph() {
            KnowledgeGraphResponse graph = partyGraphService.queryByType("Person");

            assertThat(graph).isNotNull();
            assertThat(graph.getNodes()).isEmpty();
            assertThat(graph.getEdges()).isEmpty();
        }

        @Test
        @DisplayName("未导入种子时 queryRelated 返回空 nodes/edges 不报错")
        void queryRelatedOnEmptyDatabaseReturnsEmptyGraph() {
            KnowledgeGraphResponse graph =
                    partyGraphService.queryRelated("Person", "mao_zedong", List.of("PARTICIPATED_IN"));

            assertThat(graph).isNotNull();
            assertThat(graph.getNodes()).isEmpty();
            assertThat(graph.getEdges()).isEmpty();
        }
    }
}
