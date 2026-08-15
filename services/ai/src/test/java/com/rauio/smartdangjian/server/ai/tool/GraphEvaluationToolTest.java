package com.rauio.smartdangjian.server.ai.tool;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.model.ToolContext;

import com.rauio.smartdangjian.exception.BusinessException;
import com.rauio.smartdangjian.server.ai.constants.AiErrorConstants;
import com.rauio.smartdangjian.server.graph.pojo.response.GraphEdgeResponse;
import com.rauio.smartdangjian.server.graph.pojo.response.GraphNodeResponse;
import com.rauio.smartdangjian.server.graph.pojo.response.KnowledgeGraphResponse;
import com.rauio.smartdangjian.server.graph.service.KnowledgeGraphService;
import com.rauio.smartdangjian.server.user.service.UserService;

@ExtendWith(MockitoExtension.class)
class GraphEvaluationToolTest {

    @Mock
    private KnowledgeGraphService knowledgeGraphService;

    @Mock
    private UserService userService;

    @InjectMocks
    private GraphEvaluationTool graphEvaluationTool;

    @Test
    @DisplayName("getUserKnowledgeGraph 返回图谱结构摘要（节点/关系统计与名称列表）")
    void getUserKnowledgeGraph() {
        ToolContext toolContext = new ToolContext(Map.of("userId", "10001"));

        KnowledgeGraphResponse graph = KnowledgeGraphResponse.builder()
                .nodes(List.of(
                        GraphNodeResponse.builder()
                                .id("User:10001")
                                .label("User")
                                .name("张三")
                                .build(),
                        GraphNodeResponse.builder()
                                .id("Course:1")
                                .label("Course")
                                .name("二十大精神解读")
                                .build(),
                        GraphNodeResponse.builder()
                                .id("Chapter:10")
                                .label("Chapter")
                                .name("第一章 大会主题")
                                .build()))
                .edges(List.of(
                        GraphEdgeResponse.builder()
                                .source("User:10001")
                                .target("Course:1")
                                .type("LEARNED")
                                .build(),
                        GraphEdgeResponse.builder()
                                .source("User:10001")
                                .target("Chapter:10")
                                .type("LEARNED_CHAPTER")
                                .build()))
                .build();
        when(knowledgeGraphService.getUserGraph("10001")).thenReturn(graph);

        Map<String, Object> summary = graphEvaluationTool.getUserKnowledgeGraph(toolContext);

        assertThat(summary)
                .containsEntry("userId", "10001")
                .containsEntry("nodeCount", 3)
                .containsEntry("edgeCount", 2)
                .containsEntry("courseCount", 1L)
                .containsEntry("chapterCount", 1L)
                .containsEntry("learnedChapterCount", 1L);
        assertThat((List<String>) summary.get("courseNames")).containsExactly("二十大精神解读");
        assertThat((List<String>) summary.get("chapterNames")).containsExactly("第一章 大会主题");
        assertThat((Map<String, Long>) summary.get("edgeTypeCounts"))
                .containsEntry("LEARNED", 1L)
                .containsEntry("LEARNED_CHAPTER", 1L);
    }

    @Test
    @DisplayName("空图谱返回零计数摘要（不抛错）")
    void emptyGraph() {
        ToolContext toolContext = new ToolContext(Map.of("userId", "10002"));
        when(knowledgeGraphService.getUserGraph("10002"))
                .thenReturn(KnowledgeGraphResponse.builder()
                        .nodes(List.of())
                        .edges(List.of())
                        .build());

        Map<String, Object> summary = graphEvaluationTool.getUserKnowledgeGraph(toolContext);

        assertThat(summary)
                .containsEntry("userId", "10002")
                .containsEntry("nodeCount", 0)
                .containsEntry("edgeCount", 0)
                .containsEntry("courseCount", 0L)
                .containsEntry("chapterCount", 0L)
                .containsEntry("learnedChapterCount", 0L);
        assertThat((List<String>) summary.get("courseNames")).isEmpty();
        assertThat((List<String>) summary.get("chapterNames")).isEmpty();
    }

    @Test
    @DisplayName("userId 缺失时抛出 USER_ID_REQUIRED 且不调用 Service")
    void missingUserIdThrowsBusinessException() {
        ToolContext toolContext = new ToolContext(Map.of());

        assertThatThrownBy(() -> graphEvaluationTool.getUserKnowledgeGraph(toolContext))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getCode())
                .isEqualTo(AiErrorConstants.USER_ID_REQUIRED);

        verify(knowledgeGraphService, never()).getUserGraph(anyString());
    }
}
