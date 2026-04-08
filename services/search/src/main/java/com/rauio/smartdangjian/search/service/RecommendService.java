package com.rauio.smartdangjian.search.service;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rauio.smartdangjian.server.content.mapper.ChapterMapper;
import com.rauio.smartdangjian.server.content.mapper.CategoryCourseMapper;
import com.rauio.smartdangjian.server.content.mapper.CourseMapper;
import com.rauio.smartdangjian.server.content.pojo.entity.Chapter;
import com.rauio.smartdangjian.server.content.pojo.entity.CategoryCourse;
import com.rauio.smartdangjian.server.content.pojo.entity.Course;
import com.rauio.smartdangjian.server.learning.mapper.UserChapterProgressMapper;
import com.rauio.smartdangjian.server.learning.mapper.UserLearningRecordMapper;
import com.rauio.smartdangjian.server.learning.pojo.dto.UserBehaviorDto;
import com.rauio.smartdangjian.server.learning.pojo.entity.UserChapterProgress;
import com.rauio.smartdangjian.server.learning.pojo.entity.UserLearningRecord;
import com.rauio.smartdangjian.server.user.mapper.UserSimilarityMapper;
import com.rauio.smartdangjian.server.user.pojo.entity.UserSimilarity;
import com.rauio.smartdangjian.server.user.service.UserSimilarityService;
import com.rauio.smartdangjian.search.pojo.vo.UserProfileVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecommendService {

    private static final int TOP_N_NEIGHBORS = 20;

    private final UserLearningRecordMapper userLearningRecordMapper;
    private final UserChapterProgressMapper userChapterProgressMapper;
    private final UserSimilarityMapper userSimilarityMapper;
    private final ChapterMapper chapterMapper;
    private final CategoryCourseMapper categoryCourseMapper;
    private final CourseMapper courseMapper;
    private final UserSimilarityService userSimilarityService;
    private final Neo4jClient neo4jClient;
    private final UserProfileService userProfileService;

    // ==================== 综合推荐 ====================

    /**
     * 综合推荐：融合协同过滤、知识图谱、画像推荐结果
     */
    public Page<String> recommend(String userId, int pageNum, int pageSize) {
        List<ScoredItem> merged = new ArrayList<>();

        // 协同过滤 (权重 0.4)
        Page<String> cfPage = recommendByCF(userId, 1, pageSize);
        for (int i = 0; i < cfPage.getRecords().size(); i++) {
            merged.add(new ScoredItem(cfPage.getRecords().get(i), 0.4 * (pageSize - i)));
        }

        // 知识图谱推荐 (权重 0.3)
        Page<String> graphPage = recommendByGraph(userId, 1, pageSize);
        for (int i = 0; i < graphPage.getRecords().size(); i++) {
            merged.add(new ScoredItem(graphPage.getRecords().get(i), 0.3 * (pageSize - i)));
        }

        // 画像推荐 (权重 0.3)
        Page<String> profilePage = recommendByProfile(userId, 1, pageSize);
        for (int i = 0; i < profilePage.getRecords().size(); i++) {
            merged.add(new ScoredItem(profilePage.getRecords().get(i), 0.3 * (pageSize - i)));
        }

        // 合并相同 ID 的分数并排序
        Map<String, Double> scoreMap = new HashMap<>();
        for (ScoredItem item : merged) {
            scoreMap.merge(item.id, item.score, Double::sum);
        }

        List<String> sorted = scoreMap.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .map(Map.Entry::getKey)
                .toList();

        return paginate(sorted, pageNum, pageSize);
    }

    // ==================== 协同过滤推荐 ====================

    public Page<String> recommendByCF(String userId, int pageNum, int pageSize) {
        int neighborSize = 10;
        Page<UserSimilarity> similarityPage = userSimilarityMapper.selectPage(
                new Page<>(1, neighborSize),
                new LambdaQueryWrapper<UserSimilarity>()
                        .eq(UserSimilarity::getUserId1, userId)
                        .orderByDesc(UserSimilarity::getSimilarityScore)
        );

        List<UserSimilarity> similarityList = similarityPage.getRecords();
        if (similarityList.isEmpty()) {
            return new Page<>(pageNum, pageSize);
        }

        List<String> similarUserIds = similarityList.stream()
                .map(UserSimilarity::getUserId2)
                .collect(Collectors.toList());

        Set<String> userLearnedCourseIds = getLearnedCourseIdsByUserId(userId);

        List<UserLearningRecord> records = userLearningRecordMapper.selectList(
                new LambdaQueryWrapper<UserLearningRecord>()
                        .in(UserLearningRecord::getUserId, similarUserIds)
                        .select(UserLearningRecord::getChapterId, UserLearningRecord::getUserId)
        );
        List<UserChapterProgress> progresses = userChapterProgressMapper.selectList(
                new LambdaQueryWrapper<UserChapterProgress>()
                        .in(UserChapterProgress::getUserId, similarUserIds)
                        .select(UserChapterProgress::getChapterId, UserChapterProgress::getUserId)
        );

        Set<String> allInvolvedChapterIds = new HashSet<>();
        allInvolvedChapterIds.addAll(records.stream().map(UserLearningRecord::getChapterId).toList());
        allInvolvedChapterIds.addAll(progresses.stream().map(UserChapterProgress::getChapterId).toList());

        if (allInvolvedChapterIds.isEmpty()) {
            return new Page<>(pageNum, pageSize);
        }

        List<Chapter> chapters = chapterMapper.selectByIds(allInvolvedChapterIds);
        Map<String, String> chapterToCourseMap = chapters.stream()
                .collect(Collectors.toMap(Chapter::getId, Chapter::getCourseId));

        Map<String, Double> courseScoreMap = new HashMap<>();

        Consumer<String> addScore = (chapterId) -> {
            String courseId = chapterToCourseMap.get(chapterId);
            if (courseId != null && !userLearnedCourseIds.contains(courseId)) {
                courseScoreMap.merge(courseId, 1.0, Double::sum);
            }
        };

        records.forEach(r -> addScore.accept(r.getChapterId()));
        progresses.forEach(p -> addScore.accept(p.getChapterId()));

        List<String> sorted = courseScoreMap.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .map(Map.Entry::getKey)
                .toList();

        return paginate(sorted, pageNum, pageSize);
    }

    // ==================== 知识图谱推荐 ====================

    /**
     * 基于知识图谱推荐：查找相似用户学过但当前用户未学的课程
     */
    public Page<String> recommendByGraph(String userId, int pageNum, int pageSize) {
        String cypher = """
                MATCH (me:User {id: $userId})-[:LEARNED]->(c1:Course)
                MATCH (other:User)-[:LEARNED]->(c1)
                MATCH (other)-[:LEARNED]->(c2:Course)
                WHERE NOT (me)-[:LEARNED]->(c2)
                RETURN c2.id AS courseId, count(DISTINCT other) AS score
                ORDER BY score DESC
                """;

        List<String> sorted = new ArrayList<>();
        sorted.addAll(neo4jClient.query(cypher)
                .bind(userId).to("userId")
                .fetchAs(String.class)
                .mappedBy((type, record) -> record.get("courseId").asString())
                .all());

        return paginate(sorted, pageNum, pageSize);
    }

    // ==================== 画像推荐 ====================

    /**
     * 基于用户画像的内容推荐：根据兴趣分类和知识水平推荐课程
     */
    public Page<String> recommendByProfile(String userId, int pageNum, int pageSize) {
        UserProfileVO profile = userProfileService.getProfile(userId);
        if (profile == null) {
            return new Page<>(pageNum, pageSize);
        }

        // 获取用户已学的课程 ID，排除
        Set<String> learnedCourseIds = getLearnedCourseIdsByUserId(userId);

        LambdaQueryWrapper<Course> wrapper = new LambdaQueryWrapper<Course>()
                .eq(Course::getIsPublished, true)
                .notIn(!learnedCourseIds.isEmpty(), Course::getId, learnedCourseIds);

        // 优先推荐兴趣分类
        List<String> interestIds = profile.getInterestCategoryIds();
        if (interestIds != null && !interestIds.isEmpty()) {
            List<String> matchedCourseIds = categoryCourseMapper.selectList(new LambdaQueryWrapper<CategoryCourse>()
                            .in(CategoryCourse::getCategoryId, interestIds))
                    .stream()
                    .map(CategoryCourse::getCourseId)
                    .filter(Objects::nonNull)
                    .distinct()
                    .toList();
            if (matchedCourseIds.isEmpty()) {
                return new Page<>(pageNum, pageSize);
            }
            wrapper.in(Course::getId, matchedCourseIds);
        }

        // 根据答题正确率推荐适合难度
        if (profile.getQuiz() != null && profile.getQuiz().getCorrectRate() > 0) {
            double rate = profile.getQuiz().getCorrectRate();
            String suitableDifficulty = rate > 0.8 ? "hard" : rate > 0.5 ? "medium" : "easy";
            wrapper.eq(Course::getDifficulty, suitableDifficulty);
        }

        wrapper.orderByDesc(Course::getEnrollmentCount);

        Page<Course> coursePage = courseMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
        List<String> courseIds = coursePage.getRecords().stream()
                .map(Course::getId)
                .toList();

        Page<String> result = new Page<>(pageNum, pageSize);
        result.setTotal(coursePage.getTotal());
        result.setRecords(courseIds);
        return result;
    }

    // ==================== 相似度计算（定时任务） ====================

    @Scheduled(cron = "0 0 2 * * ?")
    @Transactional
    protected void calculateSimilarity() {
        List<UserBehaviorDto> allBehaviors = userLearningRecordMapper.getAllUserBehaviors();
        if (allBehaviors.isEmpty()) return;

        Map<String, Set<String>> userItemMap = allBehaviors.stream()
                .collect(Collectors.groupingBy(
                        UserBehaviorDto::getUserId,
                        Collectors.mapping(UserBehaviorDto::getChapterId, Collectors.toSet())
                ));

        Map<String, List<String>> itemUserMap = allBehaviors.stream()
                .collect(Collectors.groupingBy(
                        UserBehaviorDto::getChapterId,
                        Collectors.mapping(UserBehaviorDto::getUserId, Collectors.toList())
                ));

        Map<String, Map<String, Integer>> coOccurrenceMap = new HashMap<>();

        for (Map.Entry<String, List<String>> entry : itemUserMap.entrySet()) {
            List<String> userList = entry.getValue();
            if (userList.size() < 2) continue;

            for (int i = 0; i < userList.size(); i++) {
                String u1 = userList.get(i);
                for (int j = i + 1; j < userList.size(); j++) {
                    String u2 = userList.get(j);
                    coOccurrenceMap.computeIfAbsent(u1, k -> new HashMap<>())
                            .merge(u2, 1, Integer::sum);
                    coOccurrenceMap.computeIfAbsent(u2, k -> new HashMap<>())
                            .merge(u1, 1, Integer::sum);
                }
            }
        }

        // 清除旧的相似度数据
        userSimilarityMapper.delete(new LambdaQueryWrapper<>());

        List<UserSimilarity> buffer = new ArrayList<>();
        for (Map.Entry<String, Map<String, Integer>> entry : coOccurrenceMap.entrySet()) {
            String userId = entry.getKey();
            Map<String, Integer> relatedUsers = entry.getValue();
            double userVectorLen = Math.sqrt(userItemMap.get(userId).size());

            PriorityQueue<UserSimilarity> topQueue = new PriorityQueue<>(
                    Comparator.comparing(UserSimilarity::getSimilarityScore)
            );

            for (Map.Entry<String, Integer> relatedEntry : relatedUsers.entrySet()) {
                String relatedUserId = relatedEntry.getKey();
                int count = relatedEntry.getValue();
                double relatedUserVectorLen = Math.sqrt(userItemMap.get(relatedUserId).size());
                double score = count / (userVectorLen * relatedUserVectorLen);

                if (score < 0.1) continue;
                BigDecimal similarityScore = BigDecimal.valueOf(score);

                UserSimilarity sim = UserSimilarity.builder()
                        .userId1(userId)
                        .userId2(relatedUserId)
                        .similarityScore(similarityScore)
                        .isValid(true)
                        .calculatedAt(java.time.LocalDateTime.now())
                        .build();

                if (topQueue.size() < TOP_N_NEIGHBORS) {
                    topQueue.offer(sim);
                } else if (similarityScore.compareTo(topQueue.peek().getSimilarityScore()) > 0) {
                    topQueue.poll();
                    topQueue.offer(sim);
                }
            }
            buffer.addAll(topQueue);
            if (buffer.size() > 1000) {
                userSimilarityService.saveBatch(buffer);
                buffer.clear();
            }
        }
        if (!buffer.isEmpty()) {
            userSimilarityService.saveBatch(buffer);
        }
    }

    // ==================== 工具方法 ====================

    private Set<String> getLearnedCourseIdsByUserId(String userId) {
        List<String> chapterIds = userLearningRecordMapper.selectList(
                        new LambdaQueryWrapper<UserLearningRecord>()
                                .eq(UserLearningRecord::getUserId, userId)
                                .select(UserLearningRecord::getChapterId)
                ).stream()
                .map(UserLearningRecord::getChapterId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        if (chapterIds.isEmpty()) {
            return Collections.emptySet();
        }
        return chapterMapper.selectList(
                        new LambdaQueryWrapper<Chapter>()
                                .in(Chapter::getId, chapterIds)
                                .select(Chapter::getCourseId)
                ).stream()
                .map(Chapter::getCourseId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    private Page<String> paginate(List<String> sorted, int pageNum, int pageSize) {
        Page<String> result = new Page<>(pageNum, pageSize);
        result.setTotal(sorted.size());
        int fromIndex = (pageNum - 1) * pageSize;
        if (fromIndex >= sorted.size()) {
            result.setRecords(Collections.emptyList());
        } else {
            result.setRecords(sorted.subList(fromIndex, Math.min(fromIndex + pageSize, sorted.size())));
        }
        return result;
    }

    private record ScoredItem(String id, double score) {}
}
