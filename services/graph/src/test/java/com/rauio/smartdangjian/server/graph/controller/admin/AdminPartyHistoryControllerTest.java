package com.rauio.smartdangjian.server.graph.controller.admin;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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

import com.rauio.smartdangjian.server.graph.pojo.request.PartyHistoryEntityImportRequest;
import com.rauio.smartdangjian.server.graph.pojo.request.PartyHistoryRelationshipImportRequest;
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
            PartyHistoryEntityImportRequest request = new PartyHistoryEntityImportRequest();
            request.put("name", "毛泽东");

            var result = controller.importEntities("Person", List.of(request));

            assertThat(result.getData()).isEqualTo(5);
            ArgumentCaptor<List<Map<String, Object>>> captor = ArgumentCaptor.captor();
            verify(importService).importEntities(eq("Person"), captor.capture());
            assertThat(captor.getValue()).containsExactly(Map.of("name", "毛泽东"));
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
            PartyHistoryRelationshipImportRequest request = new PartyHistoryRelationshipImportRequest();
            request.setSource("p-1");
            request.setTarget("e-1");
            request.setType("INITIATED");

            var result = controller.importRelationships(List.of(request));

            assertThat(result.getData()).isEqualTo(3);
            ArgumentCaptor<List<Map<String, Object>>> captor = ArgumentCaptor.captor();
            verify(importService).importRelationships(captor.capture());
            assertThat(captor.getValue())
                    .containsExactly(
                            Map.of("source", "p-1", "target", "e-1", "type", "INITIATED", "properties", Map.of()));
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
