package com.rauio.smartdangjian.server.graph.controller.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.rauio.smartdangjian.server.graph.pojo.response.GraphEdgeResponse;
import com.rauio.smartdangjian.server.graph.pojo.response.GraphNodeResponse;
import com.rauio.smartdangjian.server.graph.pojo.response.KnowledgeGraphResponse;
import com.rauio.smartdangjian.server.graph.service.KnowledgeGraphService;

@ExtendWith(MockitoExtension.class)
class UserKnowledgeGraphControllerTest {

    @Mock
    private KnowledgeGraphService knowledgeGraphService;

    @InjectMocks
    private UserKnowledgeGraphController controller;

    @Test
    @DisplayName("getUserGraph 委托 service 返回用户学习图谱")
    void getUserGraph() {
        KnowledgeGraphResponse graph = KnowledgeGraphResponse.builder()
                .nodes(List.of(GraphNodeResponse.builder()
                        .id("User:user-1")
                        .label("User")
                        .name("张三")
                        .build()))
                .edges(List.of(GraphEdgeResponse.builder()
                        .source("User:user-1")
                        .target("Course:course-1")
                        .type("LEARNED")
                        .build()))
                .build();
        when(knowledgeGraphService.getUserGraph("user-1")).thenReturn(graph);

        var result = controller.getUserGraph("user-1");

        assertThat(result).isNotNull();
        assertThat(result.getData()).isNotNull();
        assertThat(result.getData().getNodes()).hasSize(1);
        assertThat(result.getData().getEdges()).hasSize(1);
    }

    @Test
    @DisplayName("getCourseGraph 委托 service 返回课程图谱")
    void getCourseGraph() {
        KnowledgeGraphResponse graph = KnowledgeGraphResponse.builder()
                .nodes(List.of(GraphNodeResponse.builder()
                        .id("Course:course-1")
                        .label("Course")
                        .name("测试课程")
                        .build()))
                .edges(List.of())
                .build();
        when(knowledgeGraphService.getCourseGraph("course-1")).thenReturn(graph);

        var result = controller.getCourseGraph("course-1");

        assertThat(result).isNotNull();
        assertThat(result.getData()).isNotNull();
        assertThat(result.getData().getNodes()).hasSize(1);
    }
}
