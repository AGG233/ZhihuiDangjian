package com.rauio.smartdangjian.server.graph.controller.admin;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.rauio.smartdangjian.server.graph.service.PartyHistoryGraphService;
import com.rauio.smartdangjian.server.graph.service.PartyHistoryImportService;

@ExtendWith(MockitoExtension.class)
@DisplayName("管理员党史图谱管理接口测试")
class AdminPartyHistoryControllerTest {

    @Mock
    private PartyHistoryImportService importService;

    @Mock
    private PartyHistoryGraphService graphService;

    @InjectMocks
    private AdminPartyHistoryController controller;

    @Nested
    @DisplayName("批量导入实体")
    class ImportEntitiesTests {

        @Test
        @DisplayName("按标签批量导入实体成功")
        void importEntitiesSuccess() {
            when(importService.importEntities(anyString(), any())).thenReturn(5);

            var result = controller.importEntities("Person", List.of(Map.of("name", "毛泽东")));

            assertThat(result.getData()).isEqualTo(5);
        }

        @Test
        @DisplayName("导入空列表返回 0")
        void importEntitiesEmpty() {
            when(importService.importEntities(anyString(), any())).thenReturn(0);

            var result = controller.importEntities("Event", List.of());

            assertThat(result.getData()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("批量导入关系")
    class ImportRelationshipsTests {

        @Test
        @DisplayName("批量导入关系成功")
        void importRelationshipsSuccess() {
            when(importService.importRelationships(any())).thenReturn(3);

            var result = controller.importRelationships(
                    List.of(Map.of("source", "p-1", "target", "e-1", "type", "INITIATED")));

            assertThat(result.getData()).isEqualTo(3);
        }

        @Test
        @DisplayName("导入关系空列表返回 0")
        void importRelationshipsEmpty() {
            when(importService.importRelationships(any())).thenReturn(0);

            var result = controller.importRelationships(List.of());

            assertThat(result.getData()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("删除实体")
    class DeleteEntityTests {

        @Test
        @DisplayName("根据 graphId 删除实体成功")
        void deleteEntitySuccess() {
            var result = controller.deleteEntity("entity-1");

            verify(graphService).deleteEntity("entity-1");
            assertThat(result.getCode()).isEqualTo("200");
        }
    }
}
