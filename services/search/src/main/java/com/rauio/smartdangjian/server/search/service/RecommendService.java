package com.rauio.smartdangjian.server.search.service;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rauio.smartdangjian.server.content.service.chapter.ChapterService;
import com.rauio.smartdangjian.server.content.service.course.CourseService;
import com.rauio.smartdangjian.server.learning.pojo.dto.ChapterProgressSummaryDto;
import com.rauio.smartdangjian.server.learning.pojo.dto.LearningRecordSummaryDto;
import com.rauio.smartdangjian.server.learning.pojo.dto.UserBehaviorDto;
import com.rauio.smartdangjian.server.learning.service.UserChapterProgressService;
import com.rauio.smartdangjian.server.learning.service.UserLearningRecordService;
import com.rauio.smartdangjian.server.search.pojo.response.UserProfileResponse;
import com.rauio.smartdangjian.server.user.pojo.dto.UserSimilaritySummaryDto;
import com.rauio.smartdangjian.server.user.service.UserSimilarityService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecommendService {

    private static final int TOP_N_NEIGHBORS = 20;

    private final UserLearningRecordService learningRecordService;
    private final UserChapterProgressService chapterProgressService;
    private final ChapterService chapterService;
    private final CourseService courseService;
    private final UserSimilarityService userSimilarityService;
    private final Neo4jClient neo4jClient;
    private final UserProfileService userProfileService;

    // ==================== 综合推荐 ====================

    /**
     * 综合推荐：融合协同过滤、知识图谱、画像推荐结果
     */
    public Page<Long> recommend(Long userId, int pageNum, int pageSize) {
        List<ScoredItem> merged = new ArrayList<>();

        // 协同过滤 (权重 0.4)
        Page<Long> cfPage = recommendByCF(userId, 1, pageSize);
        for (int i = 0; i < cfPage.getRecords().size(); i++) {
            merged.add(new ScoredItem(cfPage.getRecords().get(i), 0.4 * (pageSize - i)));
        }

        // 知识图谱推荐 (权重 0.3)
        Page<Long> graphPage = recommendByGraph(userId, 1, pageSize);
        for (int i = 0; i < graphPage.getRecords().size(); i++) {
            merged.add(new ScoredItem(graphPage.getRecords().get(i), 0.3 * (pageSize - i)));
        }

        // 画像推荐 (权重 0.3)
        Page<Long> profilePage = recommendByProfile(userId, 1, pageSize);
        for (int i = 0; i < profilePage.getRecords().size(); i++) {
            merged.add(new ScoredItem(profilePage.getRecords().get(i), 0.3 * (pageSize - i)));
        }

        // 合并相同 ID 的分数并排序
        Map<Long, Double> scoreMap = new HashMap<>();
        for (ScoredItem item : merged) {
            scoreMap.merge(item.id, item.score, Double::sum);
        }

        List<Long> sorted = scoreMap.entrySet().stream()
                .sorted(Map.Entry.<Long, Double>comparingByValue().reversed())
                .map(Map.Entry::getKey)
                .toList();

        return paginate(sorted, pageNum, pageSize);
    }

    // ==================== 协同过滤推荐 ====================

    public Page<Long> recommendByCF(Long userId, int pageNum, int pageSize) {
        int neighborSize = 10;
        List<UserSimilaritySummaryDto> similarityList = userSimilarityService.listTopSimilarUsers(userId, neighborSize);
        if (similarityList.isEmpty()) {
            return new Page<>(pageNum, pageSize);
        }

        List<Long> similarUserIds =
                similarityList.stream().map(UserSimilaritySummaryDto::userId2).collect(Collectors.toList());

        Set<Long> userLearnedCourseIds = getLearnedCourseIdsByUserId(userId);

        List<LearningRecordSummaryDto> records =
                learningRecordService.listChapterRecordSummariesByUserIds(similarUserIds);
        List<ChapterProgressSummaryDto> progresses =
                chapterProgressService.listChapterProgressSummariesByUserIds(similarUserIds);

        Set<Long> allInvolvedChapterIds = new HashSet<>();
        allInvolvedChapterIds.addAll(
                records.stream().map(LearningRecordSummaryDto::chapterId).toList());
        allInvolvedChapterIds.addAll(
                progresses.stream().map(ChapterProgressSummaryDto::chapterId).toList());

        if (allInvolvedChapterIds.isEmpty()) {
            return new Page<>(pageNum, pageSize);
        }

        Map<Long, Long> chapterToCourseMap = chapterService.getCourseIdMapByChapterIds(allInvolvedChapterIds);

        Map<Long, Double> courseScoreMap = new HashMap<>();

        Consumer<Long> addScore = (chapterId) -> {
            Long courseId = chapterToCourseMap.get(chapterId);
            if (courseId != null && !userLearnedCourseIds.contains(courseId)) {
                courseScoreMap.merge(courseId, 1.0, Double::sum);
            }
        };

        records.forEach(r -> addScore.accept(r.chapterId()));
        progresses.forEach(p -> addScore.accept(p.chapterId()));

        List<Long> sorted = courseScoreMap.entrySet().stream()
                .sorted(Map.Entry.<Long, Double>comparingByValue().reversed())
                .map(Map.Entry::getKey)
                .toList();

        return paginate(sorted, pageNum, pageSize);
    }

    // ==================== 知识图谱推荐 ====================

    /**
     * 基于知识图谱推荐：查找相似用户学过但当前用户未学的课程
     */
    public Page<Long> recommendByGraph(Long userId, int pageNum, int pageSize) {
        String cypher =
                """
                MATCH (me:User {id: $userId})-[:LEARNED]->(c1:Course)
                MATCH (other:User)-[:LEARNED]->(c1)
                MATCH (other)-[:LEARNED]->(c2:Course)
                WHERE NOT (me)-[:LEARNED]->(c2)
                RETURN c2.id AS courseId, count(DISTINCT other) AS score
                ORDER BY score DESC
                """;

        List<Long> sorted = neo4jClient
                .query(cypher)
                .bind(userId)
                .to("userId")
                .fetchAs(String.class)
                .mappedBy((type, record) -> record.get("courseId").asString())
                .all()
                .stream()
                .map(Long::valueOf)
                .collect(Collectors.toList());

        return paginate(sorted, pageNum, pageSize);
    }

    // ==================== 画像推荐 ====================

    /**
     * 基于用户画像的内容推荐：根据兴趣分类和知识水平推荐课程
     */
    public Page<Long> recommendByProfile(Long userId, int pageNum, int pageSize) {
        UserProfileResponse profile = userProfileService.getProfile(userId.toString());
        if (profile == null) {
            return new Page<>(pageNum, pageSize);
        }

        // 获取用户已学的课程 ID，排除
        Set<Long> learnedCourseIds = getLearnedCourseIdsByUserId(userId);

        List<Long> interestIds = profile.getInterestCategoryIds();

        // 根据答题正确率推荐适合难度
        String suitableDifficulty = null;
        if (profile.getQuiz() != null && profile.getQuiz().getCorrectRate() > 0) {
            double rate = profile.getQuiz().getCorrectRate();
            suitableDifficulty = rate > 0.8 ? "advanced" : rate > 0.5 ? "intermediate" : "beginner";
        }

        return courseService.recommendPublishedCourseIds(
                interestIds == null ? Collections.emptyList() : interestIds,
                learnedCourseIds,
                suitableDifficulty,
                pageNum,
                pageSize);
    }

    // ==================== 相似度计算（定时任务） ====================

    @Scheduled(cron = "0 0 2 * * ?")
    protected void calculateSimilarity() {
        List<UserBehaviorDto> allBehaviors = learningRecordService.listAllUserBehaviors();
        if (allBehaviors.isEmpty()) return;

        Map<Long, Set<Long>> userItemMap = allBehaviors.stream()
                .collect(Collectors.groupingBy(
                        UserBehaviorDto::getUserId,
                        Collectors.mapping(UserBehaviorDto::getChapterId, Collectors.toSet())));

        Map<Long, List<Long>> itemUserMap = allBehaviors.stream()
                .collect(Collectors.groupingBy(
                        UserBehaviorDto::getChapterId,
                        Collectors.mapping(UserBehaviorDto::getUserId, Collectors.toList())));

        Map<Long, Map<Long, Integer>> coOccurrenceMap = new HashMap<>();

        for (Map.Entry<Long, List<Long>> entry : itemUserMap.entrySet()) {
            List<Long> userList = entry.getValue();
            if (userList.size() < 2) continue;

            for (int i = 0; i < userList.size(); i++) {
                Long u1 = userList.get(i);
                for (int j = i + 1; j < userList.size(); j++) {
                    Long u2 = userList.get(j);
                    coOccurrenceMap.computeIfAbsent(u1, k -> new HashMap<>()).merge(u2, 1, Integer::sum);
                    coOccurrenceMap.computeIfAbsent(u2, k -> new HashMap<>()).merge(u1, 1, Integer::sum);
                }
            }
        }

        // 清除旧的相似度数据
        userSimilarityService.deleteAllSimilarities();

        List<UserSimilaritySummaryDto> buffer = new ArrayList<>();
        for (Map.Entry<Long, Map<Long, Integer>> entry : coOccurrenceMap.entrySet()) {
            Long userId = entry.getKey();
            Map<Long, Integer> relatedUsers = entry.getValue();
            double userVectorLen = Math.sqrt(userItemMap.get(userId).size());

            PriorityQueue<UserSimilaritySummaryDto> topQueue =
                    new PriorityQueue<>(Comparator.comparing(UserSimilaritySummaryDto::similarityScore));

            for (Map.Entry<Long, Integer> relatedEntry : relatedUsers.entrySet()) {
                Long relatedUserId = relatedEntry.getKey();
                int count = relatedEntry.getValue();
                double relatedUserVectorLen =
                        Math.sqrt(userItemMap.get(relatedUserId).size());
                double score = count / (userVectorLen * relatedUserVectorLen);

                if (score < 0.1) continue;
                BigDecimal similarityScore = BigDecimal.valueOf(score);

                UserSimilaritySummaryDto sim = new UserSimilaritySummaryDto(userId, relatedUserId, similarityScore);

                if (topQueue.size() < TOP_N_NEIGHBORS) {
                    topQueue.offer(sim);
                } else if (similarityScore.compareTo(topQueue.peek().similarityScore()) > 0) {
                    topQueue.poll();
                    topQueue.offer(sim);
                }
            }
            buffer.addAll(topQueue);
            if (buffer.size() > 1000) {
                userSimilarityService.saveSummaries(buffer);
                buffer.clear();
            }
        }
        if (!buffer.isEmpty()) {
            userSimilarityService.saveSummaries(buffer);
        }
    }

    // ==================== 工具方法 ====================

    private Set<Long> getLearnedCourseIdsByUserId(Long userId) {
        List<Long> chapterIds = learningRecordService.listRecordSummariesByUserId(userId).stream()
                .map(LearningRecordSummaryDto::chapterId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        if (chapterIds.isEmpty()) {
            return Collections.emptySet();
        }
        return new HashSet<>(chapterService.listCourseIdsByChapterIds(chapterIds));
    }

    private Page<Long> paginate(List<Long> sorted, int pageNum, int pageSize) {
        Page<Long> result = new Page<>(pageNum, pageSize);
        result.setTotal(sorted.size());
        int fromIndex = (pageNum - 1) * pageSize;
        if (fromIndex >= sorted.size()) {
            result.setRecords(Collections.emptyList());
        } else {
            result.setRecords(sorted.subList(fromIndex, Math.min(fromIndex + pageSize, sorted.size())));
        }
        return result;
    }

    private record ScoredItem(Long id, double score) {}
}
