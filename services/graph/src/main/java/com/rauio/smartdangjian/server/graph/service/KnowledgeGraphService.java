package com.rauio.smartdangjian.server.graph.service;

import com.rauio.smartdangjian.exception.BusinessException;
import com.rauio.smartdangjian.server.content.mapper.ChapterMapper;
import com.rauio.smartdangjian.server.content.mapper.CourseMapper;
import com.rauio.smartdangjian.server.user.mapper.UserMapper;
import com.rauio.smartdangjian.server.content.pojo.entity.Chapter;
import com.rauio.smartdangjian.server.content.pojo.entity.Course;
import com.rauio.smartdangjian.server.user.pojo.entity.User;
import com.rauio.smartdangjian.server.graph.pojo.vo.GraphEdgeVO;
import com.rauio.smartdangjian.server.graph.pojo.vo.GraphNodeVO;
import com.rauio.smartdangjian.server.graph.pojo.vo.KnowledgeGraphVO;
import lombok.RequiredArgsConstructor;
import org.neo4j.driver.Value;
import org.neo4j.driver.types.Node;
import org.neo4j.driver.types.Relationship;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class KnowledgeGraphService {

    private final Neo4jClient neo4jClient;
    private final UserMapper userMapper;
    private final CourseMapper courseMapper;
    private final ChapterMapper chapterMapper;

    /**
     * 将用户学习章节的关系写入知识图谱。
     *
     * @param userId 用户 ID
     * @param chapterId 章节 ID
     */
    public void upsertLearningGraph(String userId, String chapterId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(4000, "用户不存在");
        }
        Chapter chapter = chapterMapper.selectById(chapterId);
        if (chapter == null) {
            throw new BusinessException(4000, "章节不存在");
        }
        Course course = courseMapper.selectById(chapter.getCourseId());
        if (course == null) {
            throw new BusinessException(4000, "课程不存在");
        }

        String userName = user.getRealName() != null ? user.getRealName() : user.getUsername();
        String cypher = """
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

        neo4jClient.query(cypher)
                .bind(userId).to("userId")
                .bind(userName).to("userName")
                .bind(course.getId()).to("courseId")
                .bind(course.getTitle()).to("courseTitle")
                .bind(chapter.getId()).to("chapterId")
                .bind(chapter.getTitle()).to("chapterTitle")
                .run();
    }

    /**
     * 获取用户维度的知识图谱。
     *
     * @param userId 用户 ID
     * @return 知识图谱结果
     */
    public KnowledgeGraphVO getUserGraph(String userId) {
        String cypher = """
                MATCH (u:User {id:$userId})
                OPTIONAL MATCH (u)-[r1:LEARNED]->(c:Course)
                OPTIONAL MATCH (c)-[r2:HAS_CHAPTER]->(ch:Chapter)
                OPTIONAL MATCH (u)-[r3:LEARNED_CHAPTER]->(ch)
                RETURN u, c, ch, r1, r2, r3
                """;

        List<Map<String, Object>> rows = (List<Map<String, Object>>) neo4jClient.query(cypher)
                .bind(userId).to("userId")
                .fetch()
                .all();

        return buildGraph(rows);
    }

    /**
     * 获取课程维度的知识图谱。
     *
     * @param courseId 课程 ID
     * @return 知识图谱结果
     */
    public KnowledgeGraphVO getCourseGraph(String courseId) {
        String cypher = """
                MATCH (c:Course {id:$courseId})
                OPTIONAL MATCH (c)<-[r1:LEARNED]-(u:User)
                OPTIONAL MATCH (c)-[r2:HAS_CHAPTER]->(ch:Chapter)
                RETURN c, u, ch, r1, r2
                """;

        List<Map<String, Object>> rows = (List<Map<String, Object>>) neo4jClient.query(cypher)
                .bind(courseId).to("courseId")
                .fetch()
                .all();

        return buildGraph(rows);
    }

    /**
     * 将 Neo4j 查询结果转换为前端图谱结构。
     *
     * @param rows 查询结果行
     * @return 图谱视图对象
     */
    private KnowledgeGraphVO buildGraph(List<Map<String, Object>> rows) {
        Map<String, GraphNodeVO> nodeMap = new LinkedHashMap<>();
        Set<String> edgeKeys = new LinkedHashSet<>();
        List<GraphEdgeVO> edges = new ArrayList<>();

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

        return KnowledgeGraphVO.builder()
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
    private String addNode(Map<String, GraphNodeVO> nodeMap, Node node) {
        if (node == null) {
            return null;
        }
        String label = node.labels().iterator().hasNext() ? node.labels().iterator().next() : "Node";
        String id = readId(node);
        String key = label + ":" + id;
        if (!nodeMap.containsKey(key)) {
            nodeMap.put(key, GraphNodeVO.builder()
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
    private void addEdge(Set<String> edgeKeys, List<GraphEdgeVO> edges, String source, String target, String type) {
        String key = source + "|" + type + "|" + target;
        if (edgeKeys.add(key)) {
            edges.add(GraphEdgeVO.builder()
                    .source(source)
                    .target(target)
                    .type(type)
                    .build());
        }
    }
}
