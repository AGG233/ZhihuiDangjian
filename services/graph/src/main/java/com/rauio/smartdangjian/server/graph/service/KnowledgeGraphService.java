package com.rauio.smartdangjian.server.graph.service;

import java.util.*;
import java.util.stream.Collectors;

import org.neo4j.driver.Value;
import org.neo4j.driver.types.Node;
import org.neo4j.driver.types.Relationship;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.stereotype.Service;

import com.rauio.smartdangjian.exception.BusinessException;
import com.rauio.smartdangjian.server.content.pojo.entity.Chapter;
import com.rauio.smartdangjian.server.content.pojo.entity.Course;
import com.rauio.smartdangjian.server.content.service.chapter.ChapterService;
import com.rauio.smartdangjian.server.content.service.course.CourseService;
import com.rauio.smartdangjian.server.graph.constants.GraphErrorConstants;
import com.rauio.smartdangjian.server.graph.pojo.response.GraphEdgeResponse;
import com.rauio.smartdangjian.server.graph.pojo.response.GraphNodeResponse;
import com.rauio.smartdangjian.server.graph.pojo.response.KnowledgeGraphResponse;
import com.rauio.smartdangjian.server.user.pojo.entity.User;
import com.rauio.smartdangjian.server.user.service.UserService;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class KnowledgeGraphService {

    private final Neo4jClient neo4jClient;
    private final UserService userService;
    private final CourseService courseService;
    private final ChapterService chapterService;

    /**
     * 批量将用户学习章节的关系写入知识图谱（预加载实体版本）。
     * 避免 N+1 查询，调用方需预加载 User/Chapter/Course。
     *
     * @param user 已加载的用户实体
     * @param chapter 已加载的章节实体
     * @param course 已加载的课程实体
     */
    @CircuitBreaker(name = "neo4jService", fallbackMethod = "upsertWithEntitiesFallback")
    public void upsertLearningGraph(User user, Chapter chapter, Course course) {
        if (user == null || chapter == null || course == null) {
            log.warn("跳过图谱写入：User/Chapter/Course 存在空值");
            return;
        }

        String userName = user.getRealName() != null ? user.getRealName() : user.getUsername();
        String cypher =
                """
                MERGE (u:User {id:$userId})
                SET u.name = $userName
                MERGE (c:Course {id:$courseId})
                SET c.title = $courseTitle
                MERGE (ch:Chapter {id:$chapterId})
                SET ch.title = $chapterTitle
                MERGE (u)-[:LEARNED]->(c)
                MERGE (c)-[:HAS_CHAPTER]->(ch)
                MERGE (u)-[:LEARNED_CHAPTER]->(ch)
                """;

        neo4jClient
                .query(cypher)
                .bind(user.getId())
                .to("userId")
                .bind(userName)
                .to("userName")
                .bind(course.getId())
                .to("courseId")
                .bind(course.getTitle())
                .to("courseTitle")
                .bind(chapter.getId())
                .to("chapterId")
                .bind(chapter.getTitle())
                .to("chapterTitle")
                .run();
    }

    /**
     * 批量将用户学习章节关系写入知识图谱。
     * 内部批量预加载 User/Chapter/Course，避免遍历时 N+1 查询。
     *
     * @param userId 用户 ID
     * @param chapterIds 章节 ID 列表
     */
    public void batchUpsertLearningGraph(Long userId, List<Long> chapterIds) {
        if (userId == null || chapterIds == null || chapterIds.isEmpty()) {
            return;
        }

        User user = userService.getById(userId);
        if (user == null) {
            log.warn("批量图谱写入跳过：用户不存在 userId={}", userId);
            return;
        }

        List<Chapter> chapters = chapterService.listByIds(chapterIds);
        if (chapters.isEmpty()) {
            return;
        }

        Set<Long> courseIds = chapters.stream()
                .map(Chapter::getCourseId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Long, Course> courseMap =
                courseService.listByIds(courseIds).stream().collect(Collectors.toMap(Course::getId, c -> c));

        for (Chapter chapter : chapters) {
            if (chapter.getCourseId() == null) {
                continue;
            }
            Course course = courseMap.get(chapter.getCourseId());
            if (course == null) {
                continue;
            }
            upsertLearningGraph(user, chapter, course);
        }
    }

    /**
     * 将用户学习章节的关系写入知识图谱。
     * Neo4j 操作受熔断保护。
     */
    @CircuitBreaker(name = "neo4jService", fallbackMethod = "upsertLearningGraphFallback")
    public void upsertLearningGraph(Long userId, Long chapterId) {
        User user = userService.getById(userId);
        if (user == null) {
            throw new BusinessException(GraphErrorConstants.USER_NOT_FOUND, "用户不存在");
        }
        Chapter chapter = chapterService.getById(chapterId);
        if (chapter == null) {
            throw new BusinessException(GraphErrorConstants.CHAPTER_NOT_FOUND, "章节不存在");
        }
        Course course = courseService.getById(chapter.getCourseId());
        if (course == null) {
            throw new BusinessException(GraphErrorConstants.COURSE_NOT_FOUND, "课程不存在");
        }

        String userName = user.getRealName() != null ? user.getRealName() : user.getUsername();
        String cypher =
                """
                MERGE (u:User {id:$userId})
                SET u.name = $userName
                MERGE (c:Course {id:$courseId})
                SET c.title = $courseTitle
                MERGE (ch:Chapter {id:$chapterId})
                SET ch.title = $chapterTitle
                MERGE (u)-[:LEARNED]->(c)
                MERGE (c)-[:HAS_CHAPTER]->(ch)
                MERGE (u)-[:LEARNED_CHAPTER]->(ch)
                """;

        neo4jClient
                .query(cypher)
                .bind(userId)
                .to("userId")
                .bind(userName)
                .to("userName")
                .bind(course.getId())
                .to("courseId")
                .bind(course.getTitle())
                .to("courseTitle")
                .bind(chapter.getId())
                .to("chapterId")
                .bind(chapter.getTitle())
                .to("chapterTitle")
                .run();
    }

    /**
     * 获取用户维度的知识图谱。
     * Neo4j 查询受熔断保护。
     */
    @CircuitBreaker(name = "neo4jService", fallbackMethod = "getUserGraphFallback")
    public KnowledgeGraphResponse getUserGraph(String userId) {
        String cypher =
                """
                MATCH (u:User {id:$userId})
                OPTIONAL MATCH (u)-[r1:LEARNED]->(c:Course)
                OPTIONAL MATCH (c)-[r2:HAS_CHAPTER]->(ch:Chapter)
                OPTIONAL MATCH (u)-[r3:LEARNED_CHAPTER]->(ch)
                RETURN u, c, ch, r1, r2, r3
                """;

        List<Map<String, Object>> rows = (List<Map<String, Object>>)
                neo4jClient.query(cypher).bind(userId).to("userId").fetch().all();

        return buildGraph(rows);
    }

    /**
     * 获取课程维度的知识图谱。
     *
     * @param courseId 课程 ID
     * @return 知识图谱结果
     */
    public KnowledgeGraphResponse getCourseGraph(String courseId) {
        String cypher =
                """
                MATCH (c:Course {id:$courseId})
                OPTIONAL MATCH (c)<-[r1:LEARNED]-(u:User)
                OPTIONAL MATCH (c)-[r2:HAS_CHAPTER]->(ch:Chapter)
                RETURN c, u, ch, r1, r2
                """;

        List<Map<String, Object>> rows = (List<Map<String, Object>>)
                neo4jClient.query(cypher).bind(courseId).to("courseId").fetch().all();

        return buildGraph(rows);
    }

    /**
     * 将 Neo4j 查询结果转换为前端图谱结构。
     *
     * @param rows 查询结果行
     * @return 图谱视图对象
     */
    private KnowledgeGraphResponse buildGraph(List<Map<String, Object>> rows) {
        Map<String, GraphNodeResponse> nodeMap = new LinkedHashMap<>();
        Set<String> edgeKeys = new LinkedHashSet<>();
        List<GraphEdgeResponse> edges = new ArrayList<>();

        for (Map<String, Object> row : rows) {
            Node userNode = asNode(row.get("u"));
            Node courseNode = asNode(row.get("c"));
            Node chapterNode = asNode(row.get("ch"));

            Relationship r1 = asRelationship(row.get("r1"));
            Relationship r2 = asRelationship(row.get("r2"));
            Relationship r3 = asRelationship(row.get("r3"));

            String userKey = addNode(nodeMap, userNode);
            String courseKey = addNode(nodeMap, courseNode);
            String chapterKey = addNode(nodeMap, chapterNode);

            if (r1 != null && userKey != null && courseKey != null) {
                addEdge(edgeKeys, edges, userKey, courseKey, r1.type());
            }
            if (r2 != null && courseKey != null && chapterKey != null) {
                addEdge(edgeKeys, edges, courseKey, chapterKey, r2.type());
            }
            if (r3 != null && userKey != null && chapterKey != null) {
                addEdge(edgeKeys, edges, userKey, chapterKey, r3.type());
            }
        }

        return KnowledgeGraphResponse.builder()
                .nodes(new ArrayList<>(nodeMap.values()))
                .edges(edges)
                .build();
    }

    /**
     * 将对象安全转换为 Neo4j 节点。
     *
     * @param value 原始对象
     * @return 节点对象
     */
    private Node asNode(Object value) {
        if (value instanceof Node node) {
            return node;
        }
        return null;
    }

    /**
     * 将对象安全转换为 Neo4j 关系。
     *
     * @param value 原始对象
     * @return 关系对象
     */
    private Relationship asRelationship(Object value) {
        if (value instanceof Relationship relationship) {
            return relationship;
        }
        return null;
    }

    /**
     * 向图谱节点映射中注册节点。
     *
     * @param nodeMap 节点映射
     * @param node Neo4j 节点
     * @return 节点唯一键
     */
    private String addNode(Map<String, GraphNodeResponse> nodeMap, Node node) {
        if (node == null) {
            return null;
        }
        String label =
                node.labels().iterator().hasNext() ? node.labels().iterator().next() : "Node";
        String id = readId(node);
        String key = label + ":" + id;
        if (!nodeMap.containsKey(key)) {
            nodeMap.put(
                    key,
                    GraphNodeResponse.builder()
                            .id(key)
                            .label(label)
                            .name(readName(node, id))
                            .build());
        }
        return key;
    }

    /**
     * 读取节点业务 ID。
     *
     * @param node Neo4j 节点
     * @return 节点 ID
     */
    private String readId(Node node) {
        if (node.containsKey("id") && !node.get("id").isNull()) {
            Value value = node.get("id");
            if ("INTEGER".equals(value.type().name())) {
                return String.valueOf(value.asLong());
            }
            return value.asString();
        }
        return String.valueOf(node.id());
    }

    /**
     * 读取节点展示名称。
     *
     * @param node Neo4j 节点
     * @param fallback 回退名称
     * @return 节点名称
     */
    private String readName(Node node, String fallback) {
        if (node.containsKey("name") && !node.get("name").isNull()) {
            return node.get("name").asString();
        }
        if (node.containsKey("title") && !node.get("title").isNull()) {
            return node.get("title").asString();
        }
        return fallback;
    }

    /**
     * 去重并追加图谱边。
     *
     * @param edgeKeys 边去重键集合
     * @param edges 边列表
     * @param source 源节点键
     * @param target 目标节点键
     * @param type 边类型
     */
    private void addEdge(
            Set<String> edgeKeys, List<GraphEdgeResponse> edges, String source, String target, String type) {
        String key = source + "|" + type + "|" + target;
        if (edgeKeys.add(key)) {
            edges.add(GraphEdgeResponse.builder()
                    .source(source)
                    .target(target)
                    .type(type)
                    .build());
        }
    }

    /**
     * Neo4j 批量写入熔断降级回退方法。
     */
    @SuppressWarnings("unused")
    private void upsertWithEntitiesFallback(User user, Chapter chapter, Course course, Throwable t) {
        log.error(
                "Neo4j 图谱写入失败（实体版本），触发熔断降级: userId={}, chapterId={}",
                user != null ? user.getId() : null,
                chapter != null ? chapter.getId() : null,
                t);
        throw new BusinessException(GraphErrorConstants.LEARNING_GRAPH_SYNC_FAILED, "知识图谱服务暂时不可用，请稍后重试");
    }

    /**
     * Neo4j 图谱写入熔断降级回退方法。
     */
    @SuppressWarnings("unused")
    private void upsertLearningGraphFallback(Long userId, Long chapterId, Throwable t) {
        log.error("Neo4j 图谱写入失败，触发熔断降级: userId={}, chapterId={}", userId, chapterId, t);
        throw new BusinessException(GraphErrorConstants.LEARNING_GRAPH_SYNC_FAILED, "知识图谱服务暂时不可用，请稍后重试");
    }

    /**
     * Neo4j 图谱查询熔断降级回退方法。
     */
    @SuppressWarnings("unused")
    private KnowledgeGraphResponse getUserGraphFallback(String userId, Throwable t) {
        log.error("Neo4j 用户图谱查询失败，触发熔断降级: userId={}", userId, t);
        return KnowledgeGraphResponse.builder()
                .nodes(List.of())
                .edges(List.of())
                .build();
    }
}
