package com.rauio.smartdangjian.service.search;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rauio.smartdangjian.mapper.ChapterMapper;
import com.rauio.smartdangjian.mapper.UserChapterProgressMapper;
import com.rauio.smartdangjian.mapper.UserLearningRecordMapper;
import com.rauio.smartdangjian.mapper.UserSimilarityMapper;
import com.rauio.smartdangjian.pojo.Chapter;
import com.rauio.smartdangjian.pojo.UserChapterProgress;
import com.rauio.smartdangjian.pojo.UserLearningRecord;
import com.rauio.smartdangjian.pojo.UserSimilarity;
import com.rauio.smartdangjian.pojo.dto.UserBehaviorDto;
import com.rauio.smartdangjian.service.learning.UserLearningRecordService;
import com.rauio.smartdangjian.service.user.UserSimilarityService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Deprecated
public class RecommendService {

    private static final int TOP_N_NEIGHBORS = 20;

    private final UserLearningRecordMapper userLearningRecordMapper;
    private final UserLearningRecordService  userLearningRecordService;
    private final UserChapterProgressMapper userChapterProgressMapper;
    private final UserSimilarityMapper userSimilarityMapper;
    private final ChapterMapper chapterMapper;
    private final UserSimilarityService userSimilarityService;

    public Page<String> recommendByCF(String userId,int pageNum, int pageSize) {

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


        Set<String> userLearnedCourseIds = new HashSet<>(userLearningRecordService.selectLearnedCoursesByUserId(userId));
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
            if (courseId != null) {
                if (!userLearnedCourseIds.contains(courseId)) {
                    courseScoreMap.merge(courseId, 1.0, Double::sum);
                }
            }
        };

        records.forEach(r -> addScore.accept(r.getChapterId()));
        progresses.forEach(p -> addScore.accept(p.getChapterId()));

        List<String> sortedCourseIds = courseScoreMap.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        Page<String> resultPage = new Page<>(pageNum, pageSize);
        resultPage.setTotal(sortedCourseIds.size());

        int fromIndex = (pageNum - 1) * pageSize;
        if (fromIndex >= sortedCourseIds.size()) {
            resultPage.setRecords(Collections.emptyList());
        } else {
            int toIndex = Math.min(fromIndex + pageSize, sortedCourseIds.size());
            resultPage.setRecords(sortedCourseIds.subList(fromIndex, toIndex));
        }

        return resultPage;
    }

    @Scheduled(cron = "0 0 2 * * ?")
    @Transactional
    protected void calculateSimilarity(){

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

        List<UserSimilarity> buffer = new ArrayList<>();

        for (Map.Entry<String, Map<String, Integer>> entry : coOccurrenceMap.entrySet()) {
            String userId = entry.getKey();
            Map<String, Integer> relatedUsers = entry.getValue();

            double userVectorLen = Math.sqrt(userItemMap.get(userId).size());

            PriorityQueue<UserSimilarity> topQueue = new PriorityQueue<>(
                    Comparator.comparingDouble(UserSimilarity::getSimilarityScore)
            );

            for (Map.Entry<String, Integer> relatedEntry : relatedUsers.entrySet()) {
                String relatedUserId = relatedEntry.getKey();
                Integer count = relatedEntry.getValue();

                double relatedUserVectorLen = Math.sqrt(userItemMap.get(relatedUserId).size());

                double score = count / (userVectorLen * relatedUserVectorLen);

                if (score < 0.1) continue;

                UserSimilarity sim = UserSimilarity.builder()
                        .userId1(userId)
                        .userId2(relatedUserId)
                        .similarityScore(score).build();

                // 维护 Top N
                if (topQueue.size() < TOP_N_NEIGHBORS) {
                    topQueue.offer(sim);
                } else if (score > topQueue.peek().getSimilarityScore()) {
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
        if(!buffer.isEmpty()){
            userSimilarityService.saveBatch(buffer);
        }
    }
}
