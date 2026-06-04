package com.rauio.smartdangjian.server.graph.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.groups.Tuple.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.neo4j.driver.Value;
import org.neo4j.driver.types.Node;
import org.neo4j.driver.types.Relationship;
import org.neo4j.driver.types.Type;
import org.springframework.data.neo4j.core.Neo4jClient;

import com.rauio.smartdangjian.exception.BusinessException;
import com.rauio.smartdangjian.server.chapter.pojo.entity.Chapter;
import com.rauio.smartdangjian.server.chapter.service.chapter.ChapterService;
import com.rauio.smartdangjian.server.course.pojo.entity.Course;
import com.rauio.smartdangjian.server.course.service.course.CourseService;
import com.rauio.smartdangjian.server.graph.pojo.response.GraphEdgeResponse;
import com.rauio.smartdangjian.server.graph.pojo.response.GraphNodeResponse;
import com.rauio.smartdangjian.server.graph.pojo.response.KnowledgeGraphResponse;
import com.rauio.smartdangjian.server.user.pojo.entity.User;
import com.rauio.smartdangjian.server.user.service.UserService;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class KnowledgeGraphServiceTest {

    @Mock
    private Neo4jClient neo4jClient;

    @Mock
    private UserService userService;

    @Mock
    private CourseService courseService;

    @Mock
    private ChapterService chapterService;

    @InjectMocks
    private KnowledgeGraphService knowledgeGraphService;

    private static final Long USER_ID = 1L;
    private static final Long USER_ID_NO_REAL_NAME = 2L;
    private static final Long CHAPTER_ID = 1L;
    private static final Long COURSE_ID = 1L;

    // ==================== Helper: Neo4jClient query chain mocks ====================

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

    // ==================== Helper: Neo4j Value mocks ====================

    private Value mockStringValue(String value) {
        Value val = mock(Value.class);
        Type type = mock(Type.class);
        when(val.isNull()).thenReturn(false);
        when(type.name()).thenReturn("STRING");
        when(val.type()).thenReturn(type);
        when(val.asString()).thenReturn(value);
        return val;
    }

    private Value mockIntegerValue(long value) {
        Value val = mock(Value.class);
        Type type = mock(Type.class);
        when(val.isNull()).thenReturn(false);
        when(type.name()).thenReturn("INTEGER");
        when(val.type()).thenReturn(type);
        when(val.asLong()).thenReturn(value);
        return val;
    }

    // ==================== Helper: Neo4j Node mocks ====================

    private Node mockUserNode(String id, String name) {
        Value idVal = mockStringValue(id);
        Value nameVal = mockStringValue(name);
        Node node = mock(Node.class);
        when(node.labels()).thenReturn(List.of("User"));
        when(node.containsKey("id")).thenReturn(true);
        when(node.get("id")).thenReturn(idVal);
        when(node.containsKey("name")).thenReturn(true);
        when(node.get("name")).thenReturn(nameVal);
        when(node.containsKey("title")).thenReturn(false);
        return node;
    }

    private Node mockUserNodeIntegerId(long id, String name) {
        Value idVal = mockIntegerValue(id);
        Value nameVal = mockStringValue(name);
        Node node = mock(Node.class);
        when(node.labels()).thenReturn(List.of("User"));
        when(node.containsKey("id")).thenReturn(true);
        when(node.get("id")).thenReturn(idVal);
        when(node.containsKey("name")).thenReturn(true);
        when(node.get("name")).thenReturn(nameVal);
        when(node.containsKey("title")).thenReturn(false);
        return node;
    }

    private Node mockCourseNode(String id, String title) {
        Value idVal = mockStringValue(id);
        Value titleVal = mockStringValue(title);
        Node node = mock(Node.class);
        when(node.labels()).thenReturn(List.of("Course"));
        when(node.containsKey("id")).thenReturn(true);
        when(node.get("id")).thenReturn(idVal);
        when(node.containsKey("name")).thenReturn(false);
        when(node.containsKey("title")).thenReturn(true);
        when(node.get("title")).thenReturn(titleVal);
        return node;
    }

    private Node mockChapterNode(String id, String title) {
        Value idVal = mockStringValue(id);
        Value titleVal = mockStringValue(title);
        Node node = mock(Node.class);
        when(node.labels()).thenReturn(List.of("Chapter"));
        when(node.containsKey("id")).thenReturn(true);
        when(node.get("id")).thenReturn(idVal);
        when(node.containsKey("name")).thenReturn(false);
        when(node.containsKey("title")).thenReturn(true);
        when(node.get("title")).thenReturn(titleVal);
        return node;
    }

    private Node mockNodeWithoutIdProperty(String label, long internalId) {
        Node node = mock(Node.class);
        when(node.labels()).thenReturn(List.of(label));
        when(node.id()).thenReturn(internalId);
        when(node.containsKey("id")).thenReturn(false);
        when(node.containsKey("name")).thenReturn(false);
        when(node.containsKey("title")).thenReturn(false);
        return node;
    }

    // ==================== Helper: Neo4j Relationship mocks ====================

    private Relationship mockRelationship(String type) {
        Relationship rel = mock(Relationship.class);
        when(rel.type()).thenReturn(type);
        return rel;
    }

    // ==================== Helper: Build test rows ====================

    private Map<String, Object> fullRow(
            String userId,
            String userName,
            String courseId,
            String courseTitle,
            String chapterId,
            String chapterTitle) {
        Map<String, Object> row = new HashMap<>();
        row.put("u", mockUserNode(userId, userName));
        row.put("c", mockCourseNode(courseId, courseTitle));
        row.put("ch", mockChapterNode(chapterId, chapterTitle));
        row.put("r1", mockRelationship("LEARNED"));
        row.put("r2", mockRelationship("HAS_CHAPTER"));
        row.put("r3", mockRelationship("LEARNED_CHAPTER"));
        return row;
    }

    private Map<String, Object> userOnlyRow(String userId, String userName) {
        Map<String, Object> row = new HashMap<>();
        row.put("u", mockUserNode(userId, userName));
        row.put("c", null);
        row.put("ch", null);
        row.put("r1", null);
        row.put("r2", null);
        row.put("r3", null);
        return row;
    }

    // ==================== NormalTests ====================

    @Nested
    @DisplayName("NormalTests — 正常路径")
    class NormalTests {

        @Test
        @DisplayName("upsertLearningGraph 成功创建图谱关系")
        void upsertLearningGraphSuccess() {
            User user = User.builder()
                    .id(USER_ID)
                    .username("zhangsan")
                    .realName("张三")
                    .build();
            Chapter chapter = Chapter.builder()
                    .id(CHAPTER_ID)
                    .courseId(COURSE_ID)
                    .title("第一章")
                    .build();
            Course course = Course.builder().id(COURSE_ID).title("测试课程").build();

            when(userService.getById(USER_ID)).thenReturn(user);
            when(chapterService.getById(CHAPTER_ID)).thenReturn(chapter);
            when(courseService.getById(COURSE_ID)).thenReturn(course);

            Neo4jClient.UnboundRunnableSpec spec = setupQueryChain();

            knowledgeGraphService.upsertLearningGraph(USER_ID, CHAPTER_ID);

            verify(neo4jClient).query(anyString());
            verify(spec, atLeast(5)).bind(any());
            verify(spec).run();
        }

        @Test
        @DisplayName("getUserGraph 返回包含用户/课程/章节的完整图谱")
        void getUserGraphWithFullData() {
            Neo4jClient.RecordFetchSpec<Map<String, Object>> fetchSpec = setupFetchChain();
            List<Map<String, Object>> rows = List.of(fullRow("1", "张三", "1", "测试课程", "1", "第一章"));
            when(fetchSpec.all()).thenReturn((Collection) rows);

            KnowledgeGraphResponse result = knowledgeGraphService.getUserGraph("1");

            assertThat(result).isNotNull();
            assertThat(result.getNodes()).hasSize(3);
            assertThat(result.getEdges()).hasSize(3);

            assertThat(result.getNodes())
                    .extracting(GraphNodeResponse::getId)
                    .containsExactlyInAnyOrder("User:1", "Course:1", "Chapter:1");
            assertThat(result.getNodes())
                    .extracting(GraphNodeResponse::getName)
                    .containsExactlyInAnyOrder("张三", "测试课程", "第一章");

            assertThat(result.getEdges())
                    .extracting(GraphEdgeResponse::getType)
                    .containsExactlyInAnyOrder("LEARNED", "HAS_CHAPTER", "LEARNED_CHAPTER");
            assertThat(result.getEdges())
                    .extracting(GraphEdgeResponse::getSource, GraphEdgeResponse::getTarget, GraphEdgeResponse::getType)
                    .containsExactlyInAnyOrder(
                            tuple("User:1", "Course:1", "LEARNED"),
                            tuple("Course:1", "Chapter:1", "HAS_CHAPTER"),
                            tuple("User:1", "Chapter:1", "LEARNED_CHAPTER"));
        }

        @Test
        @DisplayName("getCourseGraph 返回包含课程/用户/章节的完整图谱")
        void getCourseGraphWithFullData() {
            Neo4jClient.RecordFetchSpec<Map<String, Object>> fetchSpec = setupFetchChain();
            Map<String, Object> row = new HashMap<>();
            row.put("c", mockCourseNode("1", "测试课程"));
            row.put("u", mockUserNode("1", "张三"));
            row.put("ch", mockChapterNode("1", "第一章"));
            row.put("r1", mockRelationship("LEARNED"));
            row.put("r2", mockRelationship("HAS_CHAPTER"));
            row.put("r3", null);
            List<Map<String, Object>> rows = List.of(row);
            when(fetchSpec.all()).thenReturn((Collection) rows);

            KnowledgeGraphResponse result = knowledgeGraphService.getCourseGraph("1");

            assertThat(result).isNotNull();
            assertThat(result.getNodes()).hasSize(3);
            assertThat(result.getEdges()).hasSize(2);
            assertThat(result.getEdges())
                    .extracting(GraphEdgeResponse::getType)
                    .containsExactlyInAnyOrder("LEARNED", "HAS_CHAPTER");
            assertThat(result.getEdges())
                    .extracting(GraphEdgeResponse::getSource, GraphEdgeResponse::getTarget, GraphEdgeResponse::getType)
                    .containsExactlyInAnyOrder(
                            tuple("User:1", "Course:1", "LEARNED"), tuple("Course:1", "Chapter:1", "HAS_CHAPTER"));
        }

        @Test
        @DisplayName("getUserGraph 处理 INTEGER 类型节点 ID 和 name 回退到 title")
        void getUserGraphWithIntegerId() {
            Neo4jClient.RecordFetchSpec<Map<String, Object>> fetchSpec = setupFetchChain();
            Node userNode = mockUserNodeIntegerId(1L, "张三");
            Node courseNode = mockCourseNode("1", "测试课程");
            Node chapterNode = mockChapterNode("1", "第一章");

            Map<String, Object> row = new HashMap<>();
            row.put("u", userNode);
            row.put("c", courseNode);
            row.put("ch", chapterNode);
            row.put("r1", mockRelationship("LEARNED"));
            row.put("r2", mockRelationship("HAS_CHAPTER"));
            row.put("r3", mockRelationship("LEARNED_CHAPTER"));
            when(fetchSpec.all()).thenReturn((Collection) List.of(row));

            KnowledgeGraphResponse result = knowledgeGraphService.getUserGraph("1");

            assertThat(result).isNotNull();
            assertThat(result.getNodes()).hasSize(3);
            assertThat(result.getNodes())
                    .filteredOn(n -> n.getLabel().equals("User"))
                    .singleElement()
                    .extracting(GraphNodeResponse::getId)
                    .isEqualTo("User:1");
        }

        @Test
        @DisplayName("upsertLearningGraph 当 realName 为 null 时使用 username")
        void upsertLearningGraphWithUsernameFallback() {
            User user = User.builder()
                    .id(USER_ID_NO_REAL_NAME)
                    .username("lisi")
                    .realName(null)
                    .build();
            Chapter chapter = Chapter.builder()
                    .id(CHAPTER_ID)
                    .courseId(COURSE_ID)
                    .title("第一章")
                    .build();
            Course course = Course.builder().id(COURSE_ID).title("测试课程").build();

            when(userService.getById(USER_ID_NO_REAL_NAME)).thenReturn(user);
            when(chapterService.getById(CHAPTER_ID)).thenReturn(chapter);
            when(courseService.getById(COURSE_ID)).thenReturn(course);

            Neo4jClient.UnboundRunnableSpec spec = setupQueryChain();

            knowledgeGraphService.upsertLearningGraph(USER_ID_NO_REAL_NAME, CHAPTER_ID);

            verify(spec).run();
        }
    }

    // ==================== ErrorTests ====================

    @Nested
    @DisplayName("ErrorTests — 异常路径")
    class ErrorTests {

        @Test
        @DisplayName("upsertLearningGraph 用户不存在抛出 USER_NOT_FOUND")
        void upsertLearningGraphUserNotFound() {
            when(userService.getById(USER_ID)).thenReturn(null);

            assertThatThrownBy(() -> knowledgeGraphService.upsertLearningGraph(USER_ID, CHAPTER_ID))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("用户不存在");

            verify(neo4jClient, never()).query(anyString());
        }

        @Test
        @DisplayName("upsertLearningGraph 章节不存在抛出 CHAPTER_NOT_FOUND")
        void upsertLearningGraphChapterNotFound() {
            when(userService.getById(USER_ID))
                    .thenReturn(User.builder().id(USER_ID).username("test").build());
            when(chapterService.getById(CHAPTER_ID)).thenReturn(null);

            assertThatThrownBy(() -> knowledgeGraphService.upsertLearningGraph(USER_ID, CHAPTER_ID))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("章节不存在");

            verify(neo4jClient, never()).query(anyString());
        }

        @Test
        @DisplayName("upsertLearningGraph 课程不存在抛出 COURSE_NOT_FOUND")
        void upsertLearningGraphCourseNotFound() {
            when(userService.getById(USER_ID))
                    .thenReturn(User.builder().id(USER_ID).username("test").build());
            when(chapterService.getById(CHAPTER_ID))
                    .thenReturn(
                            Chapter.builder().id(CHAPTER_ID).courseId(COURSE_ID).build());
            when(courseService.getById(COURSE_ID)).thenReturn(null);

            assertThatThrownBy(() -> knowledgeGraphService.upsertLearningGraph(USER_ID, CHAPTER_ID))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("课程不存在");

            verify(neo4jClient, never()).query(anyString());
        }
    }

    // ==================== BoundaryTests ====================

    @Nested
    @DisplayName("BoundaryTests — 边界情况")
    class BoundaryTests {

        @Test
        @DisplayName("getUserGraph 空结果返回空图谱")
        void getUserGraphEmpty() {
            Neo4jClient.RecordFetchSpec<Map<String, Object>> fetchSpec = setupFetchChain();
            when(fetchSpec.all()).thenReturn(List.of());

            KnowledgeGraphResponse result = knowledgeGraphService.getUserGraph("1");

            assertThat(result).isNotNull();
            assertThat(result.getNodes()).isEmpty();
            assertThat(result.getEdges()).isEmpty();
        }

        @Test
        @DisplayName("getCourseGraph 空结果返回空图谱")
        void getCourseGraphEmpty() {
            Neo4jClient.RecordFetchSpec<Map<String, Object>> fetchSpec = setupFetchChain();
            when(fetchSpec.all()).thenReturn(List.of());

            KnowledgeGraphResponse result = knowledgeGraphService.getCourseGraph("1");

            assertThat(result).isNotNull();
            assertThat(result.getNodes()).isEmpty();
            assertThat(result.getEdges()).isEmpty();
        }

        @Test
        @DisplayName("getUserGraph 只有用户节点时返回单节点图谱")
        void getUserGraphSingleNode() {
            Neo4jClient.RecordFetchSpec<Map<String, Object>> fetchSpec = setupFetchChain();
            List<Map<String, Object>> rows = List.of(userOnlyRow("1", "张三"));
            when(fetchSpec.all()).thenReturn((Collection) rows);

            KnowledgeGraphResponse result = knowledgeGraphService.getUserGraph("1");

            assertThat(result).isNotNull();
            assertThat(result.getNodes()).hasSize(1);
            assertThat(result.getNodes())
                    .singleElement()
                    .extracting(GraphNodeResponse::getId, GraphNodeResponse::getLabel)
                    .containsExactly("User:1", "User");
            assertThat(result.getEdges()).isEmpty();
        }

        @Test
        @DisplayName("getUserGraph 同一用户学习多个课程产生多条关系")
        void getUserGraphChainRelationships() {
            Neo4jClient.RecordFetchSpec<Map<String, Object>> fetchSpec = setupFetchChain();

            Map<String, Object> row1 = fullRow("1", "张三", "1", "测试课程", "1", "第一章");
            Map<String, Object> row2 = fullRow("1", "张三", "1", "测试课程", "2", "第二章");
            when(fetchSpec.all()).thenReturn((Collection) List.of(row1, row2));

            KnowledgeGraphResponse result = knowledgeGraphService.getUserGraph("1");

            assertThat(result).isNotNull();
            assertThat(result.getNodes()).hasSize(4);
            assertThat(result.getEdges()).hasSize(5);

            assertThat(result.getNodes())
                    .extracting(GraphNodeResponse::getId)
                    .containsExactlyInAnyOrder("User:1", "Course:1", "Chapter:1", "Chapter:2");
            assertThat(result.getEdges())
                    .extracting(GraphEdgeResponse::getSource, GraphEdgeResponse::getTarget, GraphEdgeResponse::getType)
                    .containsExactlyInAnyOrder(
                            tuple("User:1", "Course:1", "LEARNED"),
                            tuple("Course:1", "Chapter:1", "HAS_CHAPTER"),
                            tuple("User:1", "Chapter:1", "LEARNED_CHAPTER"),
                            tuple("Course:1", "Chapter:2", "HAS_CHAPTER"),
                            tuple("User:1", "Chapter:2", "LEARNED_CHAPTER"));
        }

        @Test
        @DisplayName("getUserGraph 节点没有 id 属性时回退到内部 node.id()")
        void getUserGraphNodeWithoutIdProperty() {
            Neo4jClient.RecordFetchSpec<Map<String, Object>> fetchSpec = setupFetchChain();
            Map<String, Object> row = new HashMap<>();
            row.put("u", mockNodeWithoutIdProperty("User", 100L));
            row.put("c", null);
            row.put("ch", null);
            row.put("r1", null);
            row.put("r2", null);
            row.put("r3", null);
            when(fetchSpec.all()).thenReturn((Collection) List.of(row));

            KnowledgeGraphResponse result = knowledgeGraphService.getUserGraph("1");

            assertThat(result).isNotNull();
            assertThat(result.getNodes()).hasSize(1);
            assertThat(result.getNodes())
                    .singleElement()
                    .extracting(GraphNodeResponse::getId)
                    .isEqualTo("User:100");
        }

        @Test
        @DisplayName("getUserGraph 节点 id 为 null 时回退到内部 node.id()")
        void getUserGraphNodeWithNullIdProperty() {
            Neo4jClient.RecordFetchSpec<Map<String, Object>> fetchSpec = setupFetchChain();
            Value nullIdVal = mock(Value.class);
            when(nullIdVal.isNull()).thenReturn(true);
            Node node = mock(Node.class);
            when(node.labels()).thenReturn(List.of("User"));
            when(node.id()).thenReturn(200L);
            when(node.containsKey("id")).thenReturn(true);
            when(node.get("id")).thenReturn(nullIdVal);

            Map<String, Object> row = new HashMap<>();
            row.put("u", node);
            row.put("c", null);
            row.put("ch", null);
            row.put("r1", null);
            row.put("r2", null);
            row.put("r3", null);
            when(fetchSpec.all()).thenReturn((Collection) List.of(row));

            KnowledgeGraphResponse result = knowledgeGraphService.getUserGraph("1");

            assertThat(result).isNotNull();
            assertThat(result.getNodes()).hasSize(1);
            assertThat(result.getNodes())
                    .singleElement()
                    .extracting(GraphNodeResponse::getId)
                    .isEqualTo("User:200");
        }

        @Test
        @DisplayName("getUserGraph 节点标签为空时使用默认标签 Node")
        void getUserGraphNodeWithEmptyLabels() {
            Neo4jClient.RecordFetchSpec<Map<String, Object>> fetchSpec = setupFetchChain();
            Node node = mock(Node.class);
            when(node.labels()).thenReturn(List.of());
            when(node.containsKey("id")).thenReturn(false);
            when(node.containsKey("name")).thenReturn(false);
            when(node.containsKey("title")).thenReturn(false);
            when(node.id()).thenReturn(300L);

            Map<String, Object> row = new HashMap<>();
            row.put("u", node);
            row.put("c", null);
            row.put("ch", null);
            row.put("r1", null);
            row.put("r2", null);
            row.put("r3", null);
            when(fetchSpec.all()).thenReturn((Collection) List.of(row));

            KnowledgeGraphResponse result = knowledgeGraphService.getUserGraph("1");

            assertThat(result).isNotNull();
            assertThat(result.getNodes()).hasSize(1);
            assertThat(result.getNodes())
                    .singleElement()
                    .extracting(GraphNodeResponse::getId, GraphNodeResponse::getLabel)
                    .containsExactly("Node:300", "Node");
        }

        @Test
        @DisplayName("getUserGraph 关系类型为 null 时 addEdge 仍可处理")
        void getUserGraphWithNullRelationshipType() {
            Neo4jClient.RecordFetchSpec<Map<String, Object>> fetchSpec = setupFetchChain();
            Map<String, Object> row = new HashMap<>();
            row.put("u", mockUserNode("1", "张三"));
            row.put("c", mockCourseNode("1", "测试课程"));
            row.put("ch", mockChapterNode("1", "第一章"));
            Relationship rel = mock(Relationship.class);
            when(rel.type()).thenReturn(null);
            row.put("r1", rel);
            row.put("r2", mockRelationship("HAS_CHAPTER"));
            row.put("r3", mockRelationship("LEARNED_CHAPTER"));
            when(fetchSpec.all()).thenReturn((Collection) List.of(row));

            KnowledgeGraphResponse result = knowledgeGraphService.getUserGraph("1");

            assertThat(result).isNotNull();
            assertThat(result.getNodes()).hasSize(3);
            assertThat(result.getEdges()).hasSize(3);
        }

        @Test
        @DisplayName("getUserGraph 节点没有 name 和 title 时使用 id 作为回退名称")
        void getUserGraphNodeNameFallbackToId() {
            Neo4jClient.RecordFetchSpec<Map<String, Object>> fetchSpec = setupFetchChain();
            Value idVal = mockStringValue("42");
            Node node = mock(Node.class);
            when(node.labels()).thenReturn(List.of("User"));
            when(node.containsKey("id")).thenReturn(true);
            when(node.get("id")).thenReturn(idVal);
            when(node.containsKey("name")).thenReturn(false);
            when(node.containsKey("title")).thenReturn(false);

            Map<String, Object> row = new HashMap<>();
            row.put("u", node);
            row.put("c", null);
            row.put("ch", null);
            row.put("r1", null);
            row.put("r2", null);
            row.put("r3", null);
            when(fetchSpec.all()).thenReturn((Collection) List.of(row));

            KnowledgeGraphResponse result = knowledgeGraphService.getUserGraph("1");

            assertThat(result).isNotNull();
            assertThat(result.getNodes()).hasSize(1);
            assertThat(result.getNodes())
                    .singleElement()
                    .extracting(GraphNodeResponse::getName)
                    .isEqualTo("42");
        }

        @Test
        @DisplayName("buildGraph 处理空行时不报错")
        void buildGraphWithEmptyRows() {
            Neo4jClient.RecordFetchSpec<Map<String, Object>> fetchSpec = setupFetchChain();
            Map<String, Object> emptyRow = new HashMap<>();
            emptyRow.put("u", null);
            emptyRow.put("c", null);
            emptyRow.put("ch", null);
            emptyRow.put("r1", null);
            emptyRow.put("r2", null);
            emptyRow.put("r3", null);
            when(fetchSpec.all()).thenReturn((Collection) List.of(emptyRow));

            KnowledgeGraphResponse result = knowledgeGraphService.getUserGraph("1");

            assertThat(result).isNotNull();
            assertThat(result.getNodes()).isEmpty();
            assertThat(result.getEdges()).isEmpty();
        }

        @Test
        @DisplayName("getUserGraph readName with name key null value uses title")
        void getUserGraphReadNameNameNullUsesTitle() {
            Neo4jClient.RecordFetchSpec<Map<String, Object>> fetchSpec = setupFetchChain();
            Value nameNullVal = mock(Value.class);
            lenient().when(nameNullVal.isNull()).thenReturn(true);

            Value idVal = mockStringValue("1");
            Value titleVal = mockStringValue("课程名称");
            Node node = mock(Node.class);
            lenient().when(node.labels()).thenReturn(List.of("Course"));
            lenient().when(node.containsKey("id")).thenReturn(true);
            lenient().when(node.get("id")).thenReturn(idVal);
            lenient().when(node.containsKey("name")).thenReturn(true);
            lenient().when(node.get("name")).thenReturn(nameNullVal);
            lenient().when(node.containsKey("title")).thenReturn(true);
            lenient().when(node.get("title")).thenReturn(titleVal);

            Map<String, Object> row = new HashMap<>();
            row.put("c", node);
            row.put("u", null);
            row.put("ch", null);
            row.put("r1", null);
            row.put("r2", null);
            row.put("r3", null);
            lenient().when(fetchSpec.all()).thenReturn((Collection) List.of(row));

            KnowledgeGraphResponse result = knowledgeGraphService.getCourseGraph("1");

            assertThat(result).isNotNull();
            assertThat(result.getNodes()).hasSize(1);
            assertThat(result.getNodes())
                    .singleElement()
                    .extracting(GraphNodeResponse::getName)
                    .isEqualTo("课程名称");
        }

        @Test
        @DisplayName("getUserGraph readName with title key null value uses fallback")
        void getUserGraphReadNameTitleNullUsesFallback() {
            Neo4jClient.RecordFetchSpec<Map<String, Object>> fetchSpec = setupFetchChain();
            Value titleNullVal = mock(Value.class);
            lenient().when(titleNullVal.isNull()).thenReturn(true);

            Value idVal = mockStringValue("1");
            Node node = mock(Node.class);
            lenient().when(node.labels()).thenReturn(List.of("Course"));
            lenient().when(node.containsKey("id")).thenReturn(true);
            lenient().when(node.get("id")).thenReturn(idVal);
            lenient().when(node.containsKey("name")).thenReturn(false);
            lenient().when(node.containsKey("title")).thenReturn(true);
            lenient().when(node.get("title")).thenReturn(titleNullVal);

            Map<String, Object> row = new HashMap<>();
            row.put("c", node);
            row.put("u", null);
            row.put("ch", null);
            row.put("r1", null);
            row.put("r2", null);
            row.put("r3", null);
            lenient().when(fetchSpec.all()).thenReturn((Collection) List.of(row));

            KnowledgeGraphResponse result = knowledgeGraphService.getCourseGraph("1");

            assertThat(result).isNotNull();
            assertThat(result.getNodes()).hasSize(1);
            assertThat(result.getNodes())
                    .singleElement()
                    .extracting(GraphNodeResponse::getName)
                    .isEqualTo("1");
        }

        @Test
        @DisplayName("getUserGraph asRelationship 处理非 Relationship 对象")
        void getUserGraphWithNonRelationshipValue() {
            Neo4jClient.RecordFetchSpec<Map<String, Object>> fetchSpec = setupFetchChain();
            Map<String, Object> row = new HashMap<>();
            row.put("u", mockUserNode("1", "张三"));
            row.put("c", mockCourseNode("1", "测试课程"));
            row.put("ch", mockChapterNode("1", "第一章"));
            row.put("r1", "not-a-relationship");
            row.put("r2", mockRelationship("HAS_CHAPTER"));
            row.put("r3", mockRelationship("LEARNED_CHAPTER"));
            when(fetchSpec.all()).thenReturn((Collection) List.of(row));

            KnowledgeGraphResponse result = knowledgeGraphService.getUserGraph("1");

            assertThat(result).isNotNull();
            assertThat(result.getNodes()).hasSize(3);
            assertThat(result.getEdges()).hasSize(2);
            assertThat(result.getEdges())
                    .extracting(GraphEdgeResponse::getType)
                    .containsExactlyInAnyOrder("HAS_CHAPTER", "LEARNED_CHAPTER");
        }

        @Test
        @DisplayName("getUserGraph r2 为 null 但课程和章节存在时不添加 HAS_CHAPTER 边")
        void getUserGraphWithNullR2() {
            Neo4jClient.RecordFetchSpec<Map<String, Object>> fetchSpec = setupFetchChain();
            Map<String, Object> row = new HashMap<>();
            row.put("u", mockUserNode("1", "张三"));
            row.put("c", mockCourseNode("1", "测试课程"));
            row.put("ch", mockChapterNode("1", "第一章"));
            row.put("r1", mockRelationship("LEARNED"));
            row.put("r2", null);
            row.put("r3", mockRelationship("LEARNED_CHAPTER"));
            when(fetchSpec.all()).thenReturn((Collection) List.of(row));

            KnowledgeGraphResponse result = knowledgeGraphService.getUserGraph("1");

            assertThat(result).isNotNull();
            assertThat(result.getNodes()).hasSize(3);
            assertThat(result.getEdges()).hasSize(2);
            assertThat(result.getEdges())
                    .extracting(GraphEdgeResponse::getType)
                    .containsExactlyInAnyOrder("LEARNED", "LEARNED_CHAPTER");
        }

        @Test
        @DisplayName("getUserGraph value 非 Relationship 时 asRelationship 返回 null 跳过边")
        void getUserGraphAllValuesNonRelationship() {
            Neo4jClient.RecordFetchSpec<Map<String, Object>> fetchSpec = setupFetchChain();
            Map<String, Object> row = new HashMap<>();
            row.put("u", mockUserNode("1", "张三"));
            row.put("c", mockCourseNode("1", "测试课程"));
            row.put("ch", mockChapterNode("1", "第一章"));
            row.put("r1", "not-a-rel");
            row.put("r2", "also-not-a-rel");
            row.put("r3", "still-not-a-rel");
            when(fetchSpec.all()).thenReturn((Collection) List.of(row));

            KnowledgeGraphResponse result = knowledgeGraphService.getUserGraph("1");

            assertThat(result).isNotNull();
            assertThat(result.getNodes()).hasSize(3);
            assertThat(result.getEdges()).isEmpty();
        }

        @Test
        @DisplayName("getUserGraph 仅有 HAS_CHAPTER 和 LEARNED_CHAPTER 边时正确构建")
        void getUserGraphWithR2AndR3Only() {
            Neo4jClient.RecordFetchSpec<Map<String, Object>> fetchSpec = setupFetchChain();
            Map<String, Object> row = new HashMap<>();
            row.put("u", mockUserNode("1", "张三"));
            row.put("c", mockCourseNode("1", "测试课程"));
            row.put("ch", mockChapterNode("1", "第一章"));
            row.put("r1", null);
            row.put("r2", mockRelationship("HAS_CHAPTER"));
            row.put("r3", mockRelationship("LEARNED_CHAPTER"));
            when(fetchSpec.all()).thenReturn((Collection) List.of(row));

            KnowledgeGraphResponse result = knowledgeGraphService.getUserGraph("1");

            assertThat(result).isNotNull();
            assertThat(result.getNodes()).hasSize(3);
            assertThat(result.getEdges()).hasSize(2);
            assertThat(result.getEdges())
                    .extracting(GraphEdgeResponse::getType)
                    .containsExactlyInAnyOrder("HAS_CHAPTER", "LEARNED_CHAPTER");
        }

        @Test
        @DisplayName("getUserGraph 节点缺少 id 属性时回退到内部 id")
        void getUserGraphNodeWithoutIdExtended() {
            Neo4jClient.RecordFetchSpec<Map<String, Object>> fetchSpec = setupFetchChain();
            Map<String, Object> row = new HashMap<>();
            Node node = mock(Node.class);
            when(node.labels()).thenReturn(List.of("User"));
            when(node.id()).thenReturn(999L);
            when(node.containsKey("id")).thenReturn(false);
            when(node.containsKey("name")).thenReturn(true);
            Value nameVal = mockStringValue("无名氏");
            when(node.get("name")).thenReturn(nameVal);
            when(node.containsKey("title")).thenReturn(false);

            row.put("u", node);
            row.put("c", null);
            row.put("ch", null);
            row.put("r1", null);
            row.put("r2", null);
            row.put("r3", null);
            when(fetchSpec.all()).thenReturn((Collection) List.of(row));

            KnowledgeGraphResponse result = knowledgeGraphService.getUserGraph("1");

            assertThat(result).isNotNull();
            assertThat(result.getNodes()).hasSize(1);
            assertThat(result.getNodes())
                    .singleElement()
                    .extracting(GraphNodeResponse::getId)
                    .isEqualTo("User:999");
        }

        @Test
        @DisplayName("getUserGraph 节点 id 为 STRING 类型时使用 asString 而非 asLong")
        void getUserGraphNodeWithStringTypedId() {
            Neo4jClient.RecordFetchSpec<Map<String, Object>> fetchSpec = setupFetchChain();
            Map<String, Object> row = new HashMap<>();
            Value stringId = mockStringValue("str-001");
            Node node = mock(Node.class);
            when(node.labels()).thenReturn(List.of("User"));
            when(node.containsKey("id")).thenReturn(true);
            when(node.get("id")).thenReturn(stringId);
            when(node.containsKey("name")).thenReturn(false);
            when(node.containsKey("title")).thenReturn(true);
            Value titleVal = mockStringValue("string-id-user");
            when(node.get("title")).thenReturn(titleVal);

            row.put("u", node);
            row.put("c", null);
            row.put("ch", null);
            row.put("r1", null);
            row.put("r2", null);
            row.put("r3", null);
            when(fetchSpec.all()).thenReturn((Collection) List.of(row));

            KnowledgeGraphResponse result = knowledgeGraphService.getUserGraph("1");

            assertThat(result).isNotNull();
            assertThat(result.getNodes()).hasSize(1);
            assertThat(result.getNodes())
                    .singleElement()
                    .extracting(GraphNodeResponse::getId)
                    .isEqualTo("User:str-001");
        }

        @Test
        @DisplayName("buildGraph partial short-circuit: r1/r2/r3 with missing intermediate nodes")
        void buildGraphPartialShortCircuit() {
            Neo4jClient.RecordFetchSpec<Map<String, Object>> fetchSpec = setupFetchChain();

            Map<String, Object> row1 = new HashMap<>();
            row1.put("u", null);
            row1.put("c", null);
            row1.put("ch", mockChapterNode("1", "Chapter 1"));
            row1.put("r1", mockRelationship("LEARNED"));
            row1.put("r2", mockRelationship("HAS_CHAPTER"));
            row1.put("r3", mockRelationship("LEARNED_CHAPTER"));

            Map<String, Object> row2 = new HashMap<>();
            row2.put("u", mockUserNode("1", "User"));
            row2.put("c", null);
            row2.put("ch", null);
            row2.put("r1", mockRelationship("LEARNED"));
            row2.put("r2", mockRelationship("HAS_CHAPTER"));
            row2.put("r3", mockRelationship("LEARNED_CHAPTER"));

            Map<String, Object> row3 = new HashMap<>();
            row3.put("u", null);
            row3.put("c", mockCourseNode("1", "Course"));
            row3.put("ch", null);
            row3.put("r1", null);
            row3.put("r2", mockRelationship("HAS_CHAPTER"));
            row3.put("r3", null);

            when(fetchSpec.all()).thenReturn((Collection) List.of(row1, row2, row3));

            KnowledgeGraphResponse result = knowledgeGraphService.getUserGraph("1");

            assertThat(result).isNotNull();
            assertThat(result.getNodes()).hasSize(3);
            assertThat(result.getEdges()).isEmpty();
        }
    }
}
