package com.rauio.ZhihuiDangjian.service;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.rauio.ZhihuiDangjian.mapper.UserChapterProgressMapper;
import com.rauio.ZhihuiDangjian.mapper.UserLearningRecordMapper;
import com.rauio.ZhihuiDangjian.mapper.UserSimilarityMapper;
import com.rauio.ZhihuiDangjian.pojo.UserChapterProgress;
import com.rauio.ZhihuiDangjian.pojo.UserLearningRecord;
import com.rauio.ZhihuiDangjian.pojo.UserSimilarity;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RecommendService {

    private final UserLearningRecordMapper userLearningRecordMapper;
    private final UserChapterProgressMapper userChapterProgressMapper;
    private final UserSimilarityMapper userSimilarityMapper;

    private static double calculateCosineSimilarity(Set<String> items1, Set<String> items2) {

        if (items1.isEmpty() || items2.isEmpty()) {
            return 0.0;
        }

        Set<String> intersection = new HashSet<>(items1);
        intersection.retainAll(items2);
        int commonCount = intersection.size();

        if (commonCount == 0) {
            return 0.0;
        }
        double denominator = Math.sqrt(items1.size() * items2.size());
        return (double) Math.round((commonCount / denominator) * 1000) / 1000;
    }

    @Scheduled(cron = "0 0 * * * ?")
    private void calculateSimilarity(){

        LocalDateTime now = LocalDateTime.now();
        Duration duration = Duration.between(now, LocalDateTime.now());

        List<UserSimilarity> list = new ArrayList<>();

        LambdaQueryWrapper<UserLearningRecord> queryWrapper1 = new LambdaQueryWrapper<>();
        LambdaQueryWrapper<UserChapterProgress> queryWrapper2 = new LambdaQueryWrapper<>();

        //构造查询条件
        queryWrapper1.select(UserLearningRecord::getUserId, UserLearningRecord::getChapterId);
        queryWrapper2.select(UserChapterProgress::getUserId, UserChapterProgress::getChapterId);

        // 查询相关行为
        List<UserLearningRecord>    learningRecords     = userLearningRecordMapper
                .selectList(queryWrapper1);
        List<UserChapterProgress>   chapterProgresses   = userChapterProgressMapper
                .selectList(queryWrapper2);


        //将用户行为数据分组，以用户ID为键，行为数据为值
        Map<String, Set<String>> learningRecordsMap = learningRecords.stream()
                .collect(Collectors.groupingBy(
                        UserLearningRecord::getUserId,
                        Collectors.mapping(
                                UserLearningRecord::getChapterId,
                                Collectors.toSet()
                        )
                ));
        Map<String, Set<String>> chapterProgressMap = chapterProgresses.stream()
                .collect(Collectors.groupingBy(
                        UserChapterProgress::getUserId,
                        Collectors.mapping(
                                UserChapterProgress::getChapterId,
                                Collectors.toSet()
                        )
                ));
        //合并数据
        learningRecordsMap.forEach((key, value) -> {
            chapterProgressMap.merge(key, value, (set1, set2) -> {
                set1.addAll(set2);
                return set1;
            });
        });


        for (String userA : learningRecordsMap.keySet()) {
            for (String userB : learningRecordsMap.keySet()) {
                Set<String> similarUserA = learningRecordsMap.get(userA);
                Set<String> similarUserB = learningRecordsMap.get(userB);

                double score = calculateCosineSimilarity(similarUserA, similarUserB);

                if (score > 0.1) {
                    // 存 A -> B
                    list.add(new UserSimilarity(userA, userB, score));
                    // 存 B -> A (为了查询方便，通常存双向)
                    list.add(new UserSimilarity(userB, userA, score));
                }
            }
        }
        userSimilarityMapper.delete(null);
        userSimilarityMapper.insert(list);
    }
}
