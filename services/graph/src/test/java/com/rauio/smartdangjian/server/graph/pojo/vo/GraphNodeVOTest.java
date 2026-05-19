package com.rauio.smartdangjian.server.graph.pojo.vo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("GraphNodeVO 知识图谱节点视图对象")
class GraphNodeVOTest {

    @Test
    @DisplayName("使用 builder 构造 GraphNodeVO")
    void buildGraphNodeVO() {
        GraphNodeVO node = GraphNodeVO.builder()
                .id("User:user-1")
                .label("User")
                .name("张三")
                .build();

        assertThat(node.getId()).isEqualTo("User:user-1");
        assertThat(node.getLabel()).isEqualTo("User");
        assertThat(node.getName()).isEqualTo("张三");
    }
}
