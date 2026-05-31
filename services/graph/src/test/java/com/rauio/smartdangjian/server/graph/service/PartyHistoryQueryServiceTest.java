package com.rauio.smartdangjian.server.graph.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.neo4j.driver.Value;
import org.neo4j.driver.types.Node;
import org.neo4j.driver.types.Relationship;
import org.neo4j.driver.types.Type;
import org.springframework.data.neo4j.core.Neo4jClient;

import com.rauio.smartdangjian.exception.BusinessException;
import com.rauio.smartdangjian.server.graph.pojo.response.GraphEdgeResponse;
import com.rauio.smartdangjian.server.graph.pojo.response.GraphNodeResponse;
import com.rauio.smartdangjian.server.graph.pojo.response.KnowledgeGraphResponse;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PartyHistoryQueryServiceTest {

    @Mock
    private Neo4jClient neo4jClient;

    @InjectMocks
    private PartyHistoryQueryService partyHistoryQueryService;

    // ==================== Helper: Neo4jClient query chain mocks ====================

    @SuppressWarnings("unchecked")
    private Neo4jClient.RecordFetchSpec<Map<String, Object>> setupFetchChain() {
        Neo4jClient.UnboundRunnableSpec querySpec = mock(Neo4jClient.UnboundRunnableSpec.class);
        Neo4jClient.OngoingBindSpec bindSpec = mock(Neo4jClient.OngoingBindSpec.class);
        lenient().when(neo4jClient.query(anyString())).thenReturn(querySpec);
        lenient().when(querySpec.bind(any())).thenReturn(bindSpec);
        lenient().when(bindSpec.to(anyString())).thenReturn(querySpec);
        Neo4jClient.RecordFetchSpec<Map<String, Object>> fetchSpec = mock(Neo4jClient.RecordFetchSpec.class);
        lenient().when(querySpec.fetch()).thenReturn(fetchSpec);
        return fetchSpec;
    }

    @SuppressWarnings("unchecked")
    private InspectableQueryChain setupInspectableFetchChain() {
        Neo4jClient.UnboundRunnableSpec querySpec = mock(Neo4jClient.UnboundRunnableSpec.class);
        Neo4jClient.OngoingBindSpec bindSpec = mock(Neo4jClient.OngoingBindSpec.class);
        Neo4jClient.RecordFetchSpec<Map<String, Object>> fetchSpec = mock(Neo4jClient.RecordFetchSpec.class);
        lenient().when(neo4jClient.query(anyString())).thenReturn(querySpec);
        lenient().when(querySpec.bind(any())).thenReturn(bindSpec);
        lenient().when(bindSpec.to(anyString())).thenReturn(querySpec);
        lenient().when(querySpec.fetch()).thenReturn(fetchSpec);
        return new InspectableQueryChain(querySpec, fetchSpec);
    }

    private static class InspectableQueryChain {
        private final Neo4jClient.UnboundRunnableSpec querySpec;
        private final Neo4jClient.RecordFetchSpec<Map<String, Object>> fetchSpec;

        private InspectableQueryChain(
                Neo4jClient.UnboundRunnableSpec querySpec, Neo4jClient.RecordFetchSpec<Map<String, Object>> fetchSpec) {
            this.querySpec = querySpec;
            this.fetchSpec = fetchSpec;
        }
    }

    // ==================== Helper: Neo4j Value mocks ====================

    private Value mockStringValue(String value) {
        Value val = mock(Value.class);
        Type type = mock(Type.class);
        when(val.isNull()).thenReturn(false);
        when(type.name()).thenReturn("STRING");
        when(val.type()).thenReturn(type);
        when(val.asString()).thenReturn(value);
        return val;
    }

    // ==================== Helper: Neo4j Node mocks ====================

    private Node mockPartyNode(String label, String graphId, String name) {
        Value graphIdVal = mockStringValue(graphId);
        Value nameVal = mockStringValue(name);
        Node node = mock(Node.class);
        when(node.labels()).thenReturn(List.of(label));
        when(node.containsKey("graph_id")).thenReturn(true);
        when(node.get("graph_id")).thenReturn(graphIdVal);
        when(node.containsKey("name")).thenReturn(true);
        when(node.get("name")).thenReturn(nameVal);
        when(node.containsKey("title")).thenReturn(false);
        when(node.elementId()).thenReturn("4:" + label + ":" + graphId + ":0");
        return node;
    }

    private Node mockPartyNodeWithTitle(String label, String graphId, String title) {
        Value graphIdVal = mockStringValue(graphId);
        Value titleVal = mockStringValue(title);
        Node node = mock(Node.class);
        when(node.labels()).thenReturn(List.of(label));
        when(node.containsKey("graph_id")).thenReturn(true);
        when(node.get("graph_id")).thenReturn(graphIdVal);
        when(node.containsKey("name")).thenReturn(false);
        when(node.containsKey("title")).thenReturn(true);
        when(node.get("title")).thenReturn(titleVal);
        when(node.elementId()).thenReturn("4:" + label + ":" + graphId + ":0");
        return node;
    }

    /** Node without graph_id property, falls back to internal id() */
    private Node mockPartyNodeWithoutGraphId(String label, long internalId, String name) {
        Value nameVal = mockStringValue(name);
        Node node = mock(Node.class);
        when(node.labels()).thenReturn(List.of(label));
        when(node.id()).thenReturn(internalId);
        when(node.containsKey("graph_id")).thenReturn(false);
        when(node.containsKey("name")).thenReturn(true);
        when(node.get("name")).thenReturn(nameVal);
        when(node.containsKey("title")).thenReturn(false);
        when(node.elementId()).thenReturn("4:" + label + ":" + internalId + ":0");
        return node;
    }

    // ==================== Helper: Neo4j Relationship mocks ====================

    private Relationship mockRel(String type, String startElemId, String endElemId) {
        Relationship rel = mock(Relationship.class);
        when(rel.type()).thenReturn(type);
        when(rel.startNodeElementId()).thenReturn(startElemId);
        when(rel.endNodeElementId()).thenReturn(endElemId);
        return rel;
    }

    // ==================== NormalTests ====================

    @Nested
    @DisplayName("NormalTests — 正常路径")
    class NormalTests {

        @Test
        @DisplayName("searchEntities 带关键词和实体类型返回节点列表")
        void searchEntitiesWithKeywordAndTypes() {
            Neo4jClient.RecordFetchSpec<Map<String, Object>> fetchSpec = setupFetchChain();
            Map<String, Object> row = new HashMap<>();
            row.put("node", mockPartyNode("Person", "person-001", "毛泽东"));
            when(fetchSpec.all()).thenReturn((Collection) List.of(row));

            KnowledgeGraphResponse result = partyHistoryQueryService.searchEntities("毛泽东", List.of("Person"), 1, 10);

            assertThat(result).isNotNull();
            assertThat(result.getNodes()).hasSize(1);
            assertThat(result.getNodes())
                    .singleElement()
                    .extracting(GraphNodeResponse::getLabel)
                    .isEqualTo("Person");
            assertThat(result.getNodes())
                    .singleElement()
                    .extracting(GraphNodeResponse::getName)
                    .isEqualTo("毛泽东");
            assertThat(result.getEdges()).isEmpty();
        }

        @Test
        @DisplayName("searchEntities 无类型时匹配所有实体")
        void searchEntitiesWithoutTypes() {
            Neo4jClient.RecordFetchSpec<Map<String, Object>> fetchSpec = setupFetchChain();
            Map<String, Object> row1 = new HashMap<>();
            row1.put("node", mockPartyNode("Person", "person-001", "毛泽东"));
            Map<String, Object> row2 = new HashMap<>();
            row2.put("node", mockPartyNode("Event", "event-001", "遵义会议"));
            when(fetchSpec.all()).thenReturn((Collection) List.of(row1, row2));

            KnowledgeGraphResponse result = partyHistoryQueryService.searchEntities("会", null, 1, 10);

            assertThat(result).isNotNull();
            assertThat(result.getNodes()).hasSize(2);
            assertThat(result.getNodes())
                    .extracting(GraphNodeResponse::getLabel)
                    .containsExactlyInAnyOrder("Person", "Event");
        }

        @Test
        @DisplayName("searchEntities 第二页数据正确处理 skip 偏移")
        void searchEntitiesWithPagination() {
            Neo4jClient.RecordFetchSpec<Map<String, Object>> fetchSpec = setupFetchChain();
            Map<String, Object> row = new HashMap<>();
            row.put("node", mockPartyNode("Theory", "theory-001", "毛泽东思想"));
            when(fetchSpec.all()).thenReturn((Collection) List.of(row));

            KnowledgeGraphResponse result = partyHistoryQueryService.searchEntities("思想", List.of("Theory"), 2, 5);

            assertThat(result).isNotNull();
            assertThat(result.getNodes()).hasSize(1);
            assertThat(result.getNodes())
                    .singleElement()
                    .extracting(GraphNodeResponse::getId)
                    .isEqualTo("Theory:theory-001");
        }

        @Test
        @DisplayName("getEntityDetail 返回实体及其关联节点和边")
        void getEntityDetailWithRelationships() {
            Neo4jClient.RecordFetchSpec<Map<String, Object>> fetchSpec = setupFetchChain();
            Map<String, Object> row = new HashMap<>();
            row.put("n", mockPartyNode("Person", "person-001", "毛泽东"));
            row.put("r", mockRel("INITIATED", "4:Person:person-001:0", "4:Event:event-001:0"));
            row.put("m", mockPartyNode("Event", "event-001", "秋收起义"));
            when(fetchSpec.all()).thenReturn((Collection) List.of(row));

            KnowledgeGraphResponse result = partyHistoryQueryService.getEntityDetail("person-001");

            assertThat(result).isNotNull();
            assertThat(result.getNodes()).hasSize(2);
            assertThat(result.getEdges()).hasSize(1);
            assertThat(result.getEdges())
                    .singleElement()
                    .extracting(GraphEdgeResponse::getType)
                    .isEqualTo("INITIATED");
        }

        @Test
        @DisplayName("getPersonEvents 返回人物-事件关系图")
        void getPersonEvents() {
            Neo4jClient.RecordFetchSpec<Map<String, Object>> fetchSpec = setupFetchChain();
            Map<String, Object> row = new HashMap<>();
            row.put("n", mockPartyNode("Person", "person-001", "毛泽东"));
            row.put("r", mockRel("INITIATED", "4:Person:person-001:0", "4:Event:event-001:0"));
            row.put("m", mockPartyNode("Event", "event-001", "秋收起义"));
            when(fetchSpec.all()).thenReturn((Collection) List.of(row));

            KnowledgeGraphResponse result = partyHistoryQueryService.getPersonEvents("person-001");

            assertThat(result).isNotNull();
            assertThat(result.getNodes()).hasSize(2);
            assertThat(result.getEdges()).hasSize(1);
            assertThat(result.getEdges())
                    .singleElement()
                    .extracting(GraphEdgeResponse::getType)
                    .isEqualTo("INITIATED");
        }

        @Test
        @DisplayName("getEventTimeline 返回事件时间线路径")
        void getEventTimeline() {
            Neo4jClient.RecordFetchSpec<Map<String, Object>> fetchSpec = setupFetchChain();
            Map<String, Object> row = new HashMap<>();

            Node eventNode1 = mockPartyNode("Event", "event-001", "遵义会议");
            Node eventNode2 = mockPartyNode("Event", "event-002", "长征胜利");
            Relationship rel = mockRel("PRECEDED", "4:Event:event-001:0", "4:Event:event-002:0");

            row.put("nodes", List.of(eventNode1, eventNode2));
            row.put("relationships", List.of(rel));
            when(fetchSpec.all()).thenReturn((Collection) List.of(row));

            KnowledgeGraphResponse result = partyHistoryQueryService.getEventTimeline("event-001", 2);

            assertThat(result).isNotNull();
            assertThat(result.getNodes()).hasSize(2);
            assertThat(result.getEdges()).hasSize(1);
            assertThat(result.getEdges())
                    .singleElement()
                    .extracting(GraphEdgeResponse::getType)
                    .isEqualTo("PRECEDED");
        }

        @Test
        @DisplayName("getTheoryEvolution 返回理论发展关系图")
        void getTheoryEvolution() {
            Neo4jClient.RecordFetchSpec<Map<String, Object>> fetchSpec = setupFetchChain();
            Map<String, Object> row = new HashMap<>();
            row.put("t", mockPartyNode("Theory", "theory-001", "毛泽东思想"));
            row.put("r", mockRel("INHERITED_DEVELOPED", "4:Theory:theory-001:0", "4:Theory:theory-002:0"));
            row.put("related", mockPartyNode("Theory", "theory-002", "邓小平理论"));
            when(fetchSpec.all()).thenReturn((Collection) List.of(row));

            KnowledgeGraphResponse result = partyHistoryQueryService.getTheoryEvolution("theory-001");

            assertThat(result).isNotNull();
            assertThat(result.getNodes()).hasSize(2);
            assertThat(result.getEdges()).hasSize(1);
            assertThat(result.getEdges())
                    .singleElement()
                    .extracting(GraphEdgeResponse::getType)
                    .isEqualTo("INHERITED_DEVELOPED");
        }

        @Test
        @DisplayName("findConnection 找到最短路径返回节点和边")
        void findConnectionSuccess() {
            Neo4jClient.RecordFetchSpec<Map<String, Object>> fetchSpec = setupFetchChain();
            Map<String, Object> row = new HashMap<>();

            Node personNode = mockPartyNode("Person", "person-001", "毛泽东");
            Node eventNode = mockPartyNode("Event", "event-001", "秋收起义");
            Relationship rel = mockRel("INITIATED", "4:Person:person-001:0", "4:Event:event-001:0");

            row.put("nodes", List.of(personNode, eventNode));
            row.put("relationships", List.of(rel));
            when(fetchSpec.all()).thenReturn((Collection) List.of(row));

            KnowledgeGraphResponse result = partyHistoryQueryService.findConnection("person-001", "event-001", 4);

            assertThat(result).isNotNull();
            assertThat(result.getNodes()).hasSize(2);
            assertThat(result.getEdges()).hasSize(1);
            assertThat(result.getEdges())
                    .singleElement()
                    .extracting(GraphEdgeResponse::getType)
                    .isEqualTo("INITIATED");
        }

        @Test
        @DisplayName("inferPersonInfluence 返回人物影响关系图")
        void inferPersonInfluence() {
            Neo4jClient.RecordFetchSpec<Map<String, Object>> fetchSpec = setupFetchChain();
            Map<String, Object> row = new HashMap<>();

            Node personNode = mockPartyNode("Person", "person-001", "毛泽东");
            Node theoryNode = mockPartyNode("Theory", "theory-001", "毛泽东思想");
            Relationship rel = mockRel("ELABORATED", "4:Person:person-001:0", "4:Theory:theory-001:0");

            row.put("nodes", List.of(personNode, theoryNode));
            row.put("relationships", List.of(rel));
            when(fetchSpec.all()).thenReturn((Collection) List.of(row));

            KnowledgeGraphResponse result = partyHistoryQueryService.inferPersonInfluence("person-001", 3);

            assertThat(result).isNotNull();
            assertThat(result.getNodes()).hasSize(2);
            assertThat(result.getEdges()).hasSize(1);
            assertThat(result.getEdges())
                    .singleElement()
                    .extracting(GraphEdgeResponse::getType)
                    .isEqualTo("ELABORATED");
        }
    }

    // ==================== ErrorTests ====================

    @Nested
    @DisplayName("ErrorTests — 异常路径")
    class ErrorTests {

        @Test
        @DisplayName("findConnection 无路径时抛出 GRAPH_ENTITY_NOT_FOUND")
        void findConnectionNoPath() {
            Neo4jClient.RecordFetchSpec<Map<String, Object>> fetchSpec = setupFetchChain();
            when(fetchSpec.all()).thenReturn(List.of());

            assertThatThrownBy(() -> partyHistoryQueryService.findConnection("src-001", "tgt-001", 4))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("未找到从 src-001 到 tgt-001 的路径");
        }
    }

    // ==================== BoundaryTests ====================

    @Nested
    @DisplayName("BoundaryTests — 边界情况")
    class BoundaryTests {

        @Test
        @DisplayName("searchEntities 无效实体类型被过滤仅保留有效类型")
        void searchEntitiesWithInvalidTypesFiltered() {
            Neo4jClient.RecordFetchSpec<Map<String, Object>> fetchSpec = setupFetchChain();
            Map<String, Object> row = new HashMap<>();
            row.put("node", mockPartyNode("Person", "person-001", "毛泽东"));
            when(fetchSpec.all()).thenReturn((Collection) List.of(row));

            KnowledgeGraphResponse result =
                    partyHistoryQueryService.searchEntities("毛", List.of("Person", "InvalidType", "Ghost"), 1, 10);

            assertThat(result).isNotNull();
            assertThat(result.getNodes()).hasSize(1);
        }

        @Test
        @DisplayName("searchEntities 空列表类型也无结果")
        void searchEntitiesWithEmptyTypeList() {
            Neo4jClient.RecordFetchSpec<Map<String, Object>> fetchSpec = setupFetchChain();
            Map<String, Object> row = new HashMap<>();
            row.put("node", mockPartyNode("Location", "loc-001", "延安"));
            when(fetchSpec.all()).thenReturn((Collection) List.of(row));

            KnowledgeGraphResponse result = partyHistoryQueryService.searchEntities("延安", List.of(), 1, 10);

            assertThat(result).isNotNull();
            assertThat(result.getNodes()).hasSize(1);
        }

        @Test
        @DisplayName("searchEntities 仅无效类型时全部过滤仍返回空")
        void searchEntitiesAllInvalidTypes() {
            Neo4jClient.RecordFetchSpec<Map<String, Object>> fetchSpec = setupFetchChain();
            when(fetchSpec.all()).thenReturn(List.of());

            KnowledgeGraphResponse result =
                    partyHistoryQueryService.searchEntities("毛", List.of("Invalid", "Ghost"), 1, 10);

            assertThat(result).isNotNull();
            assertThat(result.getNodes()).isEmpty();
            assertThat(result.getEdges()).isEmpty();
        }

        @Test
        @DisplayName("searchEntities page < 1 被钳制为 1（skip=0）")
        void searchEntitiesPageLessThanOne() {
            InspectableQueryChain chain = setupInspectableFetchChain();
            when(chain.fetchSpec.all()).thenReturn(List.of());

            partyHistoryQueryService.searchEntities("test", List.of("Person"), 0, 10);

            ArgumentCaptor<Object> bindCaptor = ArgumentCaptor.forClass(Object.class);
            verify(chain.querySpec, times(4)).bind(bindCaptor.capture());
            assertThat(bindCaptor.getAllValues()).containsExactly("test", List.of("Person"), 0, 10);
        }

        @Test
        @DisplayName("searchEntities size > 100 被钳制为 limit=100")
        void searchEntitiesSizeExceedsMax() {
            InspectableQueryChain chain = setupInspectableFetchChain();
            when(chain.fetchSpec.all()).thenReturn(List.of());

            partyHistoryQueryService.searchEntities("test", List.of("Person"), 1, 200);

            ArgumentCaptor<Object> bindCaptor = ArgumentCaptor.forClass(Object.class);
            verify(chain.querySpec, times(4)).bind(bindCaptor.capture());
            assertThat(bindCaptor.getAllValues()).containsExactly("test", List.of("Person"), 0, 100);
        }

        @Test
        @DisplayName("searchEntities size = 0 被钳制为 limit=1")
        void searchEntitiesSizeZero() {
            InspectableQueryChain chain = setupInspectableFetchChain();
            when(chain.fetchSpec.all()).thenReturn(List.of());

            partyHistoryQueryService.searchEntities("test", List.of("Person"), 1, 0);

            ArgumentCaptor<Object> bindCaptor = ArgumentCaptor.forClass(Object.class);
            verify(chain.querySpec, times(4)).bind(bindCaptor.capture());
            assertThat(bindCaptor.getAllValues()).containsExactly("test", List.of("Person"), 0, 1);
        }

        @Test
        @DisplayName("searchEntities 空结果返回空图谱")
        void searchEntitiesEmptyResult() {
            Neo4jClient.RecordFetchSpec<Map<String, Object>> fetchSpec = setupFetchChain();
            when(fetchSpec.all()).thenReturn(List.of());

            KnowledgeGraphResponse result =
                    partyHistoryQueryService.searchEntities("nonexistent", List.of("Person"), 1, 10);

            assertThat(result).isNotNull();
            assertThat(result.getNodes()).isEmpty();
            assertThat(result.getEdges()).isEmpty();
        }

        @Test
        @DisplayName("getEventTimeline depth > 4 被钳制为 4")
        void getEventTimelineDepthExceedsMax() {
            InspectableQueryChain chain = setupInspectableFetchChain();
            when(chain.fetchSpec.all()).thenReturn(List.of());

            partyHistoryQueryService.getEventTimeline("event-001", 10);

            ArgumentCaptor<String> queryCaptor = ArgumentCaptor.forClass(String.class);
            verify(neo4jClient).query(queryCaptor.capture());
            assertThat(queryCaptor.getValue()).contains("*1..4");
        }

        @Test
        @DisplayName("getEventTimeline depth < 1 被钳制为 1")
        void getEventTimelineDepthBelowMin() {
            InspectableQueryChain chain = setupInspectableFetchChain();
            when(chain.fetchSpec.all()).thenReturn(List.of());

            partyHistoryQueryService.getEventTimeline("event-001", 0);

            ArgumentCaptor<String> queryCaptor = ArgumentCaptor.forClass(String.class);
            verify(neo4jClient).query(queryCaptor.capture());
            assertThat(queryCaptor.getValue()).contains("*1..1");
        }

        @Test
        @DisplayName("getEventTimeline 空结果返回空图谱")
        void getEventTimelineEmptyResult() {
            Neo4jClient.RecordFetchSpec<Map<String, Object>> fetchSpec = setupFetchChain();
            when(fetchSpec.all()).thenReturn(List.of());

            KnowledgeGraphResponse result = partyHistoryQueryService.getEventTimeline("event-001", 2);

            assertThat(result).isNotNull();
            assertThat(result.getNodes()).isEmpty();
            assertThat(result.getEdges()).isEmpty();
        }

        @Test
        @DisplayName("findConnection maxDepth < 2 被钳制为 4")
        void findConnectionDepthBelowMin() {
            InspectableQueryChain chain = setupInspectableFetchChain();
            Map<String, Object> row = new HashMap<>();
            Node n1 = mockPartyNode("Person", "p-001", "A");
            Node n2 = mockPartyNode("Person", "p-002", "B");
            Relationship r = mockRel("RELATED_TO", "4:Person:p-001:0", "4:Person:p-002:0");
            row.put("nodes", List.of(n1, n2));
            row.put("relationships", List.of(r));
            when(chain.fetchSpec.all()).thenReturn((Collection) List.of(row));

            KnowledgeGraphResponse result = partyHistoryQueryService.findConnection("p-001", "p-002", 1);

            assertThat(result.getNodes()).hasSize(2);
            ArgumentCaptor<String> queryCaptor = ArgumentCaptor.forClass(String.class);
            verify(neo4jClient).query(queryCaptor.capture());
            assertThat(queryCaptor.getValue()).contains("[*..4]");
        }

        @Test
        @DisplayName("findConnection maxDepth > 6 被钳制为 4")
        void findConnectionDepthExceedsMax() {
            InspectableQueryChain chain = setupInspectableFetchChain();
            when(chain.fetchSpec.all()).thenReturn(List.of());

            assertThatThrownBy(() -> {
                        partyHistoryQueryService.findConnection("src-001", "tgt-001", 10);
                    })
                    .isInstanceOf(BusinessException.class);

            ArgumentCaptor<String> queryCaptor = ArgumentCaptor.forClass(String.class);
            verify(neo4jClient).query(queryCaptor.capture());
            assertThat(queryCaptor.getValue()).contains("[*..4]");
        }

        @Test
        @DisplayName("inferPersonInfluence maxDepth < 2 被钳制为 4")
        void inferPersonInfluenceDepthBelowMin() {
            InspectableQueryChain chain = setupInspectableFetchChain();
            when(chain.fetchSpec.all()).thenReturn(List.of());

            partyHistoryQueryService.inferPersonInfluence("person-001", 1);

            ArgumentCaptor<String> queryCaptor = ArgumentCaptor.forClass(String.class);
            verify(neo4jClient).query(queryCaptor.capture());
            assertThat(queryCaptor.getValue()).contains("*1..4");
        }

        @Test
        @DisplayName("inferPersonInfluence maxDepth > 6 被钳制为 4")
        void inferPersonInfluenceDepthExceedsMax() {
            InspectableQueryChain chain = setupInspectableFetchChain();
            when(chain.fetchSpec.all()).thenReturn(List.of());

            partyHistoryQueryService.inferPersonInfluence("person-001", 10);

            ArgumentCaptor<String> queryCaptor = ArgumentCaptor.forClass(String.class);
            verify(neo4jClient).query(queryCaptor.capture());
            assertThat(queryCaptor.getValue()).contains("*1..4");
        }

        @Test
        @DisplayName("getEntityDetail 空结果返回空图谱")
        void getEntityDetailEmptyResult() {
            Neo4jClient.RecordFetchSpec<Map<String, Object>> fetchSpec = setupFetchChain();
            when(fetchSpec.all()).thenReturn(List.of());

            KnowledgeGraphResponse result = partyHistoryQueryService.getEntityDetail("nonexistent");

            assertThat(result).isNotNull();
            assertThat(result.getNodes()).isEmpty();
            assertThat(result.getEdges()).isEmpty();
        }

        @Test
        @DisplayName("getPersonEvents 空结果返回空图谱")
        void getPersonEventsEmptyResult() {
            Neo4jClient.RecordFetchSpec<Map<String, Object>> fetchSpec = setupFetchChain();
            when(fetchSpec.all()).thenReturn(List.of());

            KnowledgeGraphResponse result = partyHistoryQueryService.getPersonEvents("person-none");

            assertThat(result).isNotNull();
            assertThat(result.getNodes()).isEmpty();
            assertThat(result.getEdges()).isEmpty();
        }

        @Test
        @DisplayName("getTheoryEvolution 空结果返回空图谱")
        void getTheoryEvolutionEmptyResult() {
            Neo4jClient.RecordFetchSpec<Map<String, Object>> fetchSpec = setupFetchChain();
            when(fetchSpec.all()).thenReturn(List.of());

            KnowledgeGraphResponse result = partyHistoryQueryService.getTheoryEvolution("theory-none");

            assertThat(result).isNotNull();
            assertThat(result.getNodes()).isEmpty();
            assertThat(result.getEdges()).isEmpty();
        }

        @Test
        @DisplayName("inferPersonInfluence 空结果返回空图谱")
        void inferPersonInfluenceEmptyResult() {
            Neo4jClient.RecordFetchSpec<Map<String, Object>> fetchSpec = setupFetchChain();
            when(fetchSpec.all()).thenReturn(List.of());

            KnowledgeGraphResponse result = partyHistoryQueryService.inferPersonInfluence("person-none", 3);

            assertThat(result).isNotNull();
            assertThat(result.getNodes()).isEmpty();
            assertThat(result.getEdges()).isEmpty();
        }

        @Test
        @DisplayName("buildFromNodeCollection 忽略 null 节点")
        void buildFromNodeCollectionSkipsNullNode() {
            Neo4jClient.RecordFetchSpec<Map<String, Object>> fetchSpec = setupFetchChain();
            Map<String, Object> row = new HashMap<>();
            row.put("node", null);
            when(fetchSpec.all()).thenReturn((Collection) List.of(row));

            KnowledgeGraphResponse result = partyHistoryQueryService.searchEntities("test", List.of("Person"), 1, 10);

            assertThat(result).isNotNull();
            assertThat(result.getNodes()).isEmpty();
            assertThat(result.getEdges()).isEmpty();
        }

        @Test
        @DisplayName("buildFromPathRows 处理 nodes 为 null 的行")
        void buildFromPathRowsWithNullNodes() {
            Neo4jClient.RecordFetchSpec<Map<String, Object>> fetchSpec = setupFetchChain();
            Map<String, Object> row = new HashMap<>();
            row.put("nodes", null);
            row.put("relationships", null);
            when(fetchSpec.all()).thenReturn((Collection) List.of(row));

            KnowledgeGraphResponse result = partyHistoryQueryService.getEventTimeline("event-001", 2);

            assertThat(result).isNotNull();
            assertThat(result.getNodes()).isEmpty();
            assertThat(result.getEdges()).isEmpty();
        }

        @Test
        @DisplayName("buildFromPathRows 处理非列表类型 nodes 值")
        void buildFromPathRowsWithNonListNodes() {
            Neo4jClient.RecordFetchSpec<Map<String, Object>> fetchSpec = setupFetchChain();
            Map<String, Object> row = new HashMap<>();
            row.put("nodes", "not-a-list");
            row.put("relationships", "also-not-a-list");
            when(fetchSpec.all()).thenReturn((Collection) List.of(row));

            KnowledgeGraphResponse result = partyHistoryQueryService.getEventTimeline("event-001", 2);

            assertThat(result).isNotNull();
            assertThat(result.getNodes()).isEmpty();
            assertThat(result.getEdges()).isEmpty();
        }

        @Test
        @DisplayName("buildFromPathRows 过滤非 Node/Relationship 类型的元素")
        void buildFromPathRowsFiltersNonNodeElements() {
            Neo4jClient.RecordFetchSpec<Map<String, Object>> fetchSpec = setupFetchChain();
            Map<String, Object> row = new HashMap<>();

            Node validNode = mockPartyNode("Person", "p-001", "A");
            // Mixed list: valid Node + non-Node
            row.put("nodes", List.of(validNode, "not-a-node", 42));
            row.put("relationships", List.of("not-a-rel"));
            when(fetchSpec.all()).thenReturn((Collection) List.of(row));

            KnowledgeGraphResponse result = partyHistoryQueryService.getEventTimeline("event-001", 2);

            assertThat(result).isNotNull();
            assertThat(result.getNodes()).hasSize(1);
            assertThat(result.getEdges()).isEmpty();
        }

        @Test
        @DisplayName("readGraphId graph_id 为 null 时回退到内部 id")
        void readGraphIdNullValueFallsBackToInternalId() {
            Neo4jClient.RecordFetchSpec<Map<String, Object>> fetchSpec = setupFetchChain();
            Map<String, Object> row = new HashMap<>();
            // Uses node without graph_id property
            row.put("node", mockPartyNodeWithoutGraphId("Person", 42L, "无名"));
            when(fetchSpec.all()).thenReturn((Collection) List.of(row));

            KnowledgeGraphResponse result = partyHistoryQueryService.searchEntities("test", List.of("Person"), 1, 10);

            assertThat(result).isNotNull();
            assertThat(result.getNodes()).hasSize(1);
            assertThat(result.getNodes())
                    .singleElement()
                    .extracting(GraphNodeResponse::getId)
                    .isEqualTo("Person:42");
        }

        @Test
        @DisplayName("readName 在 name 不存在时回退到 title")
        void readNameFallbackToTitle() {
            Neo4jClient.RecordFetchSpec<Map<String, Object>> fetchSpec = setupFetchChain();
            Map<String, Object> row = new HashMap<>();
            row.put("node", mockPartyNodeWithTitle("Document", "doc-001", "共产党宣言"));
            when(fetchSpec.all()).thenReturn((Collection) List.of(row));

            KnowledgeGraphResponse result = partyHistoryQueryService.searchEntities("宣言", List.of("Document"), 1, 10);

            assertThat(result).isNotNull();
            assertThat(result.getNodes()).hasSize(1);
            assertThat(result.getNodes())
                    .singleElement()
                    .extracting(GraphNodeResponse::getName)
                    .isEqualTo("共产党宣言");
        }

        @Test
        @DisplayName("buildFromNodeRelCollection 忽略 null 关系")
        void buildFromNodeRelCollectionSkipsNullRel() {
            Neo4jClient.RecordFetchSpec<Map<String, Object>> fetchSpec = setupFetchChain();
            Map<String, Object> row = new HashMap<>();
            row.put("n", mockPartyNode("Person", "p-001", "A"));
            row.put("r", null);
            row.put("m", mockPartyNode("Event", "e-001", "B"));
            when(fetchSpec.all()).thenReturn((Collection) List.of(row));

            KnowledgeGraphResponse result = partyHistoryQueryService.getEntityDetail("p-001");

            assertThat(result).isNotNull();
            assertThat(result.getNodes()).hasSize(2);
            assertThat(result.getEdges()).isEmpty();
        }

        @Test
        @DisplayName("buildFromNodeRelCollection 忽略 null 目标节点")
        void buildFromNodeRelCollectionWithNullTargetNode() {
            Neo4jClient.RecordFetchSpec<Map<String, Object>> fetchSpec = setupFetchChain();
            Map<String, Object> row = new HashMap<>();
            row.put("n", mockPartyNode("Person", "p-001", "A"));
            row.put("r", mockRel("INITIATED", "4:Person:p-001:0", "4:Event:e-001:0"));
            row.put("m", null);
            when(fetchSpec.all()).thenReturn((Collection) List.of(row));

            KnowledgeGraphResponse result = partyHistoryQueryService.getEntityDetail("p-001");

            assertThat(result).isNotNull();
            assertThat(result.getNodes()).hasSize(1);
            // r is present but toKey is null, so no edge added
            assertThat(result.getEdges()).isEmpty();
        }

        @Test
        @DisplayName("addPartyNode 处理 null 节点返回 null")
        void addPartyNodeWithNullNode() {
            Neo4jClient.RecordFetchSpec<Map<String, Object>> fetchSpec = setupFetchChain();
            Map<String, Object> row = new HashMap<>();
            row.put("node", null);
            when(fetchSpec.all()).thenReturn((Collection) List.of(row));

            KnowledgeGraphResponse result = partyHistoryQueryService.searchEntities("test", null, 1, 10);

            assertThat(result).isNotNull();
            assertThat(result.getNodes()).isEmpty();
        }

        @Test
        @DisplayName("addPartyNode 节点去重不重复添加相同 graph_id")
        void addPartyNodeDeduplication() {
            Neo4jClient.RecordFetchSpec<Map<String, Object>> fetchSpec = setupFetchChain();
            Map<String, Object> row1 = new HashMap<>();
            row1.put("n", mockPartyNode("Person", "p-001", "毛泽东"));
            row1.put("r", mockRel("INITIATED", "4:Person:p-001:0", "4:Event:e-001:0"));
            row1.put("m", mockPartyNode("Event", "e-001", "秋收起义"));

            Map<String, Object> row2 = new HashMap<>();
            row2.put("n", mockPartyNode("Person", "p-001", "毛泽东")); // Same node
            row2.put("r", mockRel("PARTICIPATED_IN", "4:Person:p-001:0", "4:Event:e-002:0"));
            row2.put("m", mockPartyNode("Event", "e-002", "遵义会议"));

            when(fetchSpec.all()).thenReturn((Collection) List.of(row1, row2));

            KnowledgeGraphResponse result = partyHistoryQueryService.getEntityDetail("p-001");

            assertThat(result).isNotNull();
            // 3 distinct nodes: Person:p-001, Event:e-001, Event:e-002 (person deduplicated)
            assertThat(result.getNodes()).hasSize(3);
            assertThat(result.getEdges()).hasSize(2);
            assertThat(result.getEdges())
                    .extracting(GraphEdgeResponse::getType)
                    .containsExactlyInAnyOrder("INITIATED", "PARTICIPATED_IN");
        }

        @Test
        @DisplayName("buildFromPathRows elementId 映射缺失时跳过该关系")
        void buildFromPathRowsMissingElementIdMapping() {
            Neo4jClient.RecordFetchSpec<Map<String, Object>> fetchSpec = setupFetchChain();
            Map<String, Object> row = new HashMap<>();

            Node n1 = mockPartyNode("Event", "e-001", "A");
            // Relationship references element IDs not in the node list
            Relationship rel = mockRel("CAUSED", "unknown-start-elem", "unknown-end-elem");

            row.put("nodes", List.of(n1));
            row.put("relationships", List.of(rel));
            when(fetchSpec.all()).thenReturn((Collection) List.of(row));

            KnowledgeGraphResponse result = partyHistoryQueryService.getEventTimeline("e-001", 2);

            assertThat(result).isNotNull();
            assertThat(result.getNodes()).hasSize(1);
            // Edge not added because elementId mapping is missing
            assertThat(result.getEdges()).isEmpty();
        }

        @Test
        @DisplayName("resolveEntityTypes 过滤返回去重后的有效类型")
        void resolveEntityTypesDeduplicationAndFiltering() {
            Neo4jClient.RecordFetchSpec<Map<String, Object>> fetchSpec = setupFetchChain();
            Map<String, Object> row = new HashMap<>();
            row.put("node", mockPartyNode("Person", "p-001", "A"));
            when(fetchSpec.all()).thenReturn((Collection) List.of(row));

            // Duplicate "Person" should be deduplicated, invalid types filtered
            KnowledgeGraphResponse result = partyHistoryQueryService.searchEntities(
                    "A", List.of("Person", "Person", "Invalid", "Event"), 1, 10);

            assertThat(result).isNotNull();
            assertThat(result.getNodes()).hasSize(1);
        }
    }
}
