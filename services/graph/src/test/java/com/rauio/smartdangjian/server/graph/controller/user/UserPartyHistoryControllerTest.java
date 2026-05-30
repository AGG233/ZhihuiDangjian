package com.rauio.smartdangjian.server.graph.controller.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import com.rauio.smartdangjian.server.graph.pojo.response.GraphEdgeResponse;
import com.rauio.smartdangjian.server.graph.pojo.response.GraphNodeResponse;
import com.rauio.smartdangjian.server.graph.pojo.response.KnowledgeGraphResponse;
import com.rauio.smartdangjian.server.graph.service.PartyHistoryQueryService;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class UserPartyHistoryControllerTest {

    @Mock
    private PartyHistoryQueryService queryService;

    @InjectMocks
    private UserPartyHistoryController controller;

    @Nested
    @DisplayName("搜索党史实体")
    class SearchTests {

        @Test
        @DisplayName("按关键词搜索返回实体列表")
        void searchSuccess() {
            var graph = KnowledgeGraphResponse.builder()
                    .nodes(List.of(GraphNodeResponse.builder()
                            .id("Person:person-1")
                            .label("Person")
                            .name("毛泽东")
                            .build()))
                    .edges(List.of())
                    .build();
            when(queryService.searchEntities(anyString(), any(), anyInt(), anyInt()))
                    .thenReturn(graph);

            var result = controller.search("毛泽东", null, 1, 20);

            assertThat(result.getData().getNodes()).hasSize(1);
            assertThat(result.getData().getNodes().getFirst().getName()).isEqualTo("毛泽东");
        }

        @Test
        @DisplayName("搜索无结果返回空图谱")
        void searchEmpty() {
            var graph = KnowledgeGraphResponse.builder()
                    .nodes(List.of())
                    .edges(List.of())
                    .build();
            when(queryService.searchEntities(anyString(), any(), anyInt(), anyInt()))
                    .thenReturn(graph);

            var result = controller.search("不存在", null, 1, 20);

            assertThat(result.getData().getNodes()).isEmpty();
        }
    }

    @Nested
    @DisplayName("获取实体详情")
    class DetailTests {

        @Test
        @DisplayName("获取实体及其关联信息")
        void getDetailSuccess() {
            var graph = KnowledgeGraphResponse.builder()
                    .nodes(List.of(GraphNodeResponse.builder()
                            .id("Person:person-1")
                            .label("Person")
                            .name("邓小平")
                            .build()))
                    .edges(List.of(GraphEdgeResponse.builder()
                            .source("Person:person-1")
                            .target("Event:event-1")
                            .type("INITIATED")
                            .build()))
                    .build();
            when(queryService.getEntityDetail(anyString())).thenReturn(graph);

            var result = controller.getDetail("person-1");

            assertThat(result.getData().getNodes()).hasSize(1);
            assertThat(result.getData().getEdges()).hasSize(1);
        }
    }

    @Test
    @DisplayName("获取人物关联事件")
    void getPersonEvents() {
        var graph = KnowledgeGraphResponse.builder()
                .nodes(List.of(GraphNodeResponse.builder()
                        .id("Person:person-1")
                        .label("Person")
                        .name("朱德")
                        .build()))
                .edges(List.of())
                .build();
        when(queryService.getPersonEvents(anyString())).thenReturn(graph);

        var result = controller.getPersonEvents("person-1");

        assertThat(result.getData().getNodes()).hasSize(1);
    }

    @Test
    @DisplayName("获取事件时间线")
    void getEventTimeline() {
        var graph = KnowledgeGraphResponse.builder()
                .nodes(List.of(GraphNodeResponse.builder()
                        .id("Event:event-1")
                        .label("Event")
                        .name("事件A")
                        .build()))
                .edges(List.of())
                .build();
        when(queryService.getEventTimeline(anyString(), anyInt())).thenReturn(graph);

        var result = controller.getEventTimeline("event-1", 2);

        assertThat(result.getData().getNodes()).hasSize(1);
    }

    @Test
    @DisplayName("获取理论演进")
    void getTheoryEvolution() {
        var graph = KnowledgeGraphResponse.builder()
                .nodes(List.of(GraphNodeResponse.builder()
                        .id("Theory:t-1")
                        .label("Theory")
                        .name("三个代表")
                        .build()))
                .edges(List.of())
                .build();
        when(queryService.getTheoryEvolution(anyString())).thenReturn(graph);

        var result = controller.getTheoryEvolution("t-1");

        assertThat(result.getData().getNodes()).hasSize(1);
    }
}
