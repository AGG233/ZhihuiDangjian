package com.rauio.smartdangjian.service.graph;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.rauio.smartdangjian.dao.ChapterDao;
import com.rauio.smartdangjian.dao.CourseDao;
import com.rauio.smartdangjian.dao.UserDao;
import com.rauio.smartdangjian.exception.BusinessException;
import com.rauio.smartdangjian.mapper.UserLearningRecordMapper;
import com.rauio.smartdangjian.pojo.Chapter;
import com.rauio.smartdangjian.pojo.Course;
import com.rauio.smartdangjian.pojo.User;
import com.rauio.smartdangjian.pojo.UserLearningRecord;
import com.rauio.smartdangjian.pojo.vo.GraphEdgeVO;
import com.rauio.smartdangjian.pojo.vo.GraphNodeVO;
import com.rauio.smartdangjian.pojo.vo.KnowledgeGraphVO;
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
    private final UserDao userDao;
    private final CourseDao courseDao;
    private final ChapterDao chapterDao;
    private final UserLearningRecordMapper userLearningRecordMapper;

    public void upsertLearningGraph(Long userId, Long chapterId) {
        User user = userDao.get(userId);
        if (user == null) {
            throw new BusinessException(4000, "用户不存在");
        }
        Chapter chapter = chapterDao.getById(chapterId);
        if (chapter == null) {
            throw new BusinessException(4000, "章节不存在");
        }
        Course course = courseDao.get(chapter.getCourseId());
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

    public int syncUserLearningGraph(Long userId) {
        QueryWrapper<UserLearningRecord> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId);
        List<UserLearningRecord> records = userLearningRecordMapper.selectList(wrapper);
        for (UserLearningRecord record : records) {
            if (record.getUserId() != null && record.getChapterId() != null) {
                upsertLearningGraph(record.getUserId(), record.getChapterId());
            }
        }
        return records.size();
    }

    public KnowledgeGraphVO getUserGraph(Long userId) {
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

    public KnowledgeGraphVO getCourseGraph(Long courseId) {
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

    private Node asNode(Object value) {
        if (value instanceof Node node) {
            return node;
        }
        return null;
    }

    private Relationship asRelationship(Object value) {
        if (value instanceof Relationship relationship) {
            return relationship;
        }
        return null;
    }

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

    private String readName(Node node, String fallback) {
        if (node.containsKey("name") && !node.get("name").isNull()) {
            return node.get("name").asString();
        }
        if (node.containsKey("title") && !node.get("title").isNull()) {
            return node.get("title").asString();
        }
        return fallback;
    }

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
