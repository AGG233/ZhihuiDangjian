package com.rauio.smartdangjian.server.graph.pojo.vo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("KnowledgeGraphVO 知识图谱视图对象")
class KnowledgeGraphVOTest {

    @Test
    @DisplayName("使用 builder 构造 KnowledgeGraphVO")
    void buildKnowledgeGraphVO() {
        GraphNodeVO node = GraphNodeVO.builder().id("User:user-1").label("User").name("张三").build();
        GraphEdgeVO edge = GraphEdgeVO.builder().source("User:user-1").target("Course:course-1").type("LEARNED").build();

        KnowledgeGraphVO graph = KnowledgeGraphVO.builder()
                .nodes(List.of(node))
                .edges(List.of(edge))
                .build();

        assertThat(graph.getNodes()).hasSize(1);
        assertThat(graph.getEdges()).hasSize(1);
        assertThat(graph.getNodes().get(0).getId()).isEqualTo("User:user-1");
        assertThat(graph.getEdges().get(0).getType()).isEqualTo("LEARNED");
    }

    @Test
    @DisplayName("KnowledgeGraphVO 默认空列表")
    void defaultEmptyLists() {
        KnowledgeGraphVO graph = KnowledgeGraphVO.builder().build();
        assertThat(graph.getNodes()).isNull();
        assertThat(graph.getEdges()).isNull();
    }
}
