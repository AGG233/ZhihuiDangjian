package com.rauio.smartdangjian.server.graph.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
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
import com.rauio.smartdangjian.server.graph.constants.GraphErrorConstants;
import com.rauio.smartdangjian.server.graph.pojo.response.GraphEdgeResponse;
import com.rauio.smartdangjian.server.graph.pojo.response.GraphNodeResponse;
import com.rauio.smartdangjian.server.graph.pojo.response.KnowledgeGraphResponse;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PartyGraphServiceTest {

    @Mock
    private Neo4jClient neo4jClient;

    @InjectMocks
    private PartyGraphService partyGraphService;

    /** Neo4jClient 查询链 mock：query → bind → to → (fetch → all) */
    private final class Chain {
        final Neo4jClient.UnboundRunnableSpec spec = mock(Neo4jClient.UnboundRunnableSpec.class);
        final Neo4jClient.OngoingBindSpec bindSpec = mock(Neo4jClient.OngoingBindSpec.class);
        final Neo4jClient.RecordFetchSpec<Map<String, Object>> fetchSpec = mock(Neo4jClient.RecordFetchSpec.class);

        private Chain() {
            lenient().when(neo4jClient.query(anyString())).thenReturn(spec);
            lenient().when(spec.bind(any())).thenReturn(bindSpec);
            lenient().when(bindSpec.to(anyString())).thenReturn(spec);
            lenient().when(spec.fetch()).thenReturn(fetchSpec);
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

    private Value mockNullValue() {
        Value val = mock(Value.class);
        when(val.isNull()).thenReturn(true);
        return val;
    }

    private Value mockIntValue(long value) {
        Value val = mock(Value.class);
        Type type = mock(Type.class);
        when(val.isNull()).thenReturn(false);
        when(type.name()).thenReturn("INTEGER");
        when(val.type()).thenReturn(type);
        when(val.asLong()).thenReturn(value);
        return val;
    }

    private Node mockNode(String label, Long neo4jId, Value idValue, Value nameValue, Value titleValue) {
        Node node = mock(Node.class);
        when(node.labels()).thenReturn(label == null ? List.of() : List.of(label));
        if (neo4jId != null) {
            when(node.id()).thenReturn(neo4jId);
        }
        if (idValue != null) {
            when(node.containsKey("id")).thenReturn(true);
            when(node.get("id")).thenReturn(idValue);
        }
        if (nameValue != null) {
            when(node.containsKey("name")).thenReturn(true);
            when(node.get("name")).thenReturn(nameValue);
        }
        if (titleValue != null) {
            when(node.containsKey("title")).thenReturn(true);
            when(node.get("title")).thenReturn(titleValue);
        }
        return node;
    }

    private Map<String, Object> bindParams(ArgumentCaptor<Object> bindCaptor, ArgumentCaptor<String> toCaptor) {
        Map<String, Object> params = new LinkedHashMap<>();
        List<Object> values = bindCaptor.getAllValues();
        List<String> names = toCaptor.getAllValues();
        for (int i = 0; i < values.size(); i++) {
            params.put(names.get(i), values.get(i));
        }
        return params;
    }

    // ==================== NormalTests ====================

    @Nested
    @DisplayName("NormalTests — 正常路径")
    class NormalTests {

        @Test
        @DisplayName("upsertPartyEntity Person 绑定 id 与全部属性字段")
        void upsertPartyEntityPersonBindsAllFields() {
            Chain chain = new Chain();

            Map<String, Object> props = new LinkedHashMap<>();
            props.put("id", "p1");
            props.put("name", "毛泽东");
            props.put("description", "中国共产党创始人");
            props.put("birthYear", 1893);
            props.put("deathYear", 1976);

            partyGraphService.upsertPartyEntity("Person", props);

            ArgumentCaptor<String> cypherCaptor = ArgumentCaptor.forClass(String.class);
            verify(neo4jClient).query(cypherCaptor.capture());
            assertThat(cypherCaptor.getValue())
                    .contains("MERGE (n:Person {id:$id})")
                    .contains("n.name = $name")
                    .contains("n.description = $description")
                    .contains("n.birthYear = $birthYear")
                    .contains("n.deathYear = $deathYear");

            ArgumentCaptor<Object> bindCaptor = ArgumentCaptor.forClass(Object.class);
            ArgumentCaptor<String> toCaptor = ArgumentCaptor.forClass(String.class);
            verify(chain.spec, times(5)).bind(bindCaptor.capture());
            verify(chain.bindSpec, times(5)).to(toCaptor.capture());

            assertThat(bindParams(bindCaptor, toCaptor))
                    .containsEntry("id", "p1")
                    .containsEntry("name", "毛泽东")
                    .containsEntry("description", "中国共产党创始人")
                    .containsEntry("birthYear", 1893)
                    .containsEntry("deathYear", 1976);
            verify(chain.spec).run();
        }

        @Test
        @DisplayName("upsertPartyEntity 5 类实体各生成对应 label 的 MERGE Cypher")
        void upsertPartyEntityAllTypesGenerateTypeSpecificCypher() {
            new Chain();
            // LinkedHashMap 保证类型遍历顺序，与 cypherCaptor 捕获顺序一致
            Map<String, Map<String, Object>> cases = new LinkedHashMap<>();
            cases.put("Person", Map.of("id", "p1", "name", "毛泽东", "birthYear", 1893));
            cases.put("Event", Map.of("id", "e1", "name", "中共一大", "date", "1921-07-23", "location", "上海"));
            cases.put("Theory", Map.of("id", "t1", "name", "毛泽东思想", "era", "新民主主义革命时期"));
            cases.put("Document", Map.of("id", "d1", "name", "《共产党宣言》", "publisher", "共产主义者同盟", "date", "1848"));
            cases.put("Location", Map.of("id", "l1", "name", "上海", "type", "城市"));

            for (Map.Entry<String, Map<String, Object>> entry : cases.entrySet()) {
                partyGraphService.upsertPartyEntity(entry.getKey(), entry.getValue());
            }

            ArgumentCaptor<String> cypherCaptor = ArgumentCaptor.forClass(String.class);
            verify(neo4jClient, times(5)).query(cypherCaptor.capture());
            List<String> cyphers = cypherCaptor.getAllValues();

            assertThat(cyphers.get(0)).contains("MERGE (n:Person {id:$id})").contains("n.birthYear = $birthYear");
            assertThat(cyphers.get(1))
                    .contains("MERGE (n:Event {id:$id})")
                    .contains("n.date = $date")
                    .contains("n.location = $location");
            assertThat(cyphers.get(2)).contains("MERGE (n:Theory {id:$id})").contains("n.era = $era");
            assertThat(cyphers.get(3))
                    .contains("MERGE (n:Document {id:$id})")
                    .contains("n.publisher = $publisher")
                    .contains("n.date = $date");
            assertThat(cyphers.get(4)).contains("MERGE (n:Location {id:$id})").contains("n.type = $type");
        }

        @Test
        @DisplayName("upsertPartyEntity 重复调用仍为 MERGE（幂等）")
        void upsertPartyEntityIsIdempotent() {
            Chain chain = new Chain();
            Map<String, Object> props = Map.of("id", "p1", "name", "毛泽东", "description", "中共创始人");

            partyGraphService.upsertPartyEntity("Person", props);
            partyGraphService.upsertPartyEntity("Person", props);

            ArgumentCaptor<String> cypherCaptor = ArgumentCaptor.forClass(String.class);
            verify(neo4jClient, times(2)).query(cypherCaptor.capture());
            assertThat(cypherCaptor.getAllValues())
                    .allMatch(c -> c.contains("MERGE (n:Person {id:$id})") && !c.contains("CREATE "));
            verify(chain.spec, times(2)).run();
        }

        @Test
        @DisplayName("queryByType 返回该类型全部节点及其关联子图")
        void queryByTypeReturnsNodesAndEdges() {
            Chain chain = new Chain();
            Map<String, Object> row = new HashMap<>();
            row.put("n", mockPartyNode("Person", "p1", "毛泽东"));
            row.put("r", mockRelationship("PARTICIPATED_IN"));
            row.put("m", mockPartyNode("Event", "e1", "中共一大"));
            when(chain.fetchSpec.all()).thenReturn((Collection) List.of(row));

            KnowledgeGraphResponse result = partyGraphService.queryByType("Person");

            assertThat(result).isNotNull();
            assertThat(result.getNodes()).hasSize(2);
            assertThat(result.getNodes())
                    .extracting(GraphNodeResponse::getId)
                    .containsExactlyInAnyOrder("Person:p1", "Event:e1");
            assertThat(result.getEdges()).hasSize(1);
            assertThat(result.getEdges())
                    .singleElement()
                    .extracting(GraphEdgeResponse::getType)
                    .isEqualTo("PARTICIPATED_IN");

            ArgumentCaptor<String> cypherCaptor = ArgumentCaptor.forClass(String.class);
            verify(neo4jClient).query(cypherCaptor.capture());
            assertThat(cypherCaptor.getValue()).contains("MATCH (n:Person)");
        }

        @Test
        @DisplayName("queryRelated 1 跳邻居查询绑定 entityId 与 relationTypes")
        void queryRelatedReturnsOneHopNeighbors() {
            Chain chain = new Chain();
            Map<String, Object> row = new HashMap<>();
            row.put("n", mockPartyNode("Person", "p1", "毛泽东"));
            row.put("r", mockRelationship("INITIATED"));
            row.put("m", mockPartyNode("Event", "e1", "秋收起义"));
            when(chain.fetchSpec.all()).thenReturn((Collection) List.of(row));

            KnowledgeGraphResponse result =
                    partyGraphService.queryRelated("Person", "p1", List.of("INITIATED", "PARTICIPATED_IN"));

            assertThat(result).isNotNull();
            assertThat(result.getNodes()).hasSize(2);
            assertThat(result.getEdges()).hasSize(1);
            assertThat(result.getEdges())
                    .singleElement()
                    .extracting(GraphEdgeResponse::getType)
                    .isEqualTo("INITIATED");

            ArgumentCaptor<String> cypherCaptor = ArgumentCaptor.forClass(String.class);
            verify(neo4jClient).query(cypherCaptor.capture());
            assertThat(cypherCaptor.getValue())
                    .contains("MATCH (n:Person {id:$entityId})-[r]-(m)")
                    .contains("WHERE r.type() IN $relationTypes");

            ArgumentCaptor<Object> bindCaptor = ArgumentCaptor.forClass(Object.class);
            ArgumentCaptor<String> toCaptor = ArgumentCaptor.forClass(String.class);
            verify(chain.spec, times(2)).bind(bindCaptor.capture());
            verify(chain.bindSpec, times(2)).to(toCaptor.capture());

            assertThat(bindParams(bindCaptor, toCaptor))
                    .containsEntry("entityId", "p1")
                    .containsEntry("relationTypes", List.of("INITIATED", "PARTICIPATED_IN"));
        }
    }

    // ==================== ErrorTests ====================

    @Nested
    @DisplayName("ErrorTests — 异常路径")
    class ErrorTests {

        @Test
        @DisplayName("非法实体类型抛出 PARTY_ENTITY_TYPE_INVALID")
        void invalidTypeThrows() {
            assertThatThrownBy(() -> partyGraphService.upsertPartyEntity("Unknown", Map.of("id", "x1")))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("不支持的党史实体类型: Unknown");

            assertThatThrownBy(() -> partyGraphService.queryByType("Unknown"))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("不支持的党史实体类型: Unknown");

            assertThatThrownBy(() -> partyGraphService.queryRelated("Unknown", "x1", List.of("RELATED_TO")))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("不支持的党史实体类型: Unknown");

            verify(neo4jClient, never()).query(anyString());
        }

        @Test
        @DisplayName("upsertPartyEntity 缺少 id 抛出 PARTY_ENTITY_ID_REQUIRED")
        void upsertPartyEntityMissingIdThrows() {
            assertThatThrownBy(() -> partyGraphService.upsertPartyEntity("Person", Map.of("name", "毛泽东")))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("党史实体 ID 不能为空")
                    .extracting(e -> ((BusinessException) e).getCode())
                    .isEqualTo(GraphErrorConstants.PARTY_ENTITY_ID_REQUIRED);

            verify(neo4jClient, never()).query(anyString());
        }
    }

    // ==================== BoundaryTests ====================

    @Nested
    @DisplayName("BoundaryTests — 边界情况")
    class BoundaryTests {

        @Test
        @DisplayName("queryByType 空库返回空 nodes/edges 不报错")
        void queryByTypeEmptyReturnsEmptyGraph() {
            Chain chain = new Chain();
            when(chain.fetchSpec.all()).thenReturn(List.of());

            KnowledgeGraphResponse result = partyGraphService.queryByType("Person");

            assertThat(result).isNotNull();
            assertThat(result.getNodes()).isEmpty();
            assertThat(result.getEdges()).isEmpty();
        }

        @Test
        @DisplayName("queryRelated 目标实体不存在返回空 nodes/edges 不报错")
        void queryRelatedEntityNotFoundReturnsEmptyGraph() {
            Chain chain = new Chain();
            when(chain.fetchSpec.all()).thenReturn(List.of());

            KnowledgeGraphResponse result =
                    partyGraphService.queryRelated("Person", "not-exist", List.of("PARTICIPATED_IN"));

            assertThat(result).isNotNull();
            assertThat(result.getNodes()).isEmpty();
            assertThat(result.getEdges()).isEmpty();
        }

        @Test
        @DisplayName("queryRelated 绑定空关系类型列表不报错")
        void queryRelatedEmptyRelationTypes() {
            Chain chain = new Chain();
            when(chain.fetchSpec.all()).thenReturn(List.of());

            KnowledgeGraphResponse result = partyGraphService.queryRelated("Person", "p1", List.of());

            assertThat(result).isNotNull();
            assertThat(result.getNodes()).isEmpty();
        }
    }

    @Nested
    @DisplayName("GraphBuildBranchTests — buildGraph 分支覆盖")
    class GraphBuildBranchTests {

        @Test
        @DisplayName("源节点非 Node 时跳过该行边")
        void nonNodeSourceYieldsNoEdge() {
            Chain chain = new Chain();
            Map<String, Object> row = new HashMap<>();
            row.put("n", "not-a-node");
            row.put("r", mockRelationship("PARTICIPATED_IN"));
            row.put("m", mockPartyNode("Event", "e1", "中共一大"));
            when(chain.fetchSpec.all()).thenReturn((Collection) List.of(row));

            KnowledgeGraphResponse result = partyGraphService.queryByType("Person");

            assertThat(result.getNodes()).hasSize(1);
            assertThat(result.getNodes())
                    .singleElement()
                    .extracting(GraphNodeResponse::getId)
                    .isEqualTo("Event:e1");
            assertThat(result.getEdges()).isEmpty();
        }

        @Test
        @DisplayName("目标节点非 Node 时跳过该行边")
        void nonNodeTargetYieldsNoEdge() {
            Chain chain = new Chain();
            Map<String, Object> row = new HashMap<>();
            row.put("n", mockPartyNode("Person", "p1", "毛泽东"));
            row.put("r", mockRelationship("PARTICIPATED_IN"));
            row.put("m", "not-a-node");
            when(chain.fetchSpec.all()).thenReturn((Collection) List.of(row));

            KnowledgeGraphResponse result = partyGraphService.queryByType("Person");

            assertThat(result.getNodes()).hasSize(1);
            assertThat(result.getNodes())
                    .singleElement()
                    .extracting(GraphNodeResponse::getId)
                    .isEqualTo("Person:p1");
            assertThat(result.getEdges()).isEmpty();
        }

        @Test
        @DisplayName("节点无 label 时回退为 Node")
        void nodeWithoutLabelsUsesNodeFallbackLabel() {
            Chain chain = new Chain();
            Map<String, Object> row = new HashMap<>();
            row.put("n", mockNode(null, null, mockStringValue("p1"), mockStringValue("毛泽东"), null));
            row.put("r", null);
            row.put("m", mockPartyNode("Event", "e1", "中共一大"));
            when(chain.fetchSpec.all()).thenReturn((Collection) List.of(row));

            KnowledgeGraphResponse result = partyGraphService.queryByType("Person");

            assertThat(result.getNodes()).extracting(GraphNodeResponse::getId).contains("Node:p1");
            assertThat(result.getNodes())
                    .extracting(GraphNodeResponse::getLabel)
                    .contains("Node");
        }

        @Test
        @DisplayName("节点缺少 id 属性时回退 Neo4j 内部 ID")
        void nodeWithoutIdPropertyFallsBackToNeo4jId() {
            Chain chain = new Chain();
            Map<String, Object> row = new HashMap<>();
            row.put("n", mockNode("Person", 42L, null, mockStringValue("毛泽东"), null));
            row.put("r", null);
            row.put("m", mockPartyNode("Event", "e1", "中共一大"));
            when(chain.fetchSpec.all()).thenReturn((Collection) List.of(row));

            KnowledgeGraphResponse result = partyGraphService.queryByType("Person");

            assertThat(result.getNodes()).extracting(GraphNodeResponse::getId).contains("Person:42");
        }

        @Test
        @DisplayName("节点 id 属性为 null 时回退 Neo4j 内部 ID")
        void nodeWithNullIdPropertyFallsBackToNeo4jId() {
            Chain chain = new Chain();
            Map<String, Object> row = new HashMap<>();
            row.put("n", mockNode("Person", 42L, mockNullValue(), mockStringValue("毛泽东"), null));
            row.put("r", null);
            row.put("m", mockPartyNode("Event", "e1", "中共一大"));
            when(chain.fetchSpec.all()).thenReturn((Collection) List.of(row));

            KnowledgeGraphResponse result = partyGraphService.queryByType("Person");

            assertThat(result.getNodes()).extracting(GraphNodeResponse::getId).contains("Person:42");
        }

        @Test
        @DisplayName("节点 id 为整数时按数值读取")
        void nodeWithIntegerIdUsesNumericValue() {
            Chain chain = new Chain();
            Map<String, Object> row = new HashMap<>();
            row.put("n", mockNode("Person", null, mockIntValue(123L), mockStringValue("毛泽东"), null));
            row.put("r", null);
            row.put("m", mockPartyNode("Event", "e1", "中共一大"));
            when(chain.fetchSpec.all()).thenReturn((Collection) List.of(row));

            KnowledgeGraphResponse result = partyGraphService.queryByType("Person");

            assertThat(result.getNodes()).extracting(GraphNodeResponse::getId).contains("Person:123");
        }

        @Test
        @DisplayName("节点无 name 与 title 时名称回退为 id")
        void nodeWithoutNameOrTitleFallsBackToId() {
            Chain chain = new Chain();
            Map<String, Object> row = new HashMap<>();
            row.put("n", mockNode("Person", null, mockStringValue("p1"), null, null));
            row.put("r", null);
            row.put("m", mockPartyNode("Event", "e1", "中共一大"));
            when(chain.fetchSpec.all()).thenReturn((Collection) List.of(row));

            KnowledgeGraphResponse result = partyGraphService.queryByType("Person");

            assertThat(result.getNodes()).extracting(GraphNodeResponse::getName).contains("p1");
        }

        @Test
        @DisplayName("节点无 name 时名称回退为 title")
        void nodeWithoutNameUsesTitle() {
            Chain chain = new Chain();
            Map<String, Object> row = new HashMap<>();
            row.put("n", mockNode("Person", null, mockStringValue("p1"), null, mockStringValue("毛泽东")));
            row.put("r", null);
            row.put("m", mockPartyNode("Event", "e1", "中共一大"));
            when(chain.fetchSpec.all()).thenReturn((Collection) List.of(row));

            KnowledgeGraphResponse result = partyGraphService.queryByType("Person");

            assertThat(result.getNodes()).extracting(GraphNodeResponse::getName).contains("毛泽东");
        }

        @Test
        @DisplayName("节点 name 为 null 时名称回退为 title")
        void nodeWithNullNameUsesTitle() {
            Chain chain = new Chain();
            Map<String, Object> row = new HashMap<>();
            row.put("n", mockNode("Person", null, mockStringValue("p1"), mockNullValue(), mockStringValue("毛泽东")));
            row.put("r", null);
            row.put("m", mockPartyNode("Event", "e1", "中共一大"));
            when(chain.fetchSpec.all()).thenReturn((Collection) List.of(row));

            KnowledgeGraphResponse result = partyGraphService.queryByType("Person");

            assertThat(result.getNodes()).extracting(GraphNodeResponse::getName).contains("毛泽东");
        }

        @Test
        @DisplayName("节点 title 为 null 时名称回退为 id")
        void nodeWithNullTitleFallsBackToId() {
            Chain chain = new Chain();
            Map<String, Object> row = new HashMap<>();
            row.put("n", mockNode("Person", null, mockStringValue("p1"), null, mockNullValue()));
            row.put("r", null);
            row.put("m", mockPartyNode("Event", "e1", "中共一大"));
            when(chain.fetchSpec.all()).thenReturn((Collection) List.of(row));

            KnowledgeGraphResponse result = partyGraphService.queryByType("Person");

            assertThat(result.getNodes()).extracting(GraphNodeResponse::getName).contains("p1");
        }

        @Test
        @DisplayName("重复行去重节点与边")
        void duplicateRowsAreDeduplicated() {
            Chain chain = new Chain();
            Map<String, Object> row1 = new HashMap<>();
            row1.put("n", mockPartyNode("Person", "p1", "毛泽东"));
            row1.put("r", mockRelationship("PARTICIPATED_IN"));
            row1.put("m", mockPartyNode("Event", "e1", "中共一大"));
            Map<String, Object> row2 = new HashMap<>();
            row2.put("n", mockPartyNode("Person", "p1", "毛泽东"));
            row2.put("r", mockRelationship("PARTICIPATED_IN"));
            row2.put("m", mockPartyNode("Event", "e1", "中共一大"));
            when(chain.fetchSpec.all()).thenReturn((Collection) List.of(row1, row2));

            KnowledgeGraphResponse result = partyGraphService.queryByType("Person");

            assertThat(result.getNodes()).hasSize(2);
            assertThat(result.getEdges()).hasSize(1);
        }
    }
}
