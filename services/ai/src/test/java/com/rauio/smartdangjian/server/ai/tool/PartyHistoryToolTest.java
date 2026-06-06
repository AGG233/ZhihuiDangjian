package com.rauio.smartdangjian.server.ai.tool;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rauio.smartdangjian.server.graph.api.GraphQueryFacade;
import com.rauio.smartdangjian.server.graph.pojo.response.GraphNodeResponse;
import com.rauio.smartdangjian.server.graph.pojo.response.KnowledgeGraphResponse;

@ExtendWith(MockitoExtension.class)
class PartyHistoryToolTest {

    @Mock
    private GraphQueryFacade graphQueryFacade;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private PartyHistoryTool tool;

    private KnowledgeGraphResponse mockGraph(String label, String id, String name) {
        return KnowledgeGraphResponse.builder()
                .nodes(List.of(GraphNodeResponse.builder()
                        .id(label + ":" + id)
                        .label(label)
                        .name(name)
                        .build()))
                .edges(List.of())
                .build();
    }

    @Test
    @DisplayName("搜索党史返回序列化结果")
    void searchPartyHistory() throws Exception {
        var graph = mockGraph("Person", "person-1", "毛泽东");
        when(graphQueryFacade.searchEntities(anyString(), anyList(), anyInt(), anyInt()))
                .thenReturn(graph);
        when(objectMapper.writeValueAsString(graph)).thenReturn("{\"nodes\":[{\"name\":\"毛泽东\"}]}");

        String result = tool.searchPartyHistory("毛泽东", List.of(), 10);

        assertThat(result).contains("毛泽东");
    }

    @Test
    @DisplayName("limit为null默认使用10")
    void searchWithNullLimit() throws Exception {
        var graph = mockGraph("Person", "person-1", "测试");
        when(graphQueryFacade.searchEntities(anyString(), anyList(), anyInt(), anyInt()))
                .thenReturn(graph);
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");

        String result = tool.searchPartyHistory("测试", List.of(), null);

        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("entityTypes为null使用空列表")
    void searchWithNullTypes() throws Exception {
        var graph = mockGraph("Event", "event-1", "长征");
        when(graphQueryFacade.searchEntities(anyString(), anyList(), anyInt(), anyInt()))
                .thenReturn(graph);
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");

        String result = tool.searchPartyHistory("长征", null, 5);

        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("查询人物详情")
    void getPersonDetail() throws Exception {
        var searchResult = mockGraph("Person", "person-1", "邓小平");
        var detail = mockGraph("Person", "person-1", "邓小平");
        when(graphQueryFacade.searchEntities("邓小平", List.of("Person"), 1, 1)).thenReturn(searchResult);
        when(graphQueryFacade.getEntityDetail("person-1")).thenReturn(detail);
        when(objectMapper.writeValueAsString(detail)).thenReturn("{\"nodes\":[{\"name\":\"邓小平\"}]}");

        String result = tool.getPersonDetail("邓小平");

        assertThat(result).contains("邓小平");
    }

    @Test
    @DisplayName("查询不存在的人物返回错误消息")
    void getPersonDetailNotFound() {
        var empty = KnowledgeGraphResponse.builder()
                .nodes(List.of())
                .edges(List.of())
                .build();
        when(graphQueryFacade.searchEntities(anyString(), anyList(), anyInt(), anyInt()))
                .thenReturn(empty);

        String result = tool.getPersonDetail("不存在");

        assertThat(result).contains("未找到人物");
    }

    @Test
    @DisplayName("追溯理论演进")
    void traceTheoryEvolution() throws Exception {
        var searchResult = mockGraph("Theory", "theory-1", "邓小平理论");
        var evolution = mockGraph("Theory", "theory-1", "邓小平理论");
        when(graphQueryFacade.searchEntities("邓小平理论", List.of("Theory"), 1, 1)).thenReturn(searchResult);
        when(graphQueryFacade.getTheoryEvolution("theory-1")).thenReturn(evolution);
        when(objectMapper.writeValueAsString(evolution)).thenReturn("{\"nodes\":[{\"name\":\"邓小平理论\"}]}");

        String result = tool.traceTheoryEvolution("邓小平理论");

        assertThat(result).contains("邓小平理论");
    }

    @Test
    @DisplayName("查询事件时间线")
    void getEventTimeline() throws Exception {
        var searchResult = mockGraph("Event", "event-1", "十一届三中全会");
        var timeline = mockGraph("Event", "event-1", "十一届三中全会");
        when(graphQueryFacade.searchEntities("十一届三中全会", List.of("Event"), 1, 1)).thenReturn(searchResult);
        when(graphQueryFacade.getEventTimeline("event-1", 3)).thenReturn(timeline);
        when(objectMapper.writeValueAsString(timeline)).thenReturn("{\"nodes\":[{\"name\":\"十一届三中全会\"}]}");

        String result = tool.getEventTimeline("十一届三中全会");

        assertThat(result).contains("十一届三中全会");
    }

    @Test
    @DisplayName("序列化失败返回空JSON")
    void serializationError() throws Exception {
        var graph = mockGraph("Person", "person-1", "测试");
        when(graphQueryFacade.searchEntities(anyString(), anyList(), anyInt(), anyInt()))
                .thenReturn(graph);
        when(objectMapper.writeValueAsString(any())).thenThrow(new JsonProcessingException("error") {});

        String result = tool.searchPartyHistory("测试", List.of(), 10);

        assertThat(result).isEqualTo("{}");
    }
}
