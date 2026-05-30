package com.rauio.smartdangjian.server.graph.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.neo4j.core.Neo4jClient;

import com.rauio.smartdangjian.exception.BusinessException;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PartyHistoryGraphServiceTest {

    @Mock
    private Neo4jClient neo4jClient;

    @InjectMocks
    private PartyHistoryGraphService graphService;

    // ==================== Neo4jClient query chain mock helper ====================

    /** Mock query().bind().to().bind().to().run() chain. */
    private Neo4jClient.UnboundRunnableSpec setupQueryChain() {
        Neo4jClient.UnboundRunnableSpec spec = mock(Neo4jClient.UnboundRunnableSpec.class);
        Neo4jClient.OngoingBindSpec bindSpec = mock(Neo4jClient.OngoingBindSpec.class);
        lenient().when(neo4jClient.query(anyString())).thenReturn(spec);
        lenient().when(spec.bind(any())).thenReturn(bindSpec);
        lenient().when(bindSpec.to(anyString())).thenReturn(spec);
        return spec;
    }

    // ==================== mergePerson / mergeEvent / mergeLocation / mergeTheory ====================

    @Nested
    @DisplayName("mergeEntity — 合并实体节点")
    class MergeEntityTests {

        @Test
        @DisplayName("mergePerson 正确调用 Cypher 语句")
        void mergePersonSuccess() {
            var spec = setupQueryChain();

            graphService.mergePerson("person-1", "毛泽东", null);

            verify(spec).run();
        }

        @Test
        @DisplayName("mergeEvent 正确调用 Cypher 语句")
        void mergeEventSuccess() {
            var spec = setupQueryChain();

            graphService.mergeEvent("event-1", "遵义会议", null);

            verify(spec).run();
        }

        @Test
        @DisplayName("mergeLocation 正确调用 Cypher 语句")
        void mergeLocationSuccess() {
            var spec = setupQueryChain();

            graphService.mergeLocation("loc-1", "井冈山", null);

            verify(spec).run();
        }

        @Test
        @DisplayName("mergeTheory 正确调用 Cypher 语句")
        void mergeTheorySuccess() {
            var spec = setupQueryChain();

            graphService.mergeTheory("theory-1", "邓小平理论", null);

            verify(spec).run();
        }

        @Test
        @DisplayName("合并实体并设置额外属性")
        void mergeEntityWithExtraProps() {
            var spec = setupQueryChain();
            Map<String, Object> extraProps = new HashMap<>();
            extraProps.put("year", 1935);
            extraProps.put("description", "重要历史事件");

            graphService.mergeEvent("event-1", "遵义会议", extraProps);

            verify(spec).run();
        }

        @Test
        @DisplayName("graphId 为 null 时自动生成 UUID")
        void mergeEntityWithNullGraphId() {
            var spec = setupQueryChain();

            graphService.mergePerson(null, "匿名人物", null);

            verify(spec).run();
        }

        @Test
        @DisplayName("graphId 为空白字符串时自动生成 UUID")
        void mergeEntityWithBlankGraphId() {
            var spec = setupQueryChain();

            graphService.mergePerson("   ", "空白ID人物", null);

            verify(spec).run();
        }
    }

    // ==================== mergeDocument ====================

    @Nested
    @DisplayName("mergeDocument — 合并文献节点")
    class MergeDocumentTests {

        @Test
        @DisplayName("mergeDocument 使用 title 属性正确合并")
        void mergeDocumentSuccess() {
            var spec = setupQueryChain();

            graphService.mergeDocument("doc-1", "共产党宣言", null);

            verify(spec).run();
        }

        @Test
        @DisplayName("mergeDocument 带额外属性")
        void mergeDocumentWithExtraProps() {
            var spec = setupQueryChain();
            Map<String, Object> extraProps = new HashMap<>();
            extraProps.put("author", "马克思");

            graphService.mergeDocument("doc-1", "共产党宣言", extraProps);

            verify(spec).run();
        }

        @Test
        @DisplayName("mergeDocument 的 graphId 为 null 时自动生成")
        void mergeDocumentWithNullGraphId() {
            var spec = setupQueryChain();

            graphService.mergeDocument(null, "无ID文献", null);

            verify(spec).run();
        }
    }

    // ==================== addRelationship ====================

    @Nested
    @DisplayName("addRelationship — 添加关系")
    class AddRelationshipTests {

        @Test
        @DisplayName("成功添加两个实体间的关系")
        void addRelationshipSuccess() {
            var spec = setupQueryChain();

            graphService.addRelationship("person-1", "event-1", "PARTICIPATED_IN", null);

            verify(spec).run();
        }

        @Test
        @DisplayName("添加带属性的关系")
        void addRelationshipWithProperties() {
            var spec = setupQueryChain();
            Map<String, Object> props = new HashMap<>();
            props.put("weight", 5);
            props.put("source", "历史文献");

            graphService.addRelationship("person-1", "event-1", "INITIATED", props);

            verify(spec).run();
        }

        @Test
        @DisplayName("relType 为 null 抛出 BusinessException")
        void addRelationshipNullType() {
            assertThatThrownBy(() -> graphService.addRelationship("a", "b", null, null))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("无效的关系类型");
        }

        @Test
        @DisplayName("relType 为小写字母抛出 BusinessException")
        void addRelationshipLowercaseType() {
            assertThatThrownBy(() -> graphService.addRelationship("a", "b", "lowercase", null))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("无效的关系类型");
        }

        @Test
        @DisplayName("relType 包含非法字符抛出 BusinessException")
        void addRelationshipInvalidChars() {
            assertThatThrownBy(() -> graphService.addRelationship("a", "b", "INITIATED-1", null))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("无效的关系类型");
        }

        @Test
        @DisplayName("relType 为空字符串抛出 BusinessException")
        void addRelationshipEmptyType() {
            assertThatThrownBy(() -> graphService.addRelationship("a", "b", "", null))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("无效的关系类型");
        }
    }

    // ==================== batchMergeEntities ====================

    @Nested
    @DisplayName("batchMergeEntities — 批量导入实体")
    class BatchMergeEntitiesTests {

        @Test
        @DisplayName("批量导入 Person 实体")
        void batchMergePersonsSuccess() {
            var spec = setupQueryChain();
            List<Map<String, Object>> entities = new ArrayList<>();
            for (int i = 0; i < 5; i++) {
                Map<String, Object> entity = new HashMap<>();
                entity.put("graph_id", "person-" + i);
                entity.put("name", "人物" + i);
                entities.add(entity);
            }

            graphService.batchMergeEntities("Person", entities);

            verify(spec).run();
        }

        @Test
        @DisplayName("批量导入时自动补全缺失的 graph_id")
        void batchMergeWithMissingGraphId() {
            var spec = setupQueryChain();
            List<Map<String, Object>> entities = new ArrayList<>();
            Map<String, Object> entity = new HashMap<>();
            entity.put("name", "无ID人物");
            entities.add(entity);

            graphService.batchMergeEntities("Person", entities);

            // graph_id 应被自动填充
            assertThat(entities.getFirst()).containsKey("graph_id");
            assertThat(entities.getFirst().get("graph_id")).isNotNull();
            verify(spec).run();
        }

        @Test
        @DisplayName("批量导入 graph_id 为 null 时自动填充")
        void batchMergeWithNullGraphId() {
            var spec = setupQueryChain();
            List<Map<String, Object>> entities = new ArrayList<>();
            Map<String, Object> entity = new HashMap<>();
            entity.put("graph_id", null);
            entity.put("name", "ID为null的人物");
            entities.add(entity);

            graphService.batchMergeEntities("Person", entities);

            assertThat(entities.getFirst().get("graph_id")).isNotNull();
            verify(spec).run();
        }

        @Test
        @DisplayName("label 为 null 抛出 BusinessException")
        void batchMergeNullLabel() {
            assertThatThrownBy(() -> graphService.batchMergeEntities(null, List.of()))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("无效的节点标签");
        }

        @Test
        @DisplayName("label 为小写字母抛出 BusinessException")
        void batchMergeLowercaseLabel() {
            assertThatThrownBy(() -> graphService.batchMergeEntities("person", List.of()))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("无效的节点标签");
        }

        @Test
        @DisplayName("label 包含非法字符抛出 BusinessException")
        void batchMergeInvalidLabelChars() {
            assertThatThrownBy(() -> graphService.batchMergeEntities("Person1", List.of()))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("无效的节点标签");
        }
    }

    // ==================== batchAddRelationships ====================

    @Nested
    @DisplayName("batchAddRelationships — 批量添加关系")
    class BatchAddRelationshipsTests {

        @Test
        @DisplayName("批量添加单种类型的关系")
        void batchAddRelationshipsSuccess() {
            var spec = setupQueryChain();
            List<Map<String, Object>> relationships = new ArrayList<>();
            for (int i = 0; i < 3; i++) {
                Map<String, Object> rel = new HashMap<>();
                rel.put("relType", "PARTICIPATED_IN");
                rel.put("sourceId", "person-" + i);
                rel.put("targetId", "event-" + i);
                rel.put("properties", new HashMap<>());
                relationships.add(rel);
            }

            graphService.batchAddRelationships(relationships);

            verify(spec).run();
        }

        @Test
        @DisplayName("批量添加多种类型的关系按类型分组执行")
        void batchAddRelationshipsMultipleTypes() {
            var spec = setupQueryChain();
            List<Map<String, Object>> relationships = new ArrayList<>();

            Map<String, Object> rel1 = new HashMap<>();
            rel1.put("relType", "INITIATED");
            rel1.put("sourceId", "person-1");
            rel1.put("targetId", "event-1");
            rel1.put("properties", new HashMap<>());
            relationships.add(rel1);

            Map<String, Object> rel2 = new HashMap<>();
            rel2.put("relType", "PARTICIPATED_IN");
            rel2.put("sourceId", "person-2");
            rel2.put("targetId", "event-2");
            rel2.put("properties", new HashMap<>());
            relationships.add(rel2);

            Map<String, Object> rel3 = new HashMap<>();
            rel3.put("relType", "INITIATED");
            rel3.put("sourceId", "person-3");
            rel3.put("targetId", "event-3");
            rel3.put("properties", new HashMap<>());
            relationships.add(rel3);

            graphService.batchAddRelationships(relationships);

            // 应该执行 2 次 run（INITIATED 和 PARTICIPATED_IN 各一次）
            verify(spec, org.mockito.Mockito.times(2)).run();
        }

        @Test
        @DisplayName("无效的 relType 抛出 BusinessException")
        void batchAddRelationshipsInvalidType() {
            List<Map<String, Object>> relationships = new ArrayList<>();
            Map<String, Object> rel = new HashMap<>();
            rel.put("relType", "invalid_type");
            rel.put("sourceId", "a");
            rel.put("targetId", "b");
            rel.put("properties", new HashMap<>());
            relationships.add(rel);

            assertThatThrownBy(() -> graphService.batchAddRelationships(relationships))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("无效的关系类型");
        }
    }

    // ==================== deleteEntity ====================

    @Nested
    @DisplayName("deleteEntity — 删除实体")
    class DeleteEntityTests {

        @Test
        @DisplayName("成功删除实体及其所有关系")
        void deleteEntitySuccess() {
            var spec = setupQueryChain();

            graphService.deleteEntity("entity-1");

            verify(spec).run();
        }
    }
}
