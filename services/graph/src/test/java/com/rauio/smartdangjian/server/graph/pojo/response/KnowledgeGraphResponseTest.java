package com.rauio.smartdangjian.server.graph.pojo.response;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("KnowledgeGraphResponse 知识图谱视图对象")
class KnowledgeGraphResponseTest {

    @Test
    @DisplayName("使用 builder 构造 KnowledgeGraphResponse")
    void buildKnowledgeGraphResponse() {
        GraphNodeResponse node = GraphNodeResponse.builder()
                .id("User:user-1")
                .label("User")
                .name("张三")
                .build();
        GraphEdgeResponse edge = GraphEdgeResponse.builder()
                .source("User:user-1")
                .target("Course:course-1")
                .type("LEARNED")
                .build();

        KnowledgeGraphResponse graph = KnowledgeGraphResponse.builder()
                .nodes(List.of(node))
                .edges(List.of(edge))
                .build();

        assertThat(graph.getNodes()).hasSize(1);
        assertThat(graph.getEdges()).hasSize(1);
        assertThat(graph.getNodes().get(0).getId()).isEqualTo("User:user-1");
        assertThat(graph.getEdges().get(0).getType()).isEqualTo("LEARNED");
    }

    @Test
    @DisplayName("KnowledgeGraphResponse 默认空列表")
    void defaultEmptyLists() {
        KnowledgeGraphResponse graph = KnowledgeGraphResponse.builder().build();
        assertThat(graph.getNodes()).isNull();
        assertThat(graph.getEdges()).isNull();
    }
}
