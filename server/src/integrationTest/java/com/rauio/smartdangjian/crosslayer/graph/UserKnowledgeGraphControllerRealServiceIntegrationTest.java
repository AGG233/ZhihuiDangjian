package com.rauio.smartdangjian.crosslayer.graph;

import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionStatus;

import com.rauio.smartdangjian.crosslayer.CrossLayerTestBase;
import com.rauio.smartdangjian.server.graph.controller.user.UserKnowledgeGraphController;
import com.rauio.smartdangjian.server.graph.pojo.response.GraphEdgeResponse;
import com.rauio.smartdangjian.server.graph.pojo.response.GraphNodeResponse;
import com.rauio.smartdangjian.server.graph.pojo.response.KnowledgeGraphResponse;
import com.rauio.smartdangjian.server.graph.service.KnowledgeGraphService;

@SpringBootTest(classes = UserKnowledgeGraphControllerRealServiceIntegrationTest.TestConfig.class)
@DisplayName("知识图谱控制层集成测试")
class UserKnowledgeGraphControllerRealServiceIntegrationTest extends CrossLayerTestBase {

    @MockitoBean
    private KnowledgeGraphService knowledgeGraphService;

    @BeforeEach
    void setUp() {
        reset(knowledgeGraphService);
        setStudentContext(1L, "uni-1");
    }

    @Test
    @DisplayName("GET /api/graph/knowledge-graphs/users/{userId} 成功返回用户学习图谱")
    void getUserGraph() throws Exception {
        KnowledgeGraphResponse response = KnowledgeGraphResponse.builder()
                .nodes(List.of(GraphNodeResponse.builder()
                        .id("User:1")
                        .label("User")
                        .name("张三")
                        .build()))
                .edges(List.of())
                .build();
        when(knowledgeGraphService.getUserGraph("1")).thenReturn(response);

        mockMvc.perform(get("/api/graph/knowledge-graphs/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.data.nodes[0].id").value("User:1"))
                .andExpect(jsonPath("$.data.nodes[0].label").value("User"))
                .andExpect(jsonPath("$.data.edges").isEmpty());

        verify(knowledgeGraphService).getUserGraph("1");
    }

    @Test
    @DisplayName("GET /api/graph/knowledge-graphs/courses/{courseId} 成功返回课程图谱")
    void getCourseGraph() throws Exception {
        KnowledgeGraphResponse response = KnowledgeGraphResponse.builder()
                .nodes(List.of(
                        GraphNodeResponse.builder()
                                .id("Course:1")
                                .label("Course")
                                .name("党史课程")
                                .build(),
                        GraphNodeResponse.builder()
                                .id("Chapter:10")
                                .label("Chapter")
                                .name("第一章")
                                .build()))
                .edges(List.of(GraphEdgeResponse.builder()
                        .source("Course:1")
                        .target("Chapter:10")
                        .type("HAS_CHAPTER")
                        .build()))
                .build();
        when(knowledgeGraphService.getCourseGraph("1")).thenReturn(response);

        mockMvc.perform(get("/api/graph/knowledge-graphs/courses/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.data.nodes.length()").value(2))
                .andExpect(jsonPath("$.data.edges[0].type").value("HAS_CHAPTER"));

        verify(knowledgeGraphService).getCourseGraph("1");
    }

    @SpringBootConfiguration
    static class TestConfig extends CrossLayerTestConfig {

        @Bean
        UserKnowledgeGraphController userKnowledgeGraphController(KnowledgeGraphService knowledgeGraphService) {
            return new UserKnowledgeGraphController(knowledgeGraphService);
        }

        @Bean
        AbstractPlatformTransactionManager transactionManager() {
            return new AbstractPlatformTransactionManager() {
                @Override
                protected Object doGetTransaction() {
                    return new Object();
                }

                @Override
                protected void doBegin(Object transaction, TransactionDefinition definition) {}

                @Override
                protected void doCommit(DefaultTransactionStatus status) {}

                @Override
                protected void doRollback(DefaultTransactionStatus status) {}
            };
        }
    }
}
