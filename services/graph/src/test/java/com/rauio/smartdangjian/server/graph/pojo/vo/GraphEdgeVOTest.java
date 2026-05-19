package com.rauio.smartdangjian.server.graph.pojo.vo;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("GraphEdgeVO 知识图谱边视图对象")
class GraphEdgeVOTest {

    @Test
    @DisplayName("使用 builder 构造 GraphEdgeVO")
    void buildGraphEdgeVO() {
        GraphEdgeVO edge = GraphEdgeVO.builder()
                .source("User:user-1")
                .target("Course:course-1")
                .type("LEARNED")
                .build();

        assertThat(edge.getSource()).isEqualTo("User:user-1");
        assertThat(edge.getTarget()).isEqualTo("Course:course-1");
        assertThat(edge.getType()).isEqualTo("LEARNED");
    }
}
