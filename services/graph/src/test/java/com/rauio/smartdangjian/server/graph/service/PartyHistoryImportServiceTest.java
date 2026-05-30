package com.rauio.smartdangjian.server.graph.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
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

@ExtendWith(MockitoExtension.class)
class PartyHistoryImportServiceTest {

    @Mock
    private PartyHistoryGraphService partyHistoryGraphService;

    @InjectMocks
    private PartyHistoryImportService importService;

    // ==================== importEntities ====================

    @Nested
    @DisplayName("importEntities — 批量导入实体节点")
    class ImportEntitiesTests {

        @Test
        @DisplayName("成功导入实体节点返回导入数量")
        void importEntitiesSuccess() {
            List<Map<String, Object>> entities = new ArrayList<>();
            for (int i = 0; i < 3; i++) {
                Map<String, Object> entity = new HashMap<>();
                entity.put("graph_id", "person-" + i);
                entity.put("name", "人物" + i);
                entities.add(entity);
            }
            doNothing().when(partyHistoryGraphService).batchMergeEntities(anyString(), anyList());

            int count = importService.importEntities("Person", entities);

            assertThat(count).isEqualTo(3);
            verify(partyHistoryGraphService).batchMergeEntities("Person", entities);
        }

        @Test
        @DisplayName("实体列表为空时返回 0 且不调用 graphService")
        void importEntitiesEmptyList() {
            int count = importService.importEntities("Person", List.of());

            assertThat(count).isZero();
            verify(partyHistoryGraphService, never()).batchMergeEntities(anyString(), anyList());
        }

        @Test
        @DisplayName("实体列表为 null 时返回 0 且不调用 graphService")
        void importEntitiesNullList() {
            int count = importService.importEntities("Person", null);

            assertThat(count).isZero();
            verify(partyHistoryGraphService, never()).batchMergeEntities(anyString(), anyList());
        }

        @Test
        @DisplayName("导入 Document 类型实体")
        void importDocumentEntities() {
            List<Map<String, Object>> entities = new ArrayList<>();
            Map<String, Object> entity = new HashMap<>();
            entity.put("graph_id", "doc-1");
            entity.put("title", "重要文献");
            entities.add(entity);

            doNothing().when(partyHistoryGraphService).batchMergeEntities(anyString(), anyList());

            int count = importService.importEntities("Document", entities);

            assertThat(count).isEqualTo(1);
            verify(partyHistoryGraphService).batchMergeEntities("Document", entities);
        }

        @Test
        @DisplayName("导入大量实体")
        void importEntitiesMany() {
            List<Map<String, Object>> entities = new ArrayList<>();
            for (int i = 0; i < 100; i++) {
                Map<String, Object> entity = new HashMap<>();
                entity.put("graph_id", "entity-" + i);
                entities.add(entity);
            }
            doNothing().when(partyHistoryGraphService).batchMergeEntities(anyString(), anyList());

            int count = importService.importEntities("Event", entities);

            assertThat(count).isEqualTo(100);
        }
    }

    // ==================== importRelationships ====================

    @Nested
    @DisplayName("importRelationships — 批量导入关系")
    class ImportRelationshipsTests {

        @Test
        @DisplayName("成功导入关系返回导入数量")
        void importRelationshipsSuccess() {
            List<Map<String, Object>> relationships = new ArrayList<>();
            for (int i = 0; i < 5; i++) {
                Map<String, Object> rel = new HashMap<>();
                rel.put("relType", "PARTICIPATED_IN");
                rel.put("sourceId", "person-" + i);
                rel.put("targetId", "event-" + i);
                rel.put("properties", new HashMap<>());
                relationships.add(rel);
            }
            doNothing().when(partyHistoryGraphService).batchAddRelationships(anyList());

            int count = importService.importRelationships(relationships);

            assertThat(count).isEqualTo(5);
            verify(partyHistoryGraphService).batchAddRelationships(relationships);
        }

        @Test
        @DisplayName("关系列表为空时返回 0 且不调用 graphService")
        void importRelationshipsEmptyList() {
            int count = importService.importRelationships(List.of());

            assertThat(count).isZero();
            verify(partyHistoryGraphService, never()).batchAddRelationships(anyList());
        }

        @Test
        @DisplayName("关系列表为 null 时返回 0 且不调用 graphService")
        void importRelationshipsNullList() {
            int count = importService.importRelationships(null);

            assertThat(count).isZero();
            verify(partyHistoryGraphService, never()).batchAddRelationships(anyList());
        }

        @Test
        @DisplayName("导入多种类型的关系")
        void importRelationshipsMultipleTypes() {
            List<Map<String, Object>> relationships = new ArrayList<>();
            Map<String, Object> rel1 = new HashMap<>();
            rel1.put("relType", "INITIATED");
            rel1.put("sourceId", "person-1");
            rel1.put("targetId", "event-1");
            rel1.put("properties", new HashMap<>());
            relationships.add(rel1);

            Map<String, Object> rel2 = new HashMap<>();
            rel2.put("relType", "PROMOTED");
            rel2.put("sourceId", "person-2");
            rel2.put("targetId", "event-2");
            rel2.put("properties", new HashMap<>());
            relationships.add(rel2);

            doNothing().when(partyHistoryGraphService).batchAddRelationships(anyList());

            int count = importService.importRelationships(relationships);

            assertThat(count).isEqualTo(2);
        }
    }
}
