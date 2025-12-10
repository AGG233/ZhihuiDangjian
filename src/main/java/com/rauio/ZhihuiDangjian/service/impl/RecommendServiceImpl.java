package com.rauio.ZhihuiDangjian.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rauio.ZhihuiDangjian.mapper.ChapterMapper;
import com.rauio.ZhihuiDangjian.mapper.UserChapterProgressMapper;
import com.rauio.ZhihuiDangjian.mapper.UserLearningRecordMapper;
import com.rauio.ZhihuiDangjian.mapper.UserSimilarityMapper;
import com.rauio.ZhihuiDangjian.pojo.Chapter;
import com.rauio.ZhihuiDangjian.pojo.UserChapterProgress;
import com.rauio.ZhihuiDangjian.pojo.UserLearningRecord;
import com.rauio.ZhihuiDangjian.pojo.UserSimilarity;
import com.rauio.ZhihuiDangjian.pojo.dto.UserBehaviorDto;
import com.rauio.ZhihuiDangjian.service.RecommendService;
import com.rauio.ZhihuiDangjian.service.UserLearningRecordService;
import com.rauio.ZhihuiDangjian.service.UserService;
import com.rauio.ZhihuiDangjian.service.UserSimilarityService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RecommendServiceImpl implements RecommendService {

    private static final int TOP_N_NEIGHBORS = 20;

    private final UserLearningRecordMapper userLearningRecordMapper;
    private final UserLearningRecordService  userLearningRecordService;
    private final UserChapterProgressMapper userChapterProgressMapper;
    private final UserSimilarityMapper userSimilarityMapper;
    private final ChapterMapper chapterMapper;
    private final UserSimilarityService userSimilarityService;
    private final UserService userService;

    private static double calculateCosineSimilarity(Set<Long> items1, Set<Long> items2) {

        if (items1.isEmpty() || items2.isEmpty()) {
            return 0.0;
        }

        Set<Long> intersection = new HashSet<>(items1);
        intersection.retainAll(items2);
        int commonCount = intersection.size();

        if (commonCount == 0) {
            return 0.0;
        }
        double denominator = Math.sqrt(items1.size() * items2.size());
        return (double) Math.round((commonCount / denominator) * 1000) / 1000;
    }

    @Override
    public Page<Long> recommendByCF(Long userId,int pageNum, int pageSize) {

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

        List<Long> similarUserIds = similarityList.stream()
                .map(UserSimilarity::getUserId2)
                .collect(Collectors.toList());


        Set<Long> userLearnedCourseIds = new HashSet<>(userLearningRecordService.selectLearnedCoursesByUserId(userId));

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

        Set<Long> allInvolvedChapterIds = new HashSet<>();
        allInvolvedChapterIds.addAll(records.stream().map(UserLearningRecord::getChapterId).toList());
        allInvolvedChapterIds.addAll(progresses.stream().map(UserChapterProgress::getChapterId).toList());

        if (allInvolvedChapterIds.isEmpty()) {
            return new Page<>(pageNum, pageSize);
        }

        List<Chapter> chapters = chapterMapper.selectByIds(allInvolvedChapterIds);
        Map<Long, Long> chapterToCourseMap = chapters.stream()
                .collect(Collectors.toMap(Chapter::getId, Chapter::getCourseId));

        Map<Long, Double> courseScoreMap = new HashMap<>();

        Consumer<Long> addScore = (chapterId) -> {
            Long courseId = chapterToCourseMap.get(chapterId);
            if (courseId != null) {
                if (!userLearnedCourseIds.contains(courseId)) {
                    courseScoreMap.merge(courseId, 1.0, Double::sum);
                }
            }
        };

        records.forEach(r -> addScore.accept(r.getChapterId()));
        progresses.forEach(p -> addScore.accept(p.getChapterId()));

        List<Long> sortedCourseIds = courseScoreMap.entrySet().stream()
                .sorted(Map.Entry.<Long, Double>comparingByValue().reversed())
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        Page<Long> resultPage = new Page<>(pageNum, pageSize);
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
    @Transactional(rollbackFor = Exception.class)
    protected void calculateSimilarity(){
        long start = System.currentTimeMillis();

        List<UserBehaviorDto> allBehaviors = userLearningRecordMapper.getAllUserBehaviors();
        if (allBehaviors.isEmpty()) return;

        Map<Long, Set<Long>> userItemMap = allBehaviors.stream()
                .collect(Collectors.groupingBy(
                        UserBehaviorDto::getUserId,
                        Collectors.mapping(UserBehaviorDto::getChapterId, Collectors.toSet())
                ));

        Map<Long, List<Long>> itemUserMap = allBehaviors.stream()
                .collect(Collectors.groupingBy(
                        UserBehaviorDto::getChapterId,
                        Collectors.mapping(UserBehaviorDto::getUserId, Collectors.toList())
                ));

        Map<Long, Map<Long, Integer>> coOccurrenceMap = new HashMap<>();


        for (Map.Entry<Long, List<Long>> entry : itemUserMap.entrySet()) {
            List<Long> userList = entry.getValue();

            if (userList.size() < 2) continue;


            for (int i = 0; i < userList.size(); i++) {
                Long u1 = userList.get(i);
                for (int j = i + 1; j < userList.size(); j++) {
                    Long u2 = userList.get(j);

                    coOccurrenceMap.computeIfAbsent(u1, k -> new HashMap<>())
                            .merge(u2, 1, Integer::sum);
                    coOccurrenceMap.computeIfAbsent(u2, k -> new HashMap<>())
                            .merge(u1, 1, Integer::sum);
                }
            }
        }

        List<UserSimilarity> buffer = new ArrayList<>();

        for (Map.Entry<Long, Map<Long, Integer>> entry : coOccurrenceMap.entrySet()) {
            Long userId = entry.getKey();
            Map<Long, Integer> relatedUsers = entry.getValue();

            double userVectorLen = Math.sqrt(userItemMap.get(userId).size());

            PriorityQueue<UserSimilarity> topQueue = new PriorityQueue<>(
                    Comparator.comparingDouble(UserSimilarity::getSimilarityScore)
            );

            for (Map.Entry<Long, Integer> relatedEntry : relatedUsers.entrySet()) {
                Long relatedUserId = relatedEntry.getKey();
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