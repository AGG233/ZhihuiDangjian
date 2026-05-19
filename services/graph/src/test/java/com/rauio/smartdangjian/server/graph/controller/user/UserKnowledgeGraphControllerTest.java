package com.rauio.smartdangjian.server.graph.controller.user;

import com.rauio.smartdangjian.server.graph.pojo.vo.GraphEdgeVO;
import com.rauio.smartdangjian.server.graph.pojo.vo.GraphNodeVO;
import com.rauio.smartdangjian.server.graph.pojo.vo.KnowledgeGraphVO;
import com.rauio.smartdangjian.server.graph.service.KnowledgeGraphService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserKnowledgeGraphControllerTest {

    @Mock
    private KnowledgeGraphService knowledgeGraphService;

    @InjectMocks
    private UserKnowledgeGraphController controller;

    @Test
    @DisplayName("getUserGraph 委托 service 返回用户学习图谱")
    void getUserGraph() {
        KnowledgeGraphVO graph = KnowledgeGraphVO.builder()
                .nodes(List.of(GraphNodeVO.builder().id("User:user-1").label("User").name("张三").build()))
                .edges(List.of(GraphEdgeVO.builder().source("User:user-1").target("Course:course-1").type("LEARNED").build()))
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
        KnowledgeGraphVO graph = KnowledgeGraphVO.builder()
                .nodes(List.of(GraphNodeVO.builder().id("Course:course-1").label("Course").name("测试课程").build()))
                .edges(List.of())
                .build();
        when(knowledgeGraphService.getCourseGraph("course-1")).thenReturn(graph);

        var result = controller.getCourseGraph("course-1");

        assertThat(result).isNotNull();
        assertThat(result.getData()).isNotNull();
        assertThat(result.getData().getNodes()).hasSize(1);
    }
}
