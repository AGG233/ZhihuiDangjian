package com.rauio.smartdangjian.crosslayer.ai;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.neo4j.driver.Values;
import org.neo4j.driver.types.Node;
import org.neo4j.driver.types.Relationship;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.rauio.smartdangjian.crosslayer.CrossLayerTestBase;
import com.rauio.smartdangjian.server.ai.tool.GraphEvaluationTool;
import com.rauio.smartdangjian.server.content.mapper.ChapterMapper;
import com.rauio.smartdangjian.server.content.mapper.CourseMapper;
import com.rauio.smartdangjian.server.content.pojo.entity.Chapter;
import com.rauio.smartdangjian.server.content.pojo.entity.Course;
import com.rauio.smartdangjian.server.graph.service.KnowledgeGraphService;
import com.rauio.smartdangjian.server.user.mapper.UserMapper;
import com.rauio.smartdangjian.server.user.pojo.entity.User;
import com.rauio.smartdangjian.server.user.service.UserService;

/**
 * 知识图谱评价调用链跨层回归测试（模块文档 3.4）。
 *
 * <p>装配真实 {@link GraphEvaluationTool} + 真实 {@link KnowledgeGraphService}，
 * Neo4jClient 以 {@link MockitoBean} 提供（fluent 查询链 mock，行数据模拟 Neo4j 返回），
 * 验证 Tool → Service 的 Spring 装配与图谱摘要构建全链路。
 */
@SpringBootTest(classes = GraphEvaluationToolCrossLayerTest.TestConfig.class)
@TestPropertySource(properties = {"spring.ai.model.embedding=dashscope", "spring.ai.vectorstore.type=none"})
@DisplayName("知识图谱评价调用链跨层回归")
class GraphEvaluationToolCrossLayerTest extends CrossLayerTestBase {

    @MockitoBean
    private Neo4jClient neo4jClient;

    @MockitoBean
    private UserMapper userMapper;

    @MockitoBean
    private CourseMapper courseMapper;

    @MockitoBean
    private ChapterMapper chapterMapper;

    @MockitoBean
    private UserService userService;

    @Autowired
    private GraphEvaluationTool graphEvaluationTool;

    @BeforeAll
    static void initMybatisPlus() {
        MybatisConfiguration config = new MybatisConfiguration();
        MapperBuilderAssistant assistant = new MapperBuilderAssistant(config, "");
        TableInfoHelper.initTableInfo(assistant, User.class);
        TableInfoHelper.initTableInfo(assistant, Course.class);
        TableInfoHelper.initTableInfo(assistant, Chapter.class);
    }

    @SpringBootConfiguration
    static class TestConfig extends CrossLayerTestConfig {

        @Bean
        KnowledgeGraphService knowledgeGraphService(
                Neo4jClient neo4jClient,
                UserMapper userMapper,
                CourseMapper courseMapper,
                ChapterMapper chapterMapper) {
            return new KnowledgeGraphService(neo4jClient, userMapper, courseMapper, chapterMapper);
        }

        @Bean
        GraphEvaluationTool graphEvaluationTool(KnowledgeGraphService knowledgeGraphService, UserService userService) {
            return new GraphEvaluationTool(knowledgeGraphService, userService);
        }
    }

    private Neo4jClient.UnboundRunnableSpec setupQueryChain() {
        Neo4jClient.UnboundRunnableSpec spec = mock(Neo4jClient.UnboundRunnableSpec.class);
        Neo4jClient.OngoingBindSpec bindSpec = mock(Neo4jClient.OngoingBindSpec.class);
        lenient().when(neo4jClient.query(anyString())).thenReturn(spec);
        lenient().when(spec.bind(any())).thenReturn(bindSpec);
        lenient().when(bindSpec.to(anyString())).thenReturn(spec);
        return spec;
    }

    @SuppressWarnings("unchecked")
    private Neo4jClient.RecordFetchSpec<Map<String, Object>> setupFetchChain() {
        Neo4jClient.UnboundRunnableSpec spec = setupQueryChain();
        Neo4jClient.RecordFetchSpec<Map<String, Object>> fetchSpec = mock(Neo4jClient.RecordFetchSpec.class);
        lenient().when(spec.fetch()).thenReturn(fetchSpec);
        return fetchSpec;
    }

    @SuppressWarnings("unchecked")
    private void stubRows(Neo4jClient.RecordFetchSpec<Map<String, Object>> fetchSpec, List<Map<String, Object>> rows) {
        when(fetchSpec.all()).thenReturn((Collection) rows);
    }

    private Node mockNode(String label, String id, String name) {
        Node node = mock(Node.class);
        when(node.labels()).thenReturn(List.of(label));
        when(node.containsKey("id")).thenReturn(true);
        when(node.get("id")).thenReturn(Values.value(id));
        when(node.containsKey("name")).thenReturn(name != null);
        if (name != null) {
            when(node.get("name")).thenReturn(Values.value(name));
        }
        return node;
    }

    private Relationship mockRelationship(String type) {
        Relationship relationship = mock(Relationship.class);
        when(relationship.type()).thenReturn(type);
        return relationship;
    }

    @Test
    @DisplayName("Tool 经真实 Service 构建图谱摘要：节点/关系统计与名称列表")
    void toolBuildsGraphSummaryThroughRealService() {
        Neo4jClient.RecordFetchSpec<Map<String, Object>> fetchSpec = setupFetchChain();
        Map<String, Object> row = Map.of(
                "u", mockNode("User", "10001", "张三"),
                "c", mockNode("Course", "1", "二十大精神解读"),
                "ch", mockNode("Chapter", "10", "第一章 大会主题"),
                "r1", mockRelationship("LEARNED"),
                "r2", mockRelationship("HAS_CHAPTER"),
                "r3", mockRelationship("LEARNED_CHAPTER"));
        stubRows(fetchSpec, List.of(row));
        when(userService.getCurrentUserId()).thenReturn("10001");

        ToolContext toolContext = mock(ToolContext.class);
        Map<String, Object> summary = graphEvaluationTool.getUserKnowledgeGraph(toolContext);

        assertThat(summary)
                .containsEntry("userId", "10001")
                .containsEntry("nodeCount", 3)
                .containsEntry("edgeCount", 3)
                .containsEntry("courseCount", 1L)
                .containsEntry("chapterCount", 1L)
                .containsEntry("learnedChapterCount", 1L);
        assertThat((List<String>) summary.get("courseNames")).containsExactly("二十大精神解读");
        assertThat((List<String>) summary.get("chapterNames")).containsExactly("第一章 大会主题");
        assertThat((Map<String, Long>) summary.get("edgeTypeCounts"))
                .containsEntry("LEARNED", 1L)
                .containsEntry("HAS_CHAPTER", 1L)
                .containsEntry("LEARNED_CHAPTER", 1L);
    }

    @Test
    @DisplayName("空图谱：Tool 返回零计数摘要（全链路不抛错）")
    void emptyGraphReturnsZeroSummary() {
        Neo4jClient.RecordFetchSpec<Map<String, Object>> fetchSpec = setupFetchChain();
        when(fetchSpec.all()).thenReturn(List.of());
        when(userService.getCurrentUserId()).thenReturn("10002");

        ToolContext toolContext = mock(ToolContext.class);
        Map<String, Object> summary = graphEvaluationTool.getUserKnowledgeGraph(toolContext);

        assertThat(summary)
                .containsEntry("userId", "10002")
                .containsEntry("nodeCount", 0)
                .containsEntry("edgeCount", 0)
                .containsEntry("courseCount", 0L)
                .containsEntry("learnedChapterCount", 0L);
    }
}
